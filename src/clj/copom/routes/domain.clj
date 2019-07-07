(ns copom.routes.domain
  (:require
   [clojure.spec.alpha :as s]
   [spec-tools.core :as st]
   [spec-tools.spec :as spec]
   [clj-time.core :as t]
   [clj-time.format :as tf]))

#_
(defn parse-date [format s]
  (let [formats {:date-time :date-time}]
    (tf/parse (tf/formatters (get formats format))
              s)))
#_
(def timestamp
  (st/spec
   {:spec #(or (instance? org.joda.time.DateTime %) (string? %))
    :json-schema/default "2017-10-12T05:04:57.585Z"
    :type :timestamp}))
#_
(defn json-timestamp->joda-datetime [_ val]
  (parse-date :date-time val))

#_
(def custom-coercion
  (-> compojure.api.coercion.spec/default-options
      (assoc-in
       [:body :formats "application/transit+json"]
       (st/type-conforming
        (merge
         conform/json-type-conforming
         {:timestamp json-timestamp->joda-datetime}
         conform/strip-extra-keys-type-conforming)))
      compojure.api.coercion.spec/create-coercion))

(s/def ::id spec/int?)
(s/def ::date string?)
(s/def ::status (st/spec #{"pending", "dispatched", "done"}))

; ------------------------------------------------------------------------------
; Neighborhood

(s/def :neighborhood/id ::id)
(s/def :neighborhood/name string?)
(s/def :neighborhood/created-at ::date)
(s/def :neighborhood/updated-at ::date)

(s/def :neighborhoods/neighborhood
       (s/keys :req [:neighborhood/id
                     :neighborhood/name
                     :neighborhood/created-at
                     :neighborhood/updated-at]))

(s/def :neighborhoods.new/neighborhood
       (s/keys :req [:neighborhood/name]))

(s/def :neighborhoods/neighborhoods (s/* :neighborhoods/neighborhood))

; ------------------------------------------------------------------------------
; Route

(s/def :route/id ::id)
(s/def :route/name string?)
(s/def :route/type (st/spec #{"Rua", "Avenida", "Rodovia", "Linha", "Travessa", "Praça"}))
(s/def :route/created-at ::date)
(s/def :route/updated-at ::date)

(s/def :routes/route
       (s/keys :req [:route/id
                     :route/name
                     :route/type
                     :route/created-at
                     :route/updated-at]))

(s/def :routes.new/route
       (s/keys :req [:route/name
                     :route/type]))

(s/def :routes/routes
       (s/* :routes/route))

; ------------------------------------------------------------------------------
; Superscriptions

(s/def :superscription/id ::id)
(s/def :superscription/neighborhood-id int?)
(s/def :superscription/route-id int?)
(s/def :superscription/num string?)
(s/def :superscription/complement string?)
(s/def :superscription/reference string?)
(s/def :superscription/city string?)
(s/def :superscription/state string?)
(s/def :superscription/created-at ::date)
(s/def :superscription/updated-at ::date)

(s/def :superscriptions/superscription
       (s/keys :req [:superscription/id
                     :superscription/neighborhood-id
                     :superscription/route-id
                     :superscription/num
                     :superscription/complement
                     :superscription/reference
                     :superscription/city
                     :superscription/state
                     :superscription/created-at
                     :superscription/updated-at]))

(s/def :superscriptions.new/superscription
       (s/keys :opt [:neighborhood/id
                     :neighborhoods.new/neighborhood
                     :route/id
                     :routes.new/route
                     :superscription/num
                     :superscription/complement
                     :superscription/reference
                     :superscription/city
                     :superscription/state]))

(s/def :superscriptions/superscriptions
       (s/* :superscriptions/superscription))
                         
; ------------------------------------------------------------------------------
; Entity

(s/def :entity/id ::id)
(s/def :entity/role (st/spec #{"requester" "suspect" "witness" "victim"}))
(s/def :entity/name string?)
(s/def :entity/doc-type (st/spec #{"CNH", "CNPJ" "CPF" "RG" "Passaporte"}))
(s/def :entity/doc-issuer string?)
(s/def :entity/doc-number string?)
(s/def :entity/father string?)
(s/def :entity/mother string?)
(s/def :entity/address-id int?)
(s/def :entity/phone string?)
(s/def :entity/created-at ::date)
(s/def :entity/updated-at ::date)

(s/def :entities/entity
       (s/keys :req [:entity/id
                     :entity/role
                     :entity/name
                     :entity/doc-type
                     :entity/doc-issuer
                     :entity/doc-number
                     :entity/father
                     :entity/mother
                     :entity/address-id
                     :entity/phone
                     :entity/created-at
                     :entity/updated-at]))

(s/def :entities.new/entity
       (s/keys :req [:entity/role]
               :opt [:entity/id
                     :entity/name
                     :entity/doc-type
                     :entity/doc-issuer
                     :entity/doc-number
                     :entity/father
                     :entity/mother
                     :superscription/id
                     :superscriptions.new/superscription
                     :entity/phone]))

(s/def :entities.new/entities
       (s/* :entities.new/entity))

; ------------------------------------------------------------------------------
; Request

(s/def :request/id ::id)
(s/def :request/complaint string?)
(s/def :request/summary string?)
(s/def :request/event-timestamp ::date)
(s/def :request/status ::status)
(s/def :request/measures string?)
(s/def :request/address-id int?)
(s/def :request/created-at ::date)
(s/def :request/updated-at ::date)

(s/def :requests/request 
       (s/keys :req [:request/id
                     :request/complaint
                     :request/summary
                     :request/event-timestamp
                     :request/status
                     :request/measures
                     :request/address-id
                     :superscriptions.new/superscription
                     :request/created-at
                     :request/updated-at]))

(s/def :requests.new/request
       (s/keys :req [:request/status]
               :opt [:request/complaint
                     :request/summary
                     :request/event-timestamp
                     :request/measures
                     :superscriptions.new/superscription
                     :entities.new/entities]))

(s/def :requests/requests
       (s/* :requests/request))