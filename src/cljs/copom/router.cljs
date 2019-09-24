(ns copom.router
  (:require
    [copom.db :refer [app-db]]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [reagent.session :as session]
    [reitit.core :as reitit]))

(defn home-controller []
  (rf/dispatch [:requests/load-requests]))

(defn requests-controller []
  (rf/dispatch [:requests/load-requests]))

(defn request-controller [params]
  (rf/dispatch [:requests/load-delicts])
  (rf/dispatch [:requests/load-request (js/parseInt (:id params))]))

(defn create-request-controller [] 
  (let [doc (r/cursor app-db [:request])]
    (rf/dispatch-sync [:requests/clear-form doc])
    (rf/dispatch [:requests/load-delicts])))

(def router
  (reitit/router
    [["/" {:name :home
           :controllers [{:start home-controller}]}]
     ["/requisicoes" {:name :requests
                      :controllers [{:start requests-controller}]}]
     ["/requisicoes/criar" {:name :create-request
                            :controllers [{:start create-request-controller}]}]
     ["/requisicoes/:id/editar" {:name :request
                                 :controllers [{:params :path-params
                                                :start request-controller}]}]
                                               
     ["/about" :about]]))


(defn href [name]
  (if-let [match (reitit/match-by-name router name)]
    (str "/#" (:path match))
    (println "HREF: No match for" name)))
