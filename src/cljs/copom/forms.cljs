(ns copom.forms
  (:require
   [cljs.reader :as reader]
   [clojure.string :as string]
   [reagent.core :as r]
   [re-frame.core :as rf]))

(defn make-vec [x]
  (if (coll? x) x [x]))

; -----------------------------------------------------------------------------
; Input Components Utils
; -----------------------------------------------------------------------------

(defn clean-attrs [attrs]
  (dissoc attrs :save-fn
                :value-fn
                :default-value
                :doc
                :data-source
                :handler
                :checked?))

(defn get-stored-val [doc name]
  (get-in doc (make-vec name)))

(defn target-value [event]
  (.-value (.-target event)))

(defn parse-number [string]
  (when-not (empty? string)
    (let [parsed (js/parseFloat string)]
      (when-not (js/isNaN parsed)
        parsed))))

(defn read-string*
  "Same as cljs.reader/read-string, except that returns a string when
  read-string returns a symbol."
  [x]
  (let [parsed (reader/read-string x)]
    (if (symbol? parsed)
      (str parsed)
      parsed)))

(defn set-val!
  [doc name v]
  (swap! doc assoc-in (make-vec name) v))

(defn on-change-set!
  [doc name f]
  (fn [event]
    (swap! doc assoc-in (make-vec name) (f event))))

(defn on-change-update! 
  "Takes a path and a function and returns a handler.
  The function will be called on the value stored at path."
  [doc name f]
  (fn [event]
    (swap! doc update-in (make-vec name) f)))

(defn multiple-opts-fn [value]
  (fn [acc]
    (if (contains? acc value)
      (disj acc value)
      ((fnil conj #{}) acc value))))

(defn on-change-update-multiple! 
  "Takes a path and a value and returns a handler.
  The value will be disj'ed or conj'ed, depending if it is included or
  not at path."
  [doc name value]
  (fn [_]
    (swap! doc update-in (make-vec name) (multiple-opts-fn value))))


; NOTE: Reason for `""`: https://zhenyong.github.io/react/tips/controlled-input-null-value.html
(defn value-attr [value]
  (or value ""))

(defn maybe-set-default-value! [{:keys [doc name default-value]}]
  (when (and (nil? (get-stored-val @doc name))
             default-value)
    (set-val! doc name default-value)))

; -----------------------------------------------------------------------------
; Input Components
; -----------------------------------------------------------------------------

(defmulti input :type)

; text, email, password
(defmethod input :default
  [attrs]
  (let [{:keys [name default-value doc]} attrs
        edited-attrs
        (merge {:on-change (on-change-set! doc name target-value)}
               (clean-attrs attrs))]
    (fn []
      (maybe-set-default-value! attrs)
      [:input (assoc edited-attrs 
                :value (value-attr (get-stored-val @doc name)))])))

(defmethod input :number
  [attrs]
  (let [{:keys [name default-value doc]} attrs
        edited-attrs
        (merge {:on-change (on-change-set! doc name 
                                           (comp parse-number target-value))}
               (clean-attrs attrs))]
    (fn []
      (maybe-set-default-value! attrs)
      [:input (assoc edited-attrs
                :value (value-attr (get-stored-val @doc name)))])))


(defn textarea
  [attrs]
  (let [{:keys [name default-value doc]} attrs
        edited-attrs
        (merge {:on-change (on-change-set! doc name target-value)}
               (clean-attrs attrs))]
    (fn []
      (maybe-set-default-value! attrs)
      [:textarea (assoc edited-attrs
                   :value (value-attr (get-stored-val @doc name)))])))
                 

(defmethod input :radio
  [attrs]
  (let [{:keys [name checked? doc value]} attrs
        edited-attrs
        (merge {:on-change (on-change-set! doc name
                                           (comp read-string* target-value))}
               (clean-attrs attrs))]
    (fn []
      (when (and (nil? (get-stored-val @doc name))
                 checked?)
        (set-val! doc name value))
      [:input (assoc edited-attrs
                :checked (= value (get-stored-val @doc name)))])))
     
;; "Each checkbox name is stored as a map key, pointing to a boolean."
(defmethod input :checkbox
  [attrs]
  (let [{:keys [name checked? doc]} attrs
        edited-attrs
        (merge {:on-change (on-change-update! doc name not)}
               (clean-attrs attrs))]
    (fn []
      (cond (and (nil? (get-stored-val @doc name))
                 checked?)
            (set-val! doc name true)
            
            (nil? (get-stored-val @doc name))
            (set-val! doc name false))
      [:input (assoc edited-attrs
                :checked (boolean (get-stored-val @doc name)))])))

; Uses plain HTML5 <input type="date" />

(defn- to-timestamp 
  "Takes a string in the format 'yyyy-mm-dd' and returns a timestamp (int).
  If date-string is empty, returns nil."
  [date-string]
  (when-not (clojure.string/blank? date-string)
    (.getTime
      (js/Date. date-string))))

(defn- to-iso-string
  "Takes a string in the format 'yyyy-mm-dd' and returns a ISO date string.
  If date-string is empty, returns nil." 
  [date-string]
  (when-not (clojure.string/blank? date-string)
    (.toISOString
      (js/Date. date-string))))

(defn- to-date-format 
  "Takes a value that can be passed to js/Date. and retuns a string in 
  the format 'yyyy-mm-dd'. If x is nil, returns an empty string."
  [x]
  (if (nil? x)
    ""
    (-> (js/Date. x)
        .toISOString
        (clojure.string/split #"T")
        first)))

(defmethod input :date
  [attrs]
  (let [{:keys [name default-value doc save-fn value-fn]
         :or {save-fn to-iso-string
              value-fn to-date-format}} attrs
        edited-attrs
        (merge {:on-change (on-change-set! doc name (comp save-fn target-value))
                ;; If there's no browser support,
                ;; then at least we'll display the expected
                ;; format, and ...
                :placeholder "yyyy-mm-dd"
                ;; ... we'll display an error message if the wrong 
                ;; format is submitted.
                :pattern "[0-9]{4}-[0-9]{2}-[0-9]{2}"}
               (clean-attrs attrs))]
    (fn []
      (when (and (nil? (get-stored-val @doc name))
                 default-value)
        (set-val! doc name (save-fn default-value)))
      [:input (assoc edited-attrs
                :value (value-fn (get-stored-val @doc name)))])))

; NOTE: if you want to select multiple, you must provide the multiple key
; with a truthy value.
; NOTE: You must provide a default-value with the default-value, or set the
; default-value on the name path. `default-value` is playing the selected
; part. Maybe default the default-value to the first option when there's no
; stored-val and no default-value.
(defn select
  [attrs options]
  (let [{:keys [name default-value doc multiple]} attrs
        on-change-fn (if multiple on-change-update-multiple! on-change-set!)
        edited-attrs
        (merge {:on-change (on-change-fn doc name (comp read-string* target-value))}
               (clean-attrs attrs))]
    (fn []
      (when (and (not (get-stored-val @doc name))
                 default-value)
        (if multiple
          ((on-change-update-multiple! doc name default-value) nil)
          (swap! doc assoc-in (make-vec name) default-value)))
      (into
        [:select (assoc edited-attrs
                   :value (value-attr (get-stored-val @doc name)))]
        options))))

;;; TODO: try Pickaday datepicker

(def datetime-format "yyyy-mm-ddT03:00:00.000Z")

; NOTE: only with Bootstrap 3
; NOTE: REQUIRES 
; "bootstrap.min.css"
; "bootstrap.min.js"
; "https://cdnjs.cloudflare.com/ajax/libs/bootstrap-datepicker/1.7.1/css/bootstrap-datepicker.min.css" 
; "https://cdnjs.cloudflare.com/ajax/libs/bootstrap-datepicker/1.7.1/js/bootstrap-datepicker.min.js"
(defn datepicker
  [attrs]
  (r/create-class
    {:display-name "datepicker component"
     
     :reagent-render
     (fn [attrs]
       (let [edited-attrs (-> attrs
                              (assoc :type :text)
                              (update :class str " form-control"))]
          [:div.input-group.date
           [input edited-attrs]
           [:div.input-group-addon
            [:i.glyphicon.glyphicon-calendar]]]))
     
     :component-did-mount
     (fn [this]
       (.datepicker (js/$ (r/dom-node this))
                    (clj->js {:format (or (:format attrs) 
                                          datetime-format)}))
       (-> (.datepicker (js/$ (r/dom-node this)))
           (.on "changeDate"
                #(let [d (.datepicker (js/$ (r/dom-node this))
                                      "getDate")]
                   (rf/dispatch [:rff/set (:name attrs)
                                 (.getTime d)])))))}))
               
; file