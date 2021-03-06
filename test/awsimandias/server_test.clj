(ns awsimandias.server-test
  (:require
   [aleph
    [http :as http]
    [netty :as netty]]
   [clojure.java.io :as io]
   [clojure.test :refer :all]))

;; these should be generated automatically

(def ca-cert "-----BEGIN CERTIFICATE-----
MIICyTCCAbGgAwIBAgIQDevqk5o7Q9+N7FegsJ/95DANBgkqhkiG9w0BAQsFADAW
MRQwEgYDVQQDDAthd3NpbWFuZGlhczAeFw0xNjExMDIxNTQ3NTJaFw0yNjExMDEx
NTQ3NTJaMBYxFDASBgNVBAMMC2F3c2ltYW5kaWFzMIIBIjANBgkqhkiG9w0BAQEF
AAOCAQ8AMIIBCgKCAQEA9UtskrayEKtMC3lCApFB4m4DagY65FN7njygzHy4FZFU
JjYo0dUT14OJo6dOGYm5ZjuCtHJoIe0GMTs9pIqzJg+L2nZUZ1A1G6nXauve3St6
ELtfkckWSUggfy8Pwi7ERqNkdQjeF04TPTBC09yib8UeovVZySZmhUwq8VNH3FOn
JjDegJtNCTDFOQAZCYjwRmb30arvN2dUA+Fj24lSRm6aUjDzFvllTdy2zM/AddbZ
tonXEyXI4XUBrzdq+xIu6+m+gfmt5QpACK7U4VXGbzZ3uL87Jg9TGHOUunXNEPSX
6kKkBsiWsS938fVtzJShtCW64n+u7HTl7y5N0Pc6LQIDAQABoxMwETAPBgNVHRMB
Af8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQCxaZN1sPy2Z3FWHeKLYV6fxZxH
KFf7VihAtWGjZsLDIxz8a/ZJ3u8yDuTlmhNzFqwgwvcsXDuOn2HN8JWzRYSt/pLN
XTrmVG8e0y83KUHGHgp7dGHNdzm5KB/JT2zHv59q2X+I1ylRQBXMcrGGJXxS0JBi
WaalGEZ6bmiUoe3YzNIs3SQQkIvHx1AjJnsWZ8rQiVczJsrZpuK82vS4GuCKRyXm
BNjiDyCiRO7MuIA7p4sdj8GXRoz8D96CxCbXPZmOjAk5eEXzL8oZCMklImunmh7s
ze+WGkYuBXM/aFHasNt1cM+sAuc+1FJNHmN0HvPMhqqQGbR0a54uvsfNugeQ
-----END CERTIFICATE-----")

(def ca-key "-----BEGIN RSA PRIVATE KEY-----
MIIEpgIBAAKCAQEA9UtskrayEKtMC3lCApFB4m4DagY65FN7njygzHy4FZFUJjYo
0dUT14OJo6dOGYm5ZjuCtHJoIe0GMTs9pIqzJg+L2nZUZ1A1G6nXauve3St6ELtf
kckWSUggfy8Pwi7ERqNkdQjeF04TPTBC09yib8UeovVZySZmhUwq8VNH3FOnJjDe
gJtNCTDFOQAZCYjwRmb30arvN2dUA+Fj24lSRm6aUjDzFvllTdy2zM/AddbZtonX
EyXI4XUBrzdq+xIu6+m+gfmt5QpACK7U4VXGbzZ3uL87Jg9TGHOUunXNEPSX6kKk
BsiWsS938fVtzJShtCW64n+u7HTl7y5N0Pc6LQIDAQABAoIBAQCzGUYF/WUYLKpS
ek/LJhoP5LmTcUcQrS/GQog5phqWjMz/NW4qf0SEh+M4XZD1roxy66KsdBg6JSDv
U0zX1fmmIZXAhcNsx7BvKVxDEkNgAz2V9+l4vXULPqGTm14LLzBaTzSrCzV1mKkN
dS5mGGN17lxHKZNL/kC+tPVpzJ/car3gZlRFdPDKj7a5u+EgkjRV37aJ8WSXTesO
li3xZ2PeYh5af0vEqjh/nEhW/ztSj+lz+THjOP6H7zIlaZJ7THG33Hkr1uz88hD3
PPCoQYX3NmDx46UPl/uQ4IU/UgoVHD4JUTb0hfu8Y5Is7JJtcjss86rI3eotKxHa
uaF1+PtBAoGBAPv7xdKw1ohVylaGeEz2fdxYFtDUyHpOJJiWn2KdUvKxLadxVI2I
bYyr70l4aFo+5x0gFUHNR15u9PtmJOGTZXQaptxChpHhaOangifcTvYQSjn72BGX
+VAJJscsoDUd5I5hg8ptoM1bfzyKD+T3rj5sIcjMMwDSNdg5Fj9CLL0RAoGBAPk0
W3PsLle/HoA8ySW+CUqeGebVS596F27h6ticyW+5mezNie/Ij6Nxvz8kZVUeBqEF
JGd/IT5EkSTYrVEBsG/qqApP12Jglc9Og7F1g3SnH69zD1iTCjAPEcrdJqoVv/rd
8oNOHLSR0fHBg8tn/NlQ6cfPHAcKHAx8vXGladtdAoGBAM8syxIWONyxBIItXx2Q
EB0pA+FBAW+lbetS88qFpRxK1kl0IVuYjtF4z21tpQaF70kABtoqKAKZYayM58fb
gy95djVkMPYiCTiTAq05MmaXMCAKUzTr6ZyHWa2qoDHr2O3DeTLm/a26C5u/u5+6
kDVcfE52rXyAWKrlDMiH5PaxAoGBAOoPJP5CDFzZ4tnrPbm3PZ7Z09Yc68RDthKt
GhiNWh0Xo9zoR+/w3ghTY574njxN0UgXPlP+8cg8Ol7zLAXzojXbnizhRJjfwpVN
Vx+sEeXUukvds8IriNwOq1VwVUpW11PWdlqhWRC4DQoWCaVYbAHon0rN2bM66ZFI
79330O75AoGBAJKlkQnZmus+NMEWQvcv53/vnjnpE6N4CMCfIOFGE2Vtq4wfCLoE
LS0QkAJwZp25u2eSFBsbAbTYkC5Ru+q1UY13mce5k9NxOn9itO4JyAq/+CfWib+m
3yK00rvpmwbZIyiRTxga6eGWYfRQmsu0to5jzNTpJr2vX9uZKURn8d6U
-----END RSA PRIVATE KEY-----")

(def server-cert "-----BEGIN CERTIFICATE-----
MIIC0zCCAbugAwIBAgIRALFU9JLP2kEMjIfTBfqsVQAwDQYJKoZIhvcNAQELBQAw
FjEUMBIGA1UEAwwLYXdzaW1hbmRpYXMwHhcNMTYxMTAyMTU1NTIyWhcNMjAxMTAy
MTU1NTIyWjARMQ8wDQYDVQQDDAZzZXJ2ZXIwggEiMA0GCSqGSIb3DQEBAQUAA4IB
DwAwggEKAoIBAQDwvVyVVx5ZxFzezRCSydDooy+RJsyh/TYV2Bx/sNfrjCbMWXZc
rAKul40ekQs9KTSyY8eNXn1UpbIGR4bJeIosu/Bq2K9uvgpyKZbg2bhRi3nh3hjx
FYIDZ4Nyp0uGNN+wZlVBiUyjN+xd1eZ3rz3FRQkfQIM8g8/3u64S4tBHvR4H0S6p
Cu7w30Fd2P7WAFWS4h/zEBCqQrR86fwtaxCZGtUSsRSkXWVIhYw1Pn3qHgHcYOGQ
PKOJkpkxmOckF5rzAEsZE5QPw24lCqH1opb2HsCn3Rw6Tb043liSCgIX7jMH0xF5
DNwkNkoSEpeHOavLgeN8+aWEp63qFA1385RBAgMBAAGjITAfMA8GA1UdEQQIMAaH
BH8AAAEwDAYDVR0TAQH/BAIwADANBgkqhkiG9w0BAQsFAAOCAQEAK25FyZXgXKjG
iiqSvH4PTxVE6ppr6dswEvSsdUaMraiW5/QplBqVQMob7yJcoNioBKPTe2ZMx3y2
ucDFgqOnVOMcBL9BfqFW2rOsTXdma0KeaFemXyiORv/a6eKStfhKRUJ4cIciyTeE
oauIxWh8UzWP7e/J6kpX+C+DQjRAmk2zB5IordwUoBmesDm2ZG5EK3bZOwprafUD
Pu+z5tCOxZ5f80QXZ3NZAc56ioEYi6oHPJ2xQNzyBMXjB8Sl/L2BhwAlN12JjAQV
pwjh7CEGrAuYehiH69PeFdt7H2i/RYUJsbak9OWt+YCZ81UxIHWU4rSjcac6/BgQ
6OvlpHQFug==
-----END CERTIFICATE-----")

(def server-key "-----BEGIN RSA PRIVATE KEY-----
MIIEpAIBAAKCAQEA8L1clVceWcRc3s0QksnQ6KMvkSbMof02Fdgcf7DX64wmzFl2
XKwCrpeNHpELPSk0smPHjV59VKWyBkeGyXiKLLvwativbr4KcimW4Nm4UYt54d4Y
8RWCA2eDcqdLhjTfsGZVQYlMozfsXdXmd689xUUJH0CDPIPP97uuEuLQR70eB9Eu
qQru8N9BXdj+1gBVkuIf8xAQqkK0fOn8LWsQmRrVErEUpF1lSIWMNT596h4B3GDh
kDyjiZKZMZjnJBea8wBLGROUD8NuJQqh9aKW9h7Ap90cOk29ON5YkgoCF+4zB9MR
eQzcJDZKEhKXhzmry4HjfPmlhKet6hQNd/OUQQIDAQABAoIBAQDNlQghSDpU8klU
ws3qbIoIgIK4c+fAVdoAIYOEz+Iz9oWTcLqRPRs1OZp7heWRH0UvKeJZqV1iEfXL
Qv2pw1RMC1quA8kcyxFkmHuOHJ84k+FLrzyhdHqIqbcpDBCE1VZI77rSsotNvw4M
vAOKJY6cje3SUGdCIFZS+mUXH7Y563BZB+oxoaqObk9bsqJYfJzlL8JQC36kHb98
JoPOl38NngorrS/opYKfE6Ms27Z5YOMmvrlRCjRLHWMLnBz5wuNjCWy4IhQs/yiQ
eiEnG8l3sQueGIS9fifTilxlwnb8zf3a+dUapd3dhByfeKYhg+wqBi720g/5uZcM
D+lakZ95AoGBAProdTChusEZyD+rfe0wtKxSzmigskGhFpXXN5ZeLToBFCJjOqIw
D8PDFJk5AhhnEHwAa5MCDqEs8+/cMaaDJaUZm/JfsxBjTsn+JiXZnjm2CC03BLcK
nszjGj/+TZxUmsI8wWoVoFgI/qa5o3jXqj029WV1sRRZR6vUQNw3iAkPAoGBAPWg
E4gBycdlVaWSeTslm1Zk4MoH+PHBfiVtO51q7Frf8iuAKMBj8Hwzw1RXJhYsAjQx
agNK4IZ1dYw6WvalZFQbCYpiPBfWHtvLUqkrOf+uym/W0BgE9Mfo2nIjta3dblfS
otkSOjAzLVhwrkE/azcBAGqcZdtMy9GSpDokRm2vAoGBANkaOQS6fEDH0UieV9jb
kwmkoOtRWMyG2WcQEQ4wMQwGkOhsWfiqg4mNDA7EkDaTP2U8a9iJKCV4Ix7Btchx
pCombZScD/G3LB6FpXaMxWcERjDxcUf8jYmVXEpQ/dCuODanoWfsjJCAP+/9oyBV
4/0Rx+ZhnraPxf89Kl4iQrgrAoGAOw09JNmpNTL7oE7SrpGgAulF0cyQ4S5vsXfN
eU8Xe9GTWzn4kih0zKd1MnWxRmtWLE4k+p2p1SmwFQKClAG3WTEW3ho38FMeICGF
wf4Od5YCPVkbtmqanuUwf3huWVp/CMYvOJjVPHlRUY3llvI0SIkJHJh1NIAGEGB8
8y19cSkCgYBkwgRhRag4KtIOuiZDm2D1y8IwmESyyPzRMrVaYYNul+PDPEXFjOzJ
Gi6DHQh+FDUGmhBwebhvNh701sG4XH5eX9coe3iux+aOx6lZl91BMmjdyFu+78Jj
YLGL9PeRFrs5EH21vYQ5uQ0OB3Esgkg4yNTmc9XSLAMnfR+j2+6Rwg==
-----END RSA PRIVATE KEY-----")

(def client-cert "-----BEGIN CERTIFICATE-----
MIICwjCCAaqgAwIBAgIRAP0IWffHpkT3gai1y1wx/fQwDQYJKoZIhvcNAQELBQAw
FjEUMBIGA1UEAwwLYXdzaW1hbmRpYXMwHhcNMTYxMTAyMTU1NjQ2WhcNMjAxMTAy
MTU1NjQ2WjARMQ8wDQYDVQQDDAZjbGllbnQwggEiMA0GCSqGSIb3DQEBAQUAA4IB
DwAwggEKAoIBAQC8A51nQ7WeikjVajPXRbOUiYYW9o4M0MrRb4OJPUYc+Jo4t9EQ
VE7MnoAXhxkievlKXr+RzYLFsqwiQwPHPHl9O33t1ahQMgPeO/e1uBgcWXJzS3ZT
JgL8jtlr5+1DIdCIimoxkH0xnlIvfvOMdClJZdPpfIZdcX04bXD+0Vq7T0Qh4XNw
kQ6dNI3QaACBMaBxJZs2IFPdYoMn8yh6O8G+24t2E5rH8ILL5XMfoQ+iM0b8mGDl
DTz9EUr8JPNjWo5S4A9orSl+taEDmsKdKOW8FwgXJc/dxF90QNwo5RXzgdC2tbtP
eMycuNEydHX9RGbJNM+nP0MOGXJVN4mi1WoLAgMBAAGjEDAOMAwGA1UdEwEB/wQC
MAAwDQYJKoZIhvcNAQELBQADggEBANIngG0FgB4yMemHW1IoSobODhi7Zk9Xitd2
+VzltFSvXg4Fe6RC0T19a0oEajoDk4w6AzknL4LOzExvwZL2LsQWVRBpFPDtmXkz
m4R2M47YbIqsO23ZERmCZEyQ6ofmZs4wSTpwRP0cefq57xZEkMLV4wGDAad5AUMe
QbqCOgzfbyogTX7hYw4tNZfCFnIIxK+Md4XltRDjPWp1NfsgKhB1LgTPvCMz4Lg4
Cksd1XYPYr8rd+BhAvjOHL9KO6cSJEr3NYM2bbacRD6U1Qp3eGuxjIwEZ5ePmq4K
dy6XnN2mvlfID9dIkwObN2Zz+RZeOmnaOm75JxQpanBfWMiYeWQ=
-----END CERTIFICATE-----")

(def client-key "-----BEGIN RSA PRIVATE KEY-----
MIIEpQIBAAKCAQEAvAOdZ0O1nopI1Woz10WzlImGFvaODNDK0W+DiT1GHPiaOLfR
EFROzJ6AF4cZInr5Sl6/kc2CxbKsIkMDxzx5fTt97dWoUDID3jv3tbgYHFlyc0t2
UyYC/I7Za+ftQyHQiIpqMZB9MZ5SL37zjHQpSWXT6XyGXXF9OG1w/tFau09EIeFz
cJEOnTSN0GgAgTGgcSWbNiBT3WKDJ/MoejvBvtuLdhOax/CCy+VzH6EPojNG/Jhg
5Q08/RFK/CTzY1qOUuAPaK0pfrWhA5rCnSjlvBcIFyXP3cRfdEDcKOUV84HQtrW7
T3jMnLjRMnR1/URmyTTPpz9DDhlyVTeJotVqCwIDAQABAoIBAQCpWvQ45VO+qD9t
BYoG2kElhi8jlhlkT9qxhqpMP8ZdKE360xDdjaOhSxCEoOuyFGVjaS9UTXMDU6sb
/ZN0rvOcK6+GQZOoETVMxGWf6WDGgIfIdOvzf+TmzGHqgfJFKztlEpEPbmBnBn5v
TAJjvOrHs5KL2eBKYYTzBBnpXnlFCFikwNRS5G71i5/BVpD9Xv+iPX/LT42xPbSB
ZFJ+zw2/76jCwm8yprLGIFUagpbKDXNdDyqDfLRMn0YGLzngxvliO4e76TGRmmPz
Zwc1iL4KbzkSelukqyzIcDrUzEmqDhPnrNVyVRAbp/aWXF5hLqAo2WY69+wpIBtZ
O5+wnjHxAoGBAOiNO5Qqa0afsGbilkYVNAYnU1jFWSj4VbjCrf1kg5CWVnrVDu/t
L6uVmBy/esJjCaBttTEtquVuTlLLSqkMGn9nqKAEZ2+EINtmAcygkx+iEpfAsqEa
Nb2ZVt+3ZL6syvI4YyvCrfimVOT8931O8lt/fIkXUyJXE7uU4yq7q3VjAoGBAM74
wEBItQtb2wH23kW/PVUs9OU9dSnKMAa3eGsQ2xg0rX75pvwklWiAJQCIu4IlRtjL
gp4Q3jmy9NQUDXLQW6AE5eQBD5hx8wvZ9UiWQ6HybYHPnIo2jgXueeXfGCPegybI
8VoYyqBoBnFbcSEgKNChL45/7+m9fKnhAFZVOM05AoGBALZiZfKrXh0yir/2P5NR
d87HsudxR58JPPm9vYV4+nJAZizJwoBLTlLM7VLGnfmsgNVQlYWvYlO0Mjte8vwn
4PRBERAGxDTCtJ49n3WkDh8GFnzsBWyySKmdhHPKunmMvVJBBQ5CDf1wPi6lc1jU
vOEj2EEQiQPqtTAt8ggGzc8vAoGBAJq8pNcqxJgia4FzdMhGau9UNh9xLDg7Hl7X
KmM5beQR9ig5M9vM6hr/uypED5QElIwe0p+kUtBmd95aw5c0KVxgbRNiq6s4fhI9
OMG0/gmEutRTSGCY2uOaYG44tc6TXBBSKQdIA62bOD3ltJWken9IEssB4XcqIJrN
SjhBbL8ZAoGAXSV9Y5SVj771gB9Rjfrim85J3+pg2X8mRV3plUjAi1FkQAYgUTyk
0bp3GJeqtrOfOoq3eT9g/CRDjqp+oXQ+ZJlNnVtgIR9OW8fQ4cfNPM5CI6iIni/D
Q1Rmq3XOYV/kp56kQOP5bymRFh3p7EghGL1Yfarcygs58HdaTwm4Z5c=
-----END RSA PRIVATE KEY-----")

;; used goodssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in client.key.1  -out pkcs8.key
;; to convert from pkcs1 to 8

(def pkcs8-server-key "-----BEGIN PRIVATE KEY-----
MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDwvVyVVx5ZxFze
zRCSydDooy+RJsyh/TYV2Bx/sNfrjCbMWXZcrAKul40ekQs9KTSyY8eNXn1UpbIG
R4bJeIosu/Bq2K9uvgpyKZbg2bhRi3nh3hjxFYIDZ4Nyp0uGNN+wZlVBiUyjN+xd
1eZ3rz3FRQkfQIM8g8/3u64S4tBHvR4H0S6pCu7w30Fd2P7WAFWS4h/zEBCqQrR8
6fwtaxCZGtUSsRSkXWVIhYw1Pn3qHgHcYOGQPKOJkpkxmOckF5rzAEsZE5QPw24l
CqH1opb2HsCn3Rw6Tb043liSCgIX7jMH0xF5DNwkNkoSEpeHOavLgeN8+aWEp63q
FA1385RBAgMBAAECggEBAM2VCCFIOlTySVTCzepsigiAgrhz58BV2gAhg4TP4jP2
hZNwupE9GzU5mnuF5ZEfRS8p4lmpXWIR9ctC/anDVEwLWq4DyRzLEWSYe44cnziT
4UuvPKF0eoiptykMEITVVkjvutKyi02/Dgy8A4oljpyN7dJQZ0IgVlL6ZRcftjnr
cFkH6jGhqo5uT1uyolh8nOUvwlALfqQdv3wmg86Xfw2eCiutL+ilgp8Toyzbtnlg
4ya+uVEKNEsdYwucHPnC42MJbLgiFCz/KJB6IScbyXexC54YhL1+J9OKXGXCdvzN
/dr51Rql3d2EHJ94piGD7CoGLvbSD/m5lwwP6VqRn3kCgYEA+uh1MKG6wRnIP6t9
7TC0rFLOaKCyQaEWldc3ll4tOgEUImM6ojAPw8MUmTkCGGcQfABrkwIOoSzz79wx
poMlpRmb8l+zEGNOyf4mJdmeObYILTcEtwqezOMaP/5NnFSawjzBahWgWAj+prmj
eNeqPTb1ZXWxFFlHq9RA3DeICQ8CgYEA9aATiAHJx2VVpZJ5OyWbVmTgygf48cF+
JW07nWrsWt/yK4AowGPwfDPDVFcmFiwCNDFqA0rghnV1jDpa9qVkVBsJimI8F9Ye
28tSqSs5/67Kb9bQGAT0x+jaciO1rd1uV9Ki2RI6MDMtWHCuQT9rNwEAapxl20zL
0ZKkOiRGba8CgYEA2Ro5BLp8QMfRSJ5X2NuTCaSg61FYzIbZZxARDjAxDAaQ6GxZ
+KqDiY0MDsSQNpM/ZTxr2IkoJXgjHsG1yHGkKiZtlJwP8bcsHoWldozFZwRGMPFx
R/yNiZVcSlD90K44NqehZ+yMkIA/7/2jIFXj/RHH5mGeto/F/z0qXiJCuCsCgYA7
DT0k2ak1MvugTtKukaAC6UXRzJDhLm+xd815Txd70ZNbOfiSKHTMp3UydbFGa1Ys
TiT6nanVKbAVAoKUAbdZMRbeGjfwUx4gIYXB/g53lgI9WRu2apqe5TB/eG5ZWn8I
xi84mNU8eVFRjeWW8jRIiQkcmHU0gAYQYHzzLX1xKQKBgGTCBGFFqDgq0g66JkOb
YPXLwjCYRLLI/NEytVphg26X48M8RcWM7MkaLoMdCH4UNQaaEHB5uG82HvTWwbhc
fl5f1yh7eK7H5o7HqVmX3UEyaN3IW77vwmNgsYv095EWuzkQfbW9hDm5DQ4HcSyC
SDjI1OZz1dIsAyd9H6Pb7pHC
-----END PRIVATE KEY-----")

(def pkcs8-client-key "-----BEGIN PRIVATE KEY-----
MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQC8A51nQ7WeikjV
ajPXRbOUiYYW9o4M0MrRb4OJPUYc+Jo4t9EQVE7MnoAXhxkievlKXr+RzYLFsqwi
QwPHPHl9O33t1ahQMgPeO/e1uBgcWXJzS3ZTJgL8jtlr5+1DIdCIimoxkH0xnlIv
fvOMdClJZdPpfIZdcX04bXD+0Vq7T0Qh4XNwkQ6dNI3QaACBMaBxJZs2IFPdYoMn
8yh6O8G+24t2E5rH8ILL5XMfoQ+iM0b8mGDlDTz9EUr8JPNjWo5S4A9orSl+taED
msKdKOW8FwgXJc/dxF90QNwo5RXzgdC2tbtPeMycuNEydHX9RGbJNM+nP0MOGXJV
N4mi1WoLAgMBAAECggEBAKla9DjlU76oP20FigbaQSWGLyOWGWRP2rGGqkw/xl0o
TfrTEN2No6FLEISg67IUZWNpL1RNcwNTqxv9k3Su85wrr4ZBk6gRNUzEZZ/pYMaA
h8h06/N/5ObMYeqB8kUrO2USkQ9uYGcGfm9MAmO86sezkovZ4EphhPMEGeleeUUI
WKTA1FLkbvWLn8FWkP1e/6I9f8tPjbE9tIFkUn7PDb/vqMLCbzKmssYgVRqClsoN
c10PKoN8tEyfRgYvOeDG+WI7h7vpMZGaY/NnBzWIvgpvORJ6W6SrLMhwOtTMSaoO
E+es1XJVEBun9pZcXmEuoCjZZjr37CkgG1k7n7CeMfECgYEA6I07lCprRp+wZuKW
RhU0BidTWMVZKPhVuMKt/WSDkJZWetUO7+0vq5WYHL96wmMJoG21MS2q5W5OUstK
qQwaf2eooARnb4Qg22YBzKCTH6ISl8CyoRo1vZlW37dkvqzK8jhjK8Kt+KZU5Pz3
fU7yW398iRdTIlcTu5TjKrurdWMCgYEAzvjAQEi1C1vbAfbeRb89VSz05T11Kcow
Brd4axDbGDStfvmm/CSVaIAlAIi7giVG2MuCnhDeObL01BQNctBboATl5AEPmHHz
C9n1SJZDofJtgc+cijaOBe555d8YI96DJsjxWhjKoGgGcVtxISAo0KEvjn/v6b18
qeEAVlU4zTkCgYEAtmJl8qteHTKKv/Y/k1F3zsey53FHnwk8+b29hXj6ckBmLMnC
gEtOUsztUsad+ayA1VCVha9iU7QyO17y/Cfg9EEREAbENMK0nj2fdaQOHwYWfOwF
bLJIqZ2Ec8q6eYy9UkEFDkIN/XA+LqVzWNS84SPYQRCJA+q1MC3yCAbNzy8CgYEA
mryk1yrEmCJrgXN0yEZq71Q2H3EsODseXtcqYzlt5BH2KDkz28zqGv+7KkQPlASU
jB7Sn6RS0GZ33lrDlzQpXGBtE2Krqzh+Ej04wbT+CYS61FNIYJja45pgbji1zpNc
EFIpB0gDrZs4PeW0laR6f0gSywHhdyogms1KOEFsvxkCgYBdJX1jlJWPvvWAH1GN
+uKbzknf6mDZfyZFXemVSMCLUWRABiBRPKTRuncYl6q2s586ird5P2D8JEOOqn6h
dD5kmU2dW2AhH05bx9Dhx808zkIjqIieL8NDVGardc5hX+SnnqRA4/lvKZEWHens
SCEYvVh9qtzKCznwd1pPCbhnlw==
-----END PRIVATE KEY-----")

(def ^:dynamic ^io.aleph.dirigiste.IPool *pool* nil)

(netty/leak-detector-level! :paranoid)

(def port 8888)
(def string-response "dawoon likes to play")

(defn string-handler [request]
  {:status 200
   :body string-response})

(defmacro with-server [server & body]
  `(let [server# ~server]
     (binding [*pool* (http/connection-pool {:connection-options {:insecure? false}})]
       (try
         ~@body
         (finally
           (.close ^java.io.Closeable server#)
           (.shutdown *pool*)
           (netty/wait-for-close server#))))))

#_(deftest mutual-auth-context-integration
    (testing "a client and server can talk with valid certs, keys, and a ca"
      (let [client-args {:cert client-cert
                         :pkey pkcs8-client-key
                         :authority ca-cert}
            client-ctx (server/client-context client-args)

            server-args {:cert server-cert
                         :pkey pkcs8-server-key
                         :authority ca-cert}
            server-ctx (server/server-context server-args)

            client-pool
            (http/connection-pool {:ssl-context client-ctx :insecure? false})]
        (with-server (http/start-server string-handler {:port port :ssl-context server-ctx})
          (is (= string-response
                 @(http/get (str "https://127.0.0.1:" port) {:pool client-pool})))))))
