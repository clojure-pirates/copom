(ns copom.views.core
  (:require
    [copom.views.requests :as requests]
    [markdown.core :refer [md->html]]
    [reagent.core :as r]
    [re-frame.core :as rf]))

(defn nav-link [uri title page]
  [:a.navbar-item
   {:href   uri
    :class (when (= page @(rf/subscribe [:page])) :is-active)}
   title])

(defn navbar []
  (r/with-let [expanded? (r/atom false)]
    [:nav.navbar.is-info>div.container
     [:div.navbar-brand
      [:a.navbar-item {:href "/" :style {:font-weight :bold}} "copom"]
      [:span.navbar-burger.burger
       {:data-target :nav-menu
        :on-click #(swap! expanded? not)
        :class (when @expanded? :is-active)}
       [:span][:span][:span]]]
     [:div#nav-menu.navbar-menu
      {:class (when @expanded? :is-active)}
      [:div.navbar-start
       [nav-link "#/" "Home" :home] " "
       [nav-link "#/about" "About" :about]]]]))

(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]])

(defn home-page []
  [:section.section>div.container>div.content
   (when-let [docs @(rf/subscribe [:docs])]
     [:div {:dangerouslySetInnerHTML {:__html (md->html docs)}}])])

;; -------------------------
;; Views Core


(def pages
  {:home #'requests/dashboard
   :about #'about-page
   :requests #'requests/requests-page
   :create-request #'requests/create-request-page
   :request #'requests/request-page})

(defn page []
  [:div
   [navbar]
   [(pages @(rf/subscribe [:page]))]])
