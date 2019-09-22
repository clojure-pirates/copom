(ns copom.middleware.formats
  (:require
    [cognitect.transit :as transit]
    [luminus-transit.time :as time]
    [muuntaja.core :as m]))  

(def instance
  (m/create
    (-> m/default-options
        (update-in
          [:formats "application/transit+json" :decoder-opts]
          (partial merge time/time-deserialization-handlers))
        (update-in
          [:formats "application/transit+json" :encoder-opts]
          (partial merge time/time-serialization-handlers)))))
                   

(comment
  (require '[copom.routes.requests :as r])
  (def req
    (-> (r/get-request {:parameters {:path {:request/id 1}}})
        :body))
  
  (->> (select-keys req [:request/summary :request/created-at])
       
       (m/encode instance "application/transit+json")
       slurp)

  (:request/created-at req)

  (require '[re-frame.core :as rf])
  (def reqs (rf/subscribe [:requests/all]))
  
  (def d
    (-> @reqs
        first
        :request/created-at)))