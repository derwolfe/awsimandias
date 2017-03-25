(ns awsimandias.aws-test
  (:require
   [clojure.test :refer :all]
   [clojure.string :as str]
   [environ.core :refer [env]]
   [awsimandias.aws :as aws]
   [manifold.deferred :as md]))

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

(deftest all-ec2-instances-tests
  ;; this is gross and meant to be temporary. Assuming we have network
  ;; connectivity, check that we are able to use amazonica's stuff to actually
  ;; talk to AWS.
  (let [creds (accounts-from-env!)]
    (testing "it gets all of them when they are there"
      (is (= [] @(apply md/zip (map #(aws/all-ec2-instances! %) creds)))))))
