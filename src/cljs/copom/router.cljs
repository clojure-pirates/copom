(ns copom.router
  (:require
    [re-frame.core :as rf]
    [reitit.core :as reitit]))

(defn home-controller []
  (rf/dispatch [:requests/load-requests]))

(defn requests-controller []
  (rf/dispatch [:requests/load-requests]))

(defn request-controller [params]
  (rf/dispatch [:requests/load-delicts])
  (rf/dispatch [:requests/load-request (js/parseInt (:id params))]))

(defn create-request-controller []
  (rf/dispatch [:requests/load-delicts]))

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
