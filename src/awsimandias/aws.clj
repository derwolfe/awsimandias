(ns awsimandias.aws
  (:import
   [com.amazonaws.regions Region RegionUtils])
  (:require
   [environ.core :refer [env]]
   [amazonica.core :as ac]
   [amazonica.aws.ec2 :as ec2]
   [amazonica.aws.simplesystemsmanagement :as ssm]
   [manifold.deferred :as md]))

(defn accounts-from-env!
  []
  (let [{:keys [aws-access-key aws-secret-key]} env]
    {:access-key aws-access-key
     :secret-key aws-secret-key}))

;; goal - enumerate the entirety of a customer's AWS ec2 footprint with the
;; maximum amount of concurrency and hopefully parallelism available to the
;; system

(defn ec2-region-names!
  "Get the regions for the given credential.

  creds - a map containing the keys :access-key and :secret-key.

  Returns a deferred that fires when ec2/describe-regions completes."
  [{:keys [access-key secret-key]}]
  (md/future
    (ac/with-credential [access-key secret-key "us-east-1"]
      (:regions (ec2/describe-regions)))))

(defn ec2-instances!
  "Get all ec2 instances for a specific account in a specific region.

  creds - a map containing access-key, secret-key, and endpoint name

  Returns a deferred that fires once describe instances completes."
  [{:keys [access-key secret-key endpoint-name region-name]}]
  (md/future
    (ac/with-credential [access-key secret-key endpoint-name]
      (md/chain
       (ec2/describe-instances)
       (fn [ec2s]
         (let [rs (:reservations ec2s)
               instances (flatten (map :instances rs))
               with-regions (map #(assoc % :aws-region-name region-name) instances)]
           with-regions))))))

(defn all-ec2-instances!
  "Get all ec2 instances across all regions for a given account.

  creds - a map containing the keys :access-key and :secret-key

  Returns a deferred that will fire when each region has returned its response."
  [creds]
  (md/let-flow [regions (ec2-region-names! creds)]
    (md/chain
     (apply md/zip
            (for [region regions
                  :let [{:keys [region-name endpoint]} region
                        args (-> (assoc creds :endpoint-name endpoint)
                                 (assoc :region-name region-name))]]
              (ec2-instances! args)))
     flatten)))

(defn ssm-regions
  "Extract the SSM able regions from the java SDK. This could lag behind what is
  available from the HTTP api."
  []
  (map #(.getName ^Region %) (RegionUtils/getRegionsForService "ssm")))

(defn ssm-instances!
  "Find which ec2 instances have SSM enabled and use the SSM service to query these hosts.

  creds - a map containing the keys :access-key, :secret-key, and the region name

  Returns a deferred that fires when all SSM instance information has been returned"
  [{:keys [access-key secret-key endpoint-name]}]
  (md/future
    (ac/with-credential [access-key secret-key endpoint-name]
      (ssm/describe-instance-information {}))))

(defn all-ssm-instances!
  "Get all of the ec2 instances with SSM enabled across all regions for a given account.

  creds - a map with a :secret-key and :access-key

  Returns a deferred that fires after all regions have returned"
  [creds]
  (let [regions (ssm-regions)]
    (md/chain
     (apply md/zip (map #(ssm-instances! (assoc creds :endpoint-name %)) regions))
     (fn [cs]
       (mapcat :instance-information-list cs)))))

(defn ssmify-ec2
  "Given two seqs, one of ec2 maps, and the other of ssm maps, match up the ec2
  instances with their ec2 counterparts. If there are no ec2 devices, then the
  ssm information won't show up.

  Returns a list of ec2 devices (maps that should have a spec, maybe?) that will
  have had their os-name and os-versions fortified by the information present in
  ssm."
  [ec2s ssms]
  (let [ssms-by-id (group-by :instance-id ssms)]
    (for [ec2 ec2s
          :let [instance-id (:instance-id ec2)
                ssm (get ssms-by-id instance-id)]]
      (if-not (nil? ssm)
        (assoc ec2 ::has-ssm true)
        (assoc ec2 ::has-ssm false)))))

(defn ec2-images!
  "Get all ec2 image ids and names for the ec2 instances currently in use."
  [image-ids {:keys [access-key secret-key endpoint-name]}]
  (md/future
    (ac/with-credential [access-key secret-key endpoint-name]
      (ec2/describe-images :image-ids image-ids))))

(defn all-ec2-images!
  "Get all ec2 images across all customer regions.

  ec2s - a list of ec2 instances
  creds - map containing an aws secret key and aws access key

  Returns a deferred containing a list of maps representing the image name and
  image id."
  [ec2s creds]
  (let [regions (ssm-regions)]
    (md/chain
     ;; it is possible for this to return 404 if an image id is requested in a
     ;; region where it doesn't exist
     (apply md/zip
            (for [region regions
                  :let [ec2s-in-region (filter #(= (:aws-region-name %) region) ec2s)
                        image-ids (map :image-id ec2s-in-region)]]
              (if (seq image-ids)
                (ec2-images! image-ids (assoc creds :endpoint-name region))
                (md/success-deferred :images '()))))
     (fn [imgs]
       (let [by-region (flatten (map :images imgs))
             ;; this query could likely be simplified to only include id and name instead of
             ;; returning extra data.
             all-amis
             (for [image by-region
                   :let [{:keys [image-id name]} image]
                   :when (some? image)]
               {:image-id image-id :name name})]
         (into {} all-amis))))))

(defn enrich-os
  "Take a list of ec2 instances and a list of maps that contain the ami
  information. If there are instances that are using any of the amis, add that
  data to the ec2 instance.

  Returns a sequence of maps."
  [ec2s amis]
  (for [ec2 ec2s
        :let [default "Unknown OS"
              match (get amis (:instance-id ec2))]]
    (if (some? match)
      (assoc ec2 ::os-name (:name match))
      (assoc ec2 ::os-name default))))

(defn ssmified-ec2-instances!
  "Get the ec2 instances and their ssm information. Once done, smash the lists
  together and tell us which devices have ssm.

  cred - a credential map with a :secret-key and :access-key

  Returns a deferred that fires when all of the ec2 and ssm info for every
  instance in every region for the credential's account has been fetched and
  those instances havebeen reconciled."
  [cred]
  (md/let-flow [ec2s (all-ec2-instances! cred)
                ssms (all-ssm-instances! cred)]
    (md/chain
     (all-ec2-images! ec2s cred)
     (fn [amis]
       (-> (enrich-os ec2s amis)
           (ssmify-ec2 ssms))))))
