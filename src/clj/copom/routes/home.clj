(ns copom.routes.home
  (:require
    [copom.layout :as layout]
    [copom.db.core :as db]
    [copom.db.queries.common :as q]
    [clojure.java.io :as io]
    [copom.middleware :as middleware]
    [ring.util.http-response :as response]))

(defn home-page [request]
  (layout/render request "home.html"))

; -----------------------------------------------------------------------------
; Requests

(defn get-requests [req]
  (response/ok
    (q/all {:table "requests"})))

; CREATE
(defn create-requests [req])  

; READ
(defn get-request [req]
  (response/ok
    (q/by-id {:table "requests" :id (get-in req [:params :id])})))

; UPDATE
(defn update-request [req])

; DELETE
(defn delete-request [req])

; -----------------------------------------------------------------------------
; Routes


(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/docs" {:get (fn [_]
                    (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                        (response/header "Content-Type" "text/plain; charset=utf-8")))}]
   ["/api"
    ["/requests" {:get get-requests}]]])

