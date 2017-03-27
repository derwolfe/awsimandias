(ns awsimandias.aws-test
  (:require
   [clojure.test :refer :all]
   [clojure.pprint :as pprint]
   [awsimandias.aws :as aws]
   [manifold.deferred :as md]))

(deftest all-ec2-instances-tests
  ;; this is gross and meant to be temporary. Assuming we have network
  ;; connectivity, check that we are able to use amazonica's stuff to actually
  ;; talk to AWS.
  ;; this is relying on certain instances with certain properties being available in AWS.
  (let [creds (first (aws/accounts-from-env!))]
    (testing "it gets all of them when they are there"
      (let [result @(aws/all-ec2-instances! creds)]
        (is (= 3 (count result)))))
    (testing "it gets all of ssms when they are there"
      (let [result @(aws/all-ssm-instances! creds)]
        (is (= 1 (count result)))))))
