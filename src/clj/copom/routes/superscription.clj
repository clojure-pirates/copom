(ns copom.routes.superscription
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.string :as string]
    [copom.db.core :as db]
    [copom.db.queries.columns :as c]
    [copom.db.queries.common :as q]
    [copom.utils :refer [m->upper-case]]
    [ring.util.http-response :as response]))


(defn min-superscription-params? [params]
  (and (:neighborhood/id params)
       (:route/id params)
       (:superscription/city params)
       (:superscription/state params)))

(defn create-route! [params]
  (q/create! {:table "route"
              :params (m->upper-case params)}))


(defn create-neighborhood! [params]
  (q/create! {:table "neighborhood"
              :params (m->upper-case params)}))

(defn create-sup! [params]
  (when (min-superscription-params? params)
    (let [sup-params (-> params
                         (dissoc :neighborhood/id :route/id)
                         m->upper-case)
          sid (q/create! {:table "superscription" :params sup-params})]
      (q/create! {:table "superscription_neighborhood"
                  :params {:superscription-id sid
                           :neighborhood-id (:neighborhood/id params)}})
      (q/create! {:table "superscription_route"
                  :params {:superscription-id sid
                           :route-id (:route/id params)}})
      sid)))

; -----------------------------------------------------------------------------
; Neighborhood Handlers

(defn get-neighborhoods [{{query :query} :params}]
  (response/ok
    (db/parser
      [{(list :neighborhoods/all
              {:filters (when query 
                          [:like :neighborhood/name (str "%" query "%")])
               :limit 10})
        c/neighborhood-columns}])))

(defn create-neighborhood [{:keys [params]}]
  (create-neighborhood! params)
  (response/ok {:result :ok}))

; -----------------------------------------------------------------------------
; Route Handlers

(defn get-routes [{{query :query} :params}]
  (response/ok
    (db/parser
      [{(list :routes/all
              {:filters (when query [:like :route/name (str "%" query "%")])
               :limit 10})
        c/route-columns}])))

(defn create-route [{:keys [params]}]
  (create-route! params)
  (response/ok {:result :ok}))

(defn create-superscription [{:keys [params]}]
  (if-let [sid (create-sup! params)]
    (response/ok
      {:superscription/id sid})
    (response/bad-request "Missing required params.")))