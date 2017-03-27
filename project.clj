(defproject awsimandias "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [aleph "0.4.2-alpha8"]
                 [io.netty/netty-all "4.1.0.Final"]
                 [manifold "0.1.6-alpha3"]
                 [cheshire "5.6.3"]
                 [amazonica "0.3.77"]
                 [environ "1.1.0"]
                 [compojure "1.5.1"]
                 [com.soundcloud/prometheus-clj "2.4.0"]
                 [com.taoensso/timbre "4.5.1"]
                 [perforate "0.3.4"]]
  :plugins [[lein-auto "0.1.2"]
            [lein-ancient "0.6.10"]
            [lein-cljfmt "0.3.0"]
            [lein-pprint "1.1.1"]
            [lein-environ "1.0.2"]
            [jonase/eastwood "0.2.3"]
            [perforate "0.3.4"]
            [lein-cloverage "1.0.9-SNAPSHOT"]]
  :min-lein-version "2.0.0"
  :eastwood {:add-linters [:unused-namespaces
                           :unused-locals
                           :unused-private-vars]
             :exclude-linters [:constant-test]}
  :cljfmt {:indents {let-flow [[:inner 0]]
                     catch [[:inner 0]]}}
  :global-vars {*warn-on-reflection* true}

  :main ^:skip-aot awsimandias.core
  :perforate {:environments [{:name :version1
                              :profiles [:test]
                              :namespaces [awsimandias.benchmarks]}]}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :user {:dependencies [[pjstadig/humane-test-output "0.8.1"]]
                    :injections [(require 'pjstadig.humane-test-output)
                                 (pjstadig.humane-test-output/activate!)]}})
