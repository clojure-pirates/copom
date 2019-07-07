(ns copom.router
  (:require
    [re-frame.core :as rf]
    [reitit.core :as reitit]))

(defn home-controller [{:keys [path]}]
  (rf/dispatch [:requests/load-requests]))

(defn requests-controller [{:keys [path]}]
  (rf/dispatch [:requests/load-requests]))

(defn create-request-controller [_]
  (rf/dispatch [:requests/load-delicts]))

(def router
  (reitit/router
    [["/" {:name :home
           :controllers [{:start home-controller}]}]
     ["/requisicoes" {:name :requests
                      :controllers [{:start requests-controller}]}]
     ["/requisicoes/criar" {:name :create-request
                            :controllers [{:start create-request-controller}]}]
     ["/about" :about]]))


(defn href [name]
  (if-let [match (reitit/match-by-name router name)]
    (str "/#" (:path match))
    (println "HREF: No match for" name)))
