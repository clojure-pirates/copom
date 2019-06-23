(ns copom.db.queries.requests
  (:require
    [copom.db.queries :as q]))

(defprotocol Request
  (create! [m]))

(extend-protocol Request
  clojure.lang.PersistentArrayMap
  (create! [m]
   (q/insert! (:table m) (:params m))))