(ns awsimandias.aws
  (:import
   [com.amazonaws.regions Region RegionUtils])
  (:require
   [clojure.string :as str]
   [environ.core :refer [env]]
   [amazonica.core :as ac]
   [amazonica.aws.ec2 :as ec2]
   [amazonica.aws.simplesystemsmanagement :as ssm]
   [manifold.deferred :as md]
   [taoensso.timbre :as timbre]))

(defn accounts-from-env!
  []
  (let [acct-string (:accounts env)
        creds (str/split acct-string #";")
        secrets-and-keys (map #(str/split % #":") creds)
        as-creds (map (fn [cred-vec]
                        {:access-key (first cred-vec)
                         :secret-key (second cred-vec)})
                      secrets-and-keys)]
    as-creds))

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
      (->> (ec2/describe-regions)
           :regions
           (map :endpoint)))))

(defn ec2-instances!
  "Get all ec2 instances for a specific account in a specific region.

  creds - a map containing access-key, secret-key, and endpoint name

  Returns a deferred that fires once describe instances completes."
  [{:keys [access-key secret-key endpoint-name]}]
  (md/future
    (ac/with-credential [access-key secret-key endpoint-name]
      (ec2/describe-instances))))

(defn all-ec2-instances!
  "Get all ec2 instances across all regions for a given account.

  creds - a map containing the keys :access-key and :secret-key

  Returns a deferred that will fire when each region has returned its response."
  [creds]
  (md/let-flow [region-names (ec2-region-names! creds)]
    (md/chain
     (apply md/zip (map #(ec2-instances! (assoc creds :endpoint-name %)) region-names))
     (fn [cs]
       (->> (mapcat #(:reservations %) cs)
            (mapcat #(:instances %)))))))

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
       (mapcat #(:instance-information-list %) cs)))))

(defn ssmify-ec2
  "Given two seqs, one of ec2 maps, and the other of ssm maps, match up the ec2
  instances with their ec2 counterparts. If there are no ec2 devices, then the
  ssm information won't show up.

  Returns a list of ec2 devices (maps that should have a spec, maybe?) that will
  have had their os-name and os-versions fortified by the information present in
  ssm.

  (Note) That seems weird; why should there be an ssm enabled device if there
  isn't one in ec2?"
  [ec2s ssms]
  ;; read these into a dictionary
  (let [ssms-by-id (group-by #(:instance-id %) ssms)]
    (for [ec2 ec2s
          :let [instance-id (:instance-id ec2)
                ssm (get ssms-by-id instance-id)]]
      (if (not (nil? ssm))
        (assoc ec2 :has-ssm true)
        (assoc ec2 :has-ssm false)))))

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
    (ssmify-ec2 ec2s ssms)))
