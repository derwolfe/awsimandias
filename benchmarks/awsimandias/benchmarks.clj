(ns awsimandias.benchmarks
  (:require
   [awsimandias.aws :as aws]
   [perforate.core :as perf]))

(perf/defgoal throughput-bench "benchmark a normal single account AWS workload using the network")

(perf/defcase throughput-bench :ssmified-ec2-instances
  []
  @(aws/ssmified-ec2-instances! (aws/accounts-from-env!)))
