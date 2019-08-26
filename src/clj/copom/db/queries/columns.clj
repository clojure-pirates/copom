(ns copom.db.queries.columns)

(def user-columns
  [:user/id :user/first-name :user/last-name :user/email])

(def neighborhood-columns
  [:neighborhood/id :neighborhood/name])

(def route-columns
  [:route/id :route/name :route/type])

(def superscription-columns
  [:superscription/id 
   :superscription/num :superscription/complement :superscription/reference
   :superscription/city :superscription/state :superscription/created-at])

(def delict-columns
  [:delict/id :delict/name :delict/weight])

(def entity-columns
  [:entity/id :entity/name :entity/doc-type :entity/doc-issuer
   :entity/doc-number :entity/father :entity/mother
   :entity/phone])

(def request-columns
  [:request/id :request/complaint :request/summary :request/event-timestamp
   :request/status :request/measures
   :request/created-at :request/updated-at])

(def request-entity-columns
  [:request-entity/id :request-entity/request-id :request-entity/entity-id])

(def request-role-columns
  [:request-role/id :request-role/role])

(def superscription-query
  (conj superscription-columns
        {:superscription/neighborhood neighborhood-columns}
        {:superscription/route route-columns}))

(def entity-query
  (conj entity-columns
        {:entity/role request-role-columns}
        {:entity/superscription superscription-query}
        {:entity/superscriptions superscription-query}))

(def request-query
  (conj request-columns
        {:request/entities entity-query}
        {:request/superscription superscription-query}
        {:request/delicts delict-columns}))
