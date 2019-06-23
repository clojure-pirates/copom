(ns copom.events.requests
  (:require
    [ajax.core :as ajax]
    [copom.events.utils :refer [base-interceptors]]
    [re-frame.core :as rf]))

(def requests-interceptos 
  (conj base-interceptors (rf/path :requests)))

(rf/reg-event-db
  :requests/clear-form
  requests-interceptos
  (fn [reqs _]
    (assoc reqs :new nil)))

(rf/reg-event-fx
  :requests/create
  requests-interceptos
  (fn [reqs [fields]]
    :todo
    nil))

(rf/reg-event-fx
  :requests/load-requests
  base-interceptors
  (fn [_ _]
    (ajax/GET "/api/requests"
              {:handler #(rf/dispatch [:rff/set :requests/all %])
               :error-handler #(prn %)
               :response-format :json
               :keywords? true})
    nil))

(rf/reg-sub
  :requests/all
  (fn [db _]
    (get-in db [:requests :all])))

(rf/reg-sub
  :requests/pending
  :<- [:requests/all]
  (fn [reqs _]
    (filter #(not= (:status %) "done") reqs)))

(rf/reg-sub
  :requests/latest
  :<- [:requests/all]
  (fn [reqs _]
    (take 10 reqs)))
        
(rf/reg-sub
  :requests.new/priority
  (fn [db _]
    (get-in db [:requests :new :priority])))

(def priority-weights
  {"apoio a policial" 4
   "ofensa à vida" 5
   "ofensa à integridade física" 2
   "ofensa à honra" 1
   "arma de fogo" 4
   "tráfico de drogas" 3
   "uso/porte de drogas" 2})

(rf/reg-sub 
  :requests/priority-score
  :<- [:requests.new/priority]
  (fn [priorities-desc _]
    (->> priorities-desc
         (filter (fn [[desc bool]] bool))
         (reduce (fn [acc [desc bool]] 
                   (+ acc (get priority-weights desc)))
                 0))))
                 
