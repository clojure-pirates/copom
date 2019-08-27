(ns copom.events
  (:require
    [copom.router :as router]
    copom.events.request
    copom.events.superscription
    [copom.events.utils :refer [base-interceptors]]
    [re-frame.core :as rf]
    [reframe-forms.events]
    [reitit.core :as reitit]
    [reitit.frontend.controllers :as rfc]
    [ajax.core :as ajax]))

;;dispatchers

(def default-db {})

(rf/reg-event-db
 :modal
 base-interceptors
 (fn [db [comp]]
   (let [modal-stack (:modal db)]
     (if (seq modal-stack)
       (update db :modal conj comp)
       (assoc db :modal [comp])))))

(rf/reg-event-db
 :remove-modal
 base-interceptors
 (fn [db _]
   (let [modal-stack (:modal db)]
     (if (seq modal-stack)
       (update db :modal pop)
       (assoc db :modal [])))))

(defn navigate! [uri]
  (set! js/location uri))                         

(rf/reg-event-db
  :set-default-db
  (fn [db _]
    (merge db default-db)))

(rf/reg-event-fx
  :navigate/by-path
  (fn [_ [_ uri]]
    (navigate! uri)
    nil))

(rf/reg-event-fx
  :navigate/by-name
  (fn [_ [_ name]]
    (if-let [match (reitit/match-by-name router/router name)]
      (navigate! (str "/#" (:path match)))
      (println "No match! for" name))
    nil))

(defn apply-controllers! [old-match new-match]
  (rfc/apply-controllers
    (:controllers old-match) new-match))

(rf/reg-event-db
  :navigate*
  (fn [db [_ route]]
    (let [controllers (apply-controllers! (:route db) route)]
      (assoc db :route (assoc route :controllers controllers)))))

(rf/reg-event-db
  :navigate-by-path
  base-interceptors
  (fn [db [uri]]
    (let [new-match (reitit/match-by-path router/router uri)
          controllers (apply-controllers! (:route db) new-match)]
      (assoc db :route (assoc new-match :controllers controllers)))))

(rf/reg-event-db
  :navigate-by-name
  base-interceptors
  (fn [db [name]]
    (let [new-match (reitit/match-by-name router/router name)
          controllers (apply-controllers! (:route db) new-match)]
      (assoc db :route (assoc new-match :controllers controllers)))))

(rf/reg-event-db
  :set-docs
  (fn [db [_ docs]]
    (assoc db :docs docs)))

(rf/reg-event-fx
  :fetch-docs
  (fn [_ _]
    {:http-xhrio {:method          :get
                  :uri             "/docs"
                  :response-format (ajax/raw-response-format)
                  :on-success       [:set-docs]}}))

(rf/reg-event-db
  :common/set-error
  (fn [db [_ error]]
    (assoc db :common/error error)))

;;subscriptions

(rf/reg-sub
  :modal
  (fn [db _]
    (let [modal-stack (:modal db)]
      (when (seq modal-stack)
        (peek modal-stack)))))

(rf/reg-sub
  :route
  (fn [db _]
    (-> db :route)))

(rf/reg-sub
  :page
  :<- [:route]
  (fn [route _]
    (-> route :data :name)))

(rf/reg-sub
  :docs
  (fn [db _]
    (:docs db)))

(rf/reg-sub
  :common/error
  (fn [db _]
    (:common/error db)))

