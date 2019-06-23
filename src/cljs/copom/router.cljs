(ns copom.router
  (:require
    [re-frame.core :as rf]
    [reitit.core :as reitit]))

(defn home-controller [{:keys [path]}]
  (rf/dispatch [:requests/load-requests]))

(defn requests-controller [{:keys [path]}]
  (rf/dispatch [:requests/load-requests]))

(def router
  (reitit/router
    [["/" {:name :home
           :controllers [{:start home-controller}]}]
     ["/requisicoes" {:name :requests
                      :controllers [{:start requests-controller}]}]
     ["/requisicoes/criar" :create-request]
     ["/about" :about]]))
