(ns copom.db.seeds
  (:require
    [user :as u]
    [clojure.string :as string]
    [copom.db.core :as db]
    [copom.db.queries :as q]
    [copom.db.queries.common :as c]
    [copom.routes.requests :as requests]))

(defn create-admin! []
  (let [params {:first-name "admin" 
                :last-name "admin" 
                :email "ciaguaranta@pm.mt.gov.br"}]
    (c/create! {:table "user"
                :params params})))

(defn create-delicts! []
  (let [items {"apoio a policial" 4
               "ofensa à vida" 5
               "ofensa à integridade física" 2
               "ofensa à honra" 1
               "arma de fogo" 4
               "tráfico de drogas" 3
               "uso/porte de drogas" 2}]
    (doseq [[d w] items]
      (c/create! {:table "delict"
                  :params {:name (string/upper-case d)
                           :weight w}}))))

(defn create-request-roles! []
  (let [roles [{:request-role/role "requester"}
               {:request-role/role "suspect"}
               {:request-role/role "witness"}
               {:request-role/role "victim"}]]
    (doseq [r roles]
      (c/create! {:table "request_role"
                  :params r}))))

(defn create-requests! []
  (let [delicts (->> (c/all {:table "delict"})
                     (take 2)
                     (map :delict/id))
        ;; minimum entity params, excluding the superscription.
        requester #:entity{:name "Efraim Augusto"
                           :phone "66999381813"
                           :role "requester"}
        ;; full superscription params
        victim-address #:superscription{:num "123"
                                        :complement "Ap. 1"
                                        :reference "Ao lado do Bar X"
                                        :city "Guarantã do Norte"
                                        :state "Mato Grosso"
                                        :route #:route{:type "Rua"
                                                       :name "2"}
                                        :neighborhood #:neighborhood{:name "Jardim Vitória"}}  
        ;; full entity params
        victim #:entity{:name "John Doe"
                        :phone "190"
                        :role "victim"
                        :doc-type "CPF"
                        :doc-issuer "República Federativa do Brasil"
                        :doc-number "xxx.xxx.xxx-xx"
                        :father "John Doe Senior"
                        :mother "Mary Doe"
                        :superscription victim-address}
        ;; minimum superscription params
        req-address #:superscription{:neighborhood #:neighborhood{:name "Centro"}
                                     :route #:route{:type "Rua"
                                                    :name "1"}
                                     :city "Guarantã do Norte"
                                     :state "Mato Grosso"}
        req #:request{:complaint "test"
                      :summary "test request with an entity, delicts, and an address"
                      :status "pending"
                      :delicts delicts
                      :superscription req-address
                      :requester requester
                      :victim victim}]
    (requests/create-request {:params req})))

(defn reset-db! []
  (u/reset-db)
  (create-admin!)
  (create-delicts!)
  (create-request-roles!)
  (create-requests!))

(comment
  
  (c/create! {:table "request" :params {:request/event-timestamp "2019-07-11T15:47:05.000>"}})
  (c/get-or-create! {:table "route" :params {:route/type "Rua" :route/name "1"}})
  (c/all {:table "neighborhood"})
  (c/all {:table "delict"})
  (c/all {:table "request_role"})
  (db/parser [{[:requests/all]
               [:request/id :request/event-timestamp]}])
  
  (reset-db!))