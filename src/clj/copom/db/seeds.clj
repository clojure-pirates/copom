(ns copom.db.seeds
  (:require
    [user :as u]
    [clojure.string :as string]
    [copom.db.core :as db]
    [copom.db.queries :as q]
    [copom.db.queries.common :as c]
    [copom.routes.entity :as ent]
    [copom.routes.requests :as req]
    [copom.routes.superscription :as sup]))

(defn create-admin! []
  (let [params {:first-name "admin" 
                :last-name "admin" 
                :email "ciaguaranta@pm.mt.gov.br"}]
    (c/create! {:table "appuser"
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

;;; create-requests! helpers

(defn- get-delicts []
  (->> (c/all {:table "delict"})
       (take 2)
       (map :delict/id)))

(defn create-entity-returning! [params]
  (-> {:params params}
      ent/create-entity
      :body
      :entity/id))

(defn create-requests! []
  (let [delicts (get-delicts)
        ;; minimum entity params, excluding the superscription.
        requester #:entity{:name "Efraim Augusto"
                           :phone "66999381813"}
        ; Create entity
        requester-id (create-entity-returning! requester)
        victim #:entity{:name "John Doe"
                        :phone "190"
                        :role "victim"
                        :doc-type "CPF"
                        :doc-issuer "República Federativa do Brasil"
                        :doc-number "xxx.xxx.xxx-xx"
                        :father "John Doe Senior"
                        :mother "Mary Doe"}
        ;; Create entity
        vid (-> {:params victim} ent/create-entity :body :entity/id)
        victim-address #:superscription{:num "123"
                                        :complement "Ap. 1"
                                        :reference "Ao lado do Bar X"
                                        :city "Guarantã do Norte"
                                        :state "Mato Grosso"
                                        :route/id (sup/create-route!
                                                    #:route{:type "Rua"
                                                            :name "2"})
                                        :neighborhood/id 
                                        (sup/create-neighborhood!
                                         {:neighborhood/name "Jardim Vitória"})}  
        ;; Create superscription; create entity-superscription
        victim-sid (-> {:params victim-address
                        :path-params {:entity/id vid}}
                       ent/create-entity-superscription
                       :body
                       :superscription/id)
        ;; minimum superscription params
        req-address #:superscription{:neighborhood/id 
                                     (sup/create-neighborhood! 
                                       #:neighborhood{:name "Centro"})
                                     :route/id 
                                     (sup/create-route!
                                       #:route{:type "Rua"
                                               :name "1"})
                                     :city "Guarantã do Norte"
                                     :state "Mato Grosso"}
        ; Create superscription
        rsid (sup/create-sup! req-address)
        req #:request{:complaint "test"
                      :summary "test request with an entity, delicts, and an address"
                      :status "pending"
                      :delicts delicts
                      :superscription {:superscription/id rsid}
                      :requester {:entity/role "requester"
                                  :entity/id requester-id}
                      :victim {:entity/role "victim"
                               :entity/id vid
                               :entity/superscription 
                               {:superscription/id victim-sid}}}]
    (req/create-request {:params req})))

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
  
  (reset-db!)

  (def requester
    #:entity{:name "Efraim Augusto"
             :phone "66999381813"}))