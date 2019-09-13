(ns copom.db.walkable
  (:require
    [clojure.java.jdbc :as jdbc]
    [com.wsscode.pathom.core :as p]
    [copom.db.queries.columns :as c]
    [walkable.sql-query-builder :as sqb]
    [walkable.sql-query-builder.emitter :as emitter]
    [walkable.sql-query-builder.floor-plan :as floor-plan]
    [walkable.sql-query-builder.expressions :as sqb-exp]))

(defmethod sqb-exp/operator? :is [_operator] true)

(defmethod sqb-exp/process-operator :is
  [_env [_operator params]]
  (sqb-exp/multiple-compararison "IS" params))

(defmethod sqb-exp/operator? :is-not [_operator] true)

(defmethod sqb-exp/process-operator :is-not
  [_env [_operator params]]
  (sqb-exp/multiple-compararison "IS NOT" params))

(def schema
  {:idents {:appuser/by-id :appuser/id
            :appusers/all "appuser"
            :neighborhood/by-id :neighborhood/id
            :neighborhood/by-name :neighborhood/name
            :neighborhoods/all "neighborhood"
            :route/by-id :route/id
            :route/by-name :route/name
            :routes/all "route"
            :superscription/by-id :superscription/id
            :superscriptions/all "superscription"
            :delict/by-id :delict/id
            :delict/by-name :delict/name
            :delicts/all "delicts"
            :entity/by-id :entity/id
            :entity/by-phone :entity/phone
            :entity/by-name :entity/name
            :entities/all "entity" 
            :request/by-id :request/id
            :requests/all "request"
            :request-roles/all "request_role"
            :request-role/by-id :request-role/id
            :request-role/by-role :request-role/role
            :request-entity/by-request-id :request-entity/request-id}
   
   :joins {:superscription/neighborhood 
           [:superscription/id :superscription-neighborhood/superscription_id
            :superscription-neighborhood/neighborhood-id :neighborhood/id]
           
           :superscription/route 
           [:superscription/id :superscription-route/superscription_id
            :superscription-route/route-id :route/id]
           
           :entity/superscription 
           [:request-entity/id :request-entity-superscription/request-entity-id,
            :request-entity-superscription/superscription-id :superscription/id]
           
           :entity/superscriptions
           [:entity/id :entity-superscription/entity-id
            :entity-superscription/superscription-id :superscription/id]
            
           :entity/role 
           [:request-entity/id :request-entity-role/request-entity-id
            :request-entity-role/role-id :request-role/id]
           
           :request/superscription 
           [:request/id :request-superscription/request-id
            :request-superscription/superscription-id :superscription/id]
           
           :request/delicts [:request/id :request-delict/request-id
                             :request-delict/delict-id :delict/id]
           
           :request/entities [:request/id :request-entity/request-id
                              :request-entity/entity-id :entity/id]}
   
   :reversed-joins {}
   
   :true-columns (->> [c/user-columns c/neighborhood-columns c/route-columns
                       c/superscription-columns c/delict-columns c/entity-columns
                       c/request-columns c/request-role-columns
                       c/request-entity-columns c/request-delict-columns]
                      flatten (into #{}))
   
   :cardinality {:appuser/by-id :one
                 :neighborhood/by-id :one
                 :route/by-id :one
                 :superscription/by-id :one
                 :delict/by-id :one
                 :entity/by-id :one
                 :request/by-id :one
                 :request-role/by-id :one
                 :request-role/by-role :one
                 :superscription/neighborhood :one
                 :superscription/route :one
                 :entity/superscription :one
                 :entity/role :one
                 :request/superscription :one}
   
   :emitter emitter/postgres-emitter})

(def compiled-schema
  (floor-plan/compile-floor-plan
    schema))

(def pathom-parser
  (p/parser
    {::p/plugins
     [(p/env-plugin
        {::p/reader
         ;; walkable's main worker
         [sqb/pull-entities 
          ;; pathom's entity reader
          p/map-reader]})]}))