(ns copom.views.components
  (:require
    [ajax.core :as ajax]
    [reagent.core :as r]
    [clojure.string :as string]
    [cljs.pprint]
    [re-frame.core :as rf]
    [copom.forms :as rff]))

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
  [{:keys [name class doc value label checked?]}]
  [:div.form-check.form-check-radio
   [:label.form-check-label
    [rff/input (merge
                 {:type :radio
                  :name name
                  :class (or class "form-check-input")
                  :doc doc
                  :value value
                  :checked? checked?})]
    label
    [:span.circle>span.check]]])

(defn checkbox-input
  "Checkbox component, with common boilerplate."
  [{:keys [name class doc label checked?]}]
  [:div.form-check
   [:label.form-check-label
    [rff/input {:type :checkbox
                :name name
                :class (or class "form-check-input")
                :doc doc
                :checked? checked?}]
    label
    [:span.form-check-sign>span.check]]])

; ------------------------------------------------------------------------------
; REFRAME-FORMS TEMP
; ------------------------------------------------------------------------------

;;; TYPEHEAD INPUT

(rf/reg-event-fx
  :rff/typehead-query
  (fn [_ [_ {{:keys [uri handler]} 
             :data-source name :name getter :getter 
             :as attrs}
            items]]
    (let [path (if (coll? name) name [name])
          base-path (into [:rff/data] path)
          q (rff/get-stored-val @(:doc attrs) name)
          save-and-display 
          (fn [items*]
            (prn items*)
            (do (reset! items items*)
                (rf/dispatch [:rff/set (conj base-path :display?) true])))]
      (when-not (string/blank? q)
        (cond uri
              (ajax/GET uri
                        {:params {:query q}
                         :handler #(let [items* (if handler (handler %) %)]
                                     (save-and-display items*))
                         :error-handler #(prn "typehead-input" %)
                         :response-format :json
                         :keywords? true})
              handler (-> q handler save-and-display))))
    nil))

; - As the user types the component will query the :uri given in :data-source.
; If a :handler was given, it will be called with the result of the query.
; - If no :uri was given, :handler must provide the items for the component.
; - If each item is a coll, the user can provide a :getter (fn) to fetch the 
; text for the :value. By default :getter is `identity`.
; - The user can also provide a :handler that will be passed an item when
; the frontend user clicks an item of the list.
(defn input [attrs]
  (let [{:keys [name data-source doc getter
                handler no-results]
         :or {getter identity}} attrs
        display? (r/atom nil)
        items (r/atom nil)
        on-change
        (fn [e]
          (rff/set-val! doc name (rff/target-value e))
          (let [{uri :uri h :handler} data-source
                q (rff/get-stored-val @doc name)
                save-and-display 
                (fn [items*]
                  (do (reset! items items*)
                      (reset! display? true)))]
            (when-not (string/blank? q)
              (cond uri
                    (ajax/GET uri
                              {:params {:query q}
                               :handler #(let [items* (if h (h %) %)]
                                           (save-and-display items*))
                               :error-handler #(prn "typehead-input" %)
                               :response-format :json
                               :keywords? true})
                    h (-> q h save-and-display)))))
        edited-attrs
        (merge {:on-change on-change
                :type :text
                :on-key-down (on-key-handler {"Escape" #(reset! display? false)})} 
               (-> attrs (dissoc :handler) rff/clean-attrs))]
    (fn []
      [:div
        [:input (assoc edited-attrs :value (rff/get-stored-val @doc name))]
        (when @display?
          (if-not (seq @items)
            no-results
            [:div.dropdown
             [:button.btn.btn-secondary.dropdown-toggle.d-none "Dropdown"]
             [:div.dropdown-menu (when (seq @items) {:class "show"})
              (for [item @items]
               ^{:key item}
               [:button.dropdown-item
                {:on-click #(do (reset! display? false)
                                (swap! doc assoc-in (rff/make-vec name)
                                       (getter item)) 
                                (when handler (handler item)))}
                (getter item)])]]))])))

; ------------------------------------------------------------------------------
; MISC
; ------------------------------------------------------------------------------

(defn modal [{:keys [attrs header body footer]}]
  [:div attrs
   [:div.modal-dialog
    [:div.modal-content
     [:div.modal-header
      [:div.modal-title
       header]]
     [:div.modal-body body]
     (when footer
       [:div.modal-footer
         footer])]]
   [:div.modal-backdrop.fade-in]])

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

(defn forward [i pages]
  (if (< i (dec pages)) (inc i) i))

(defn back [i]
  (if (pos? i) (dec i) i))

(defn nav-link [page i]
  [:li.page-item>a.page-link.btn.btn-primary
   {:on-click #(reset! page i)
    :class (when (= i @page) "active")}
   [:span i]])

(defn pager [pages page]
  (when (> pages 1)
    (into
     [:div.text-xs-center>ul.pagination.pagination-lg]
     (concat
      [[:li.page-item>a.page-link.btn.btn-primary
        {:on-click #(swap! page back pages)
         :class (when (= @page 0) "disabled")}
        [:span "<<"]]]
      (map (partial nav-link page) (range pages))
      [[:li.page-item>a.page-link.btn.btn-primary
        {:on-click #(swap! page forward pages)
         :class (when (= @page (dec pages)) "disabled")}
        [:span ">>"]]]))))

(defn partition-links [links]
  (when (not-empty links)
    (vec (partition-all 6 links))))
