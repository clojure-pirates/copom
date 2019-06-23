(ns copom.views.components
  (:require
    [reagent.core :as r]
    [clojure.string :as string]
    [reframe-forms.core :as rff]))

; ------------------------------------------------------------------------------
; Debugging
; ------------------------------------------------------------------------------

(defn pretty-display [data]
  [:pre
   (with-out-str
    (cljs.pprint/pprint data))])

; ------------------------------------------------------------------------------
; Utils
; ------------------------------------------------------------------------------

(defn on-key-handler
  "Takes a map of .-key's and functions. Returns a matching function. If
  the event.key str is present in the map, it calls the respective function."
  [keymap]
  (fn [event]
    (when-let [f (get keymap (.-key event))]
      (f))))

; ------------------------------------------------------------------------------
; Forms
; ------------------------------------------------------------------------------

(defn form-group
  "Bootstrap's `form-group` component."
  [label & input]
  [:div.form-group
   [:label label]
   (into
    [:div]
    input)])

(defn radio-input
  "Radio component, with common boilerplate."
  [{:keys [name class value label checked?]}]
  [:div.form-check.form-check-radio
   [:label.form-check-label
    [rff/input {:type :radio
                :name name
                :class (or class "form-check-input")
                :value value
                :checked? checked?}]
    label
    [:span.circle>span.check]]])

(defn checkbox-input
  "Checkbox component, with common boilerplate."
  [{:keys [name class label checked?]}]
  [:div.form-check
   [:label.form-check-label
    [rff/input {:type :checkbox
                :name name
                :class (or class "form-check-input")
                :checked? checked?}]
    label
    [:span.form-check-sign>span.check]]])

; ------------------------------------------------------------------------------
; MISC
; ------------------------------------------------------------------------------

(defn modal [{:keys [attrs header body footer]}]
  [:div attrs
   [:div.modal-dialog
    [:div.modal-content
     [:div.modal-header
      [:div.modal-title
       [:h3 header]]]
     [:div.modal-body body]
     (when footer
       [:div.modal-footer
         footer])]]
   [:div.modal-backdrop.fade.in]])

(defn nav-item [title active]
  [:li.nav-item
   [:a.nav-link
    {:on-click #(reset! active title)
     :style (when-not (= title @active) {:cursor :pointer})
     :class (when (= title @active) "active")}  
    title]])

(defn card-header [items active]
  [:div.card-header
   [:ul.nav.nav-tabs.card-header-tabs
    (for [item items]
      ^{:key item}
      [nav-item item active])]])  

(defn card-nav [items]
  (r/with-let [active (r/atom (-> items first :nav-title))]
    [:div.card
     [card-header (map :nav-title items) active]
     [:div
      (doall
        (for [{:keys [nav-title body]} items]
          ^{:key nav-title}    
          [:div.card-body
           {:class (when (not= nav-title @active) "d-none")}
           body]))]]))

; NOTE: `toggle?` -> when true, body can be collapsed and expanded by
; clicking on the title.
; NOTE: `visible?` -> body will be expanded by default when `toggle?` is true. 
(defn card [{:keys [title visible? toggle? subtitle body footer attrs]}]
  (r/with-let [show? (r/atom visible?)
               toggle-f #(swap! show? not)]
    [:div.card
     attrs
     [:div.card-body
       [:div.card-title 
        {:on-click (when toggle? toggle-f)}
        title]
       (when subtitle
         [:p.card-category subtitle])
       (when (or (not toggle?) (and toggle? @show?))
         [:div
           [:div.card-text
            body]
           [:div.card-footer
            footer]])]]))  

(defn breadcrumbs [& items]
  (into
   [:ol.breadcrumb
    [:li [:a {:href "/"} "Home"]]]
   (for [{:keys [href title active?] :as item} items]
     (if active?
       [:li.active title]
       [:li [:a {:href href} title]]))))

(defn thead [headers]
  [:thead
   [:tr
    (for [th headers]
      ^{:key th}
      [:th th])]])

(defn tbody [rows]
  (into
   [:tbody]
   (for [row rows]
     (into
      [:tr]
      (for [td row]
        [:td td])))))

(defn thead-indexed
  "Coupled with `tbody-indexed`, allocates a col for the row's index."
  [headers]
  [:thead
   (into
     [:tr
      [:th "#"]]
     (for [th headers]
       [:th th]))])

(defn tbody-indexed
  "Coupled with `thead-indexed`, allocates a col for the row's index."
  [rows]
  (into
   [:tbody]
   (map-indexed
    (fn [i row]
      (into
       [:tr [:td (inc i)]]
       (for [td row]
         [:td
          td])))
    rows)))

(defn tabulate
  "Render data as a table.
  `rows` is expected to be a coll of maps.
  `ks` are the a the set of keys from rows we want displayed.
  `class` is css class to be aplied to the `table` element."
  ([ks rows] (tabulate ks rows {}))
  ([ks rows {:keys [class]}]
   [:table
    {:class class}
    ;; if there are extra headers we append them
    [thead (map (comp (fn [s] (string/replace s #"-" " "))
                      string/capitalize
                      name)
                ks)]
    [tbody (map (apply juxt ks) rows)]]))
