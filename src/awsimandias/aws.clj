(ns awsimandias.aws
  (:import
   [com.amazonaws.regions Region RegionUtils])
  (:require
   [amazonica.core :as ac]
   [amazonica.aws.ec2 :as ec2]
   [amazonica.aws.simplesystemsmanagement :as ssm]
   [manifold.deferred :as md]))

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
           (map :region-name)))))

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
    (apply md/zip (map #(ec2-instances! (assoc creds :endpoint-name %)) region-names))))

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
  (apply md/zip (map #(ssm-instances! (assoc creds :endpoint-name %)) (ssm-regions))))

;; then use that to make all AWS calls

;; for all accounts
;; these can be cached
;; get all ssm regions
;; get all ec2 regions


;; for a given region and customer
;; get all ec2 instances
;; get all ssm instances
