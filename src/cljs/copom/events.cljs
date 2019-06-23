(ns copom.events
  (:require
    [copom.router :as router]
    copom.events.requests
    [re-frame.core :as rf]
    [reframe-forms.events]
    [reitit.core :as reitit]
    [reitit.frontend.controllers :as rfc]
    [ajax.core :as ajax]))

;;dispatchers

(def default-db {})                            

(rf/reg-event-db
  :set-default-db
  (fn [db _]
    (merge db default-db)))

(defn apply-controllers! [old-match new-match]
  (rfc/apply-controllers
    (:controllers old-match) new-match))

(rf/reg-event-db
  :navigate*
  (fn [db [_ route]]
    (apply-controllers! (:route db) route)
    (assoc db :route route)))

(rf/reg-event-db
  :navigate-by-path
  (fn [db [_ uri]]
    (let [new-match (reitit/match-by-path router/router uri)]
      (apply-controllers! (:route db) new-match)
      (assoc db :route new-match))))

(rf/reg-event-db
  :navigate-by-name
  (fn [db [_ name]]
    (let [new-match (reitit/match-by-name router/router name)]
      (apply-controllers! (:route db) new-match)
      (assoc db :route new-match))))

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

