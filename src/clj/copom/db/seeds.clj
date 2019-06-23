(ns copom.db.seeds
  (:require
    [copom.db.queries :as q]
    [copom.db.queries.common :as c]))

(defn create-admin! []
  (let [params {:first-name "admin" 
                :last-name "admin" 
                :email "ciaguaranta@pm.mt.gov.br"}]
    (c/create! {:table "users"
                :params params})))

; routes
; - type
; - name

; neighborhoods
; - name

; delicts
; - name
; - weight
(defn create-delicts! []
  (let [items {"apoio a policial" 4
               "ofensa à vida" 5
               "ofensa à integridade física" 2
               "ofensa à honra" 1
               "arma de fogo" 4
               "tráfico de drogas" 3
               "uso/porte de drogas" 2}]
    (doseq [[d w] items]
      (c/create! {:table "delicts"
                  :params {:name d
                           :weight w}}))))


(comment
  (c/all {:table "users"})
  (c/all {:table "delicts"})
  (c/all {:table "superscriptions"})
  (create-admin!)
  (create-delicts!))