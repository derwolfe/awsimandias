(ns awsimandias.core
  (:require
   [awsimandias.aws :as aws]
   [aleph.http :as http]
   [compojure.core :as cjc]
   [compojure.route :as cjr]
   [compojure.response :refer [Renderable]]
   [manifold.deferred :as md]
   [taoensso.timbre :as timbre])
  (:gen-class))

(def creds (atom []))

(defn ssm-instances-handler
  [req]
  (md/chain
   (aws/all-ssm-instances! (first @creds))
   (fn [ds]
     {:status 200
      :headers {"Content-Type" "application/edn"}
      :body (pr-str ds)})))

(cjc/defroutes routes
  (cjc/GET "/" [] ssm-instances-handler)
  (cjr/not-found "404: Not Found"))

(defn ^:private staying-alive
  []
  (.start (Thread. (fn [] (.join (Thread/currentThread))) "staying alive")))

;; make it so compojure doesn't block on deferreds being dereferenced
(extend-protocol Renderable
  manifold.deferred.Deferred
  (render [md _] md))

(defn -main
  [& args]
  (staying-alive)
  (reset! creds (aws/accounts-from-env!))
  (http/start-server routes {:port 9999}))
