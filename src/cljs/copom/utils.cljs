(ns copom.utils)

(defn to-iso-date [d]
  (-> d
      .toISOString
      (.split "T")
      first))

(defn to-time-string [d]
  (-> d
      .toTimeString
      (.split " ")
      first))

(defn request-status [r]
  (let [m {"PENDING"    "PENDENTE"
           "DONE"       "FINALIZADO"
           "DISPATCHED" "DESPACHADO"}]
    (get m (:request/status r))))
           