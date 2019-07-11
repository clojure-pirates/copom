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
  (-> req
      (json-body params)
      (clojure.set/rename-keys {:body :body-params})))

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
  
  (-> (app {:request-method :get
            :uri "/api/requests/1"})

      :body
      parse-json)
  
  (-> (app (-> (request :post "/api/requests")
               (body-params r))))

  (def r  {:request/delicts
           {1 true, 2 false, 3 false, 4 false, 5 false, 6 false, 7 true},
           :request/requester
           {:entity/role "requester",
            :entity/superscription
            {:superscription/route {:route/type "Rua"},
             :superscription/city "Guarantã do Norte",
             :superscription/state "Mato Grosso"},
            :entity/doc-type "CPF"},
           :request/status "pending",
           :request/victim
           {:entity/role "victim",
            :entity/superscription
            {:superscription/route {:route/type "Rua"},
             :superscription/city "Guarantã do Norte",
             :superscription/state "Mato Grosso"},
            :entity/doc-type "CPF"},
           :request/superscription
           {:superscription/route {:route/type "Rua"},
            :superscription/city "Guarantã do Norte",
            :superscription/state "Mato Grosso"},
           :request/date "2019-07-10T00:00:00.000Z",
           :request/witness
           {:entity/role "witness",
            :entity/superscription
            {:superscription/route {:route/type "Rua"},
             :superscription/city "Guarantã do Norte",
             :superscription/state "Mato Grosso"},
            :entity/doc-type "CPF"},
           :request/complaint "create test",
           :request/time "08:57:58",
           :request/suspect
           {:entity/role "suspect",
            :entity/superscription
            {:superscription/route {:route/type "Rua"},
             :superscription/city "Guarantã do Norte",
             :superscription/state "Mato Grosso"},
            :entity/doc-type "CPF"}, 
           :request/summary "create test"}))