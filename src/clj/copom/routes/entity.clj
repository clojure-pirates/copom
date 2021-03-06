(ns copom.routes.entity
  (:require
    [clojure.string :refer [upper-case]]
    [clojure.java.jdbc :as jdbc]
    [copom.db.core :as db]
    [copom.db.queries.columns :as c]
    [copom.db.queries.common :as q]
    [copom.routes.superscription 
     :refer [min-superscription-params? create-sup!]]
    [copom.utils :refer [m->upper-case]]
    [ring.util.http-response :as response]))

(defn create-ent-sup! [eid sid]
  (q/create! {:table "entity_superscription"
              :params {:entity-id eid
                       :superscription-id sid}}))

(defn min-entity-params? 
  [{:entity/keys [name phone]}]
  (or name phone)) 


(defn create-entity! [params]
  (when (min-entity-params? params)
    (let [;; Document fields will only be created if all the fields are filled.
          entity-doc-keys 
          (fn [{:entity/keys [doc-type doc-issuer doc-number]}]
            (when (and doc-type doc-issuer doc-number)
              [:entity/doc-type :entity/doc-issuer :entity/doc-number]))
          entity-keys (into (mapv (partial keyword "entity") 
                                  ["name" "father" "mother" "phone"])
                            (entity-doc-keys params))
          entity-params (-> params 
                            (select-keys entity-keys)
                            (m->upper-case [:entity/name :entity/doc-type 
                                            :entity/doc-issuer :entity/doc-number
                                            :entity/father :entity/mother]))
          eid (q/create! {:table "entity" :params entity-params})]
      eid)))

; -----------------------------------------------------------------------------
; Entity Handlers

(def cols
  (conj c/entity-columns
        {:entity/superscriptions c/superscription-query}))

(defn get-names [{{query :query} :params}]
  (response/ok
    (db/parser
      [{(list :entities/all
              {:filters (when query 
                          [:like :entity/name (str "%" (upper-case query) "%")])})
        cols}])))
(def query "E")

         
(defn get-phones [{{query :query} :params}]
  (response/ok
    (db/parser
      [{(list :entities/all
              {:filters (when query 
                          [:like :entity/phone (str "%" query "%")])})
        cols}])))

(defn create-entity [{:keys [params]}]
  (response/ok
    {:entity/id (create-entity! params)}))

(defn create-entity-superscription
  [{params :params
    {{eid :entity/id sid :superscription/id} :path} :parameters}]
  (jdbc/with-db-transaction [conn db/*db*]
   (binding [db/*db* conn]
     (let [sid (or sid (create-sup! params))]
       (create-ent-sup! eid sid)
       (response/ok 
         {:superscription/id sid})))))

(defn delete-entity-superscription 
  [{{{eid :entity/id sid :superscription/id} :path} :parameters}]
  (q/delete! {:table "entity_superscription"
              :where ["entity_id = ? AND superscription_id = ?" eid sid]}))
