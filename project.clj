(defproject awsimandias "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [aleph "0.4.2-alpha8"]
                 [manifold "0.1.6-alpha3"]
                 [amazonica "0.3.77"]
                 [cheshire "5.6.3"]]
  :plugins [[lein-auto "0.1.2"]
            [lein-ancient "0.6.10"]
            [lein-cljfmt "0.3.0"]
            [lein-pprint "1.1.1"]
            [lein-environ "1.0.2"]
            [jonase/eastwood "0.2.3"]
            [lein-cloverage "1.0.7-SNAPSHOT"]]
  :min-lein-version "2.0.0"
  :eastwood {:add-linters [:unused-namespaces
                           :unused-locals
                           :unused-private-vars]
             :exclude-linters [:constant-test]}
  :main ^:skip-aot awsimandias.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
