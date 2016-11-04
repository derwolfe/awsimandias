(ns awsimandias.server
  (:require
   [aleph.http :as http]
   [clojure.java.io :as io])
  (:import
   [java.io InputStream]
   [io.netty.handler.ssl SslContextBuilder ClientAuth]))

(defn ^InputStream string->input-stream
  [s]
  (io/input-stream (.getBytes s)))

(defn server-mutual-auth-tls-context
  "Create a mutually authenticated TLS context for servers

  ca - an x509 certificate that forms the trust root for the server's
  certificate.
  server-cert (string) - an x509 certificate issue by the ca.
  server-key (string)- a pkcs#8 encoded string representing the private key of
  the server."
  [ca server-cert server-key]
  (let [cert-chain (string->input-stream (str ca server-cert))
        key (string->input-stream server-key)]
    (doto
        (SslContextBuilder/forServer cert-chain key)
      (.clientAuth ClientAuth/REQUIRE)
      (.build))))


(defn client-mutual-auth-tls-context
  "Creates a mutually authenticated TLS context for client

  ca - an x509 certificate that forms the trust root for the server's
  certificate.
  client-cert (string) - an x509 certificate issue by the ca.
  client-key (string)- a pkcs#8 encoded string representing the private key of
  the server.
  server-cert (string) - an x509 certificate issue by the ca."
  [ca client-cert client-key server-cert]
  (let [client-cert-chain (string->input-stream (str ca client-cert))
        server-cert-chain (string->input-stream (str ca server-cert))
        key (string->input-stream client-key)]
    (doto
        (SslContextBuilder/forClient)
      (.trustManager server-cert-chain)
      (.keyManager client-cert-chain key nil)
      (.build))))
