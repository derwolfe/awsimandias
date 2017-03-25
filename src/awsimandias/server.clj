(ns awsimandias.server
  (:require
   [aleph.http :as http]
   [clojure.string :as str]
   [clojure.java.io :as io])
  (:import
   java.io.InputStream
   java.security.KeyStore
   java.security.KeyFactory
   java.security.PrivateKey
   java.security.cert.X509Certificate
   java.security.cert.CertificateFactory
   java.security.spec.PKCS8EncodedKeySpec
   java.io.ByteArrayInputStream
   javax.xml.bind.DatatypeConverter
   io.netty.handler.ssl.SslContext
   io.netty.handler.ssl.SslContextBuilder
   io.netty.handler.ssl.ClientAuth))

;; things needed to setup the certificate objects for netty
;; this is all from `https://github.com/pyr/net`

;; (defn ^X509Certificate s->cert
;;   [factory input]
;;   (prn input)
;;   (.generateCertificate ^CertificateFactory factory
;;                         (ByteArrayInputStream. (.getBytes ^String input))))
;;
;; (defn ^PrivateKey s->pkey
;;   "When reading private keys, we unfortunately have to
;;   read PKCS8 encoded keys, short of pulling-in bouncy castle :-(
;;   Since these keys are usually DER encoded, they're unconvienent to
;;   have laying around in strings. We resort to base64 encoded DER here."
;;   [^KeyFactory factory input]
;;   (let [wo-words (last (re-find #"(?ms)^-----BEGIN ?.*? PRIVATE KEY-----$(.+)^-----END ?.*? PRIVATE KEY-----$" input))
;;         bytes (DatatypeConverter/parseBase64Binary wo-words)
;;         kspec (PKCS8EncodedKeySpec. bytes)]
;;     (.generatePrivate factory kspec)))
;;
;; (defn ->chain
;;   [^CertificateFactory fact cert-spec]
;;   (prn cert-spec)
;;   (if (sequential? cert-spec)
;;     (into-array X509Certificate (map (partial s->cert fact) cert-spec))
;;     (into-array X509Certificate [(s->cert fact cert-spec)])))
;;
;; (defn client-context
;;   "Build an SSL client context for netty"
;;   [{:keys [cert pkey authority]}]
;;   (-> (SslContextBuilder/forClient)
;;       (.trustManager (.getBytes ^String ca))
;;       (.keyManager (.getBytes ^String cert) (.getBytes ^String pkey))
;;       .build))
;;
;; (defn server-context
;;   "Build an SSL client context for netty"
;;   [{:keys [pkey cert ca-cert]}]
;;
;;   )
;; (defn server-context
;;   "Build an SSL client context for netty"
;;   [{:keys [pkey cert ca-cert ciphers
;;            cache-size session-timeout storage]}]
;;   (binding [*storage* (or storage :guess)]
;;     (let [fact     (CertificateFactory/getInstance "X.509")
;;           certs    ^"[Ljava.security.cert.X509Certificate;" (->chain fact cert)
;;           key-fact (KeyFactory/getInstance "RSA")
;;           pk       (s->pkey key-fact pkey)
;;           builder  ^SslContextBuilder (SslContextBuilder/forServer pk certs))]
;;     (when ciphers
;;         (.ciphers builder ciphers))
;;       (when ca-cert
;;         (.trustManager builder
;;                        ^"[Ljava.security.cert.X509Certificate;"
;;                        (into-array X509Certificate (->chain fact ca-cert))))
;;       (.clientAuth builder :auth-mode-require  ClientAuth/REQUIRE))
;;       (.build ^SslContextBuilder builder))))
