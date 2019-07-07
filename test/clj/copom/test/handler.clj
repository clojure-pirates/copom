(ns copom.test.handler
  (:require
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [copom.handler :refer [app]]
    [copom.middleware.formats :as formats]
    [muuntaja.core :as m]
    [mount.core :as mount]))

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))

(defn body-params [req params]
  (assoc req :body-params params))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'copom.config/env
                 #'copom.handler/app)
    (f)))

(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= 404 (:status response))))))

(comment
  (in-ns 'copom.test.handler)
  
  (-> (app {:request-method :post
            :uri "/api/requests"
            :body-params {:x 1}})

      :body
      slurp))
      