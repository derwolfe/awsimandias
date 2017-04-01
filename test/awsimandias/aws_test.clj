(ns awsimandias.aws-test
  (:require
   [clojure.test :refer :all]
   [clojure.pprint :as pprint]
   [awsimandias.aws :as aws]
   [manifold.deferred :as md]))

;; this is gross and meant to be temporary. Assuming we have network
;; connectivity, check that we are able to use amazonica's stuff to actually
;; talk to AWS.
;; this is relying on certain instances with certain properties being available in AWS.

(deftest all-ec2-instances-tests
  (let [creds (aws/accounts-from-env!)]
    (testing "it gets all of them when they are there"
      (let [result @(aws/all-ec2-instances! creds)]
        (is (= 3 (count result)))))
    (testing "it gets all ec2-images for the customer"
      (let [ec2s @(aws/all-ec2-instances! creds)
            result @(aws/all-ec2-images! ec2s creds)]
        (is (not (empty? result)))))
    (testing "it gets all of ssms when they are there"
      (let [result @(aws/all-ssm-instances! creds)]
        (is (= 1 (count result)))))
    (testing "get the ssmified instances"
      (let [result @(aws/ssmified-ec2-instances! creds)
            with-ssm (filter #(= true (:has-ssm %)) result)
            without-ssm (filter #(= false (:has-ssm %)) result)]
        (pprint/pprint result)
        (is (= 3 (count result)))
        (is (= 1 (count with-ssm)))
        (is (= 2 (count without-ssm)))))))
