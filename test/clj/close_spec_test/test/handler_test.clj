(ns close-spec-test.test.handler-test
  (:require
   [clojure.test :refer :all]
   [ring.mock.request :refer :all]
   [close-spec-test.handler :refer :all]
   [expound.alpha :as expound]
   [clojure.spec.alpha :as s]
   [close-spec-test.middleware.formats :as formats]
   [muuntaja.core :as m]
   [mount.core :as mount]))

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))

(def expound-printer (expound/custom-printer {:print-specs? false}))
(set! s/*explain-out* (expound/custom-printer {:print-specs? false}))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'close-spec-test.config/env
                 #'close-spec-test.handler/app-routes)
    (f)))

(deftest ^:test-refresh/focus math
(testing "success"
    (let [response ((app) (-> (request :post "/api/math/plus")
                              (json-body {:x 10, :y 6})))]
      (is (= {:total 16} (m/decode-response-body response)))))
  (testing "failure"
    (let [response ((app) (-> (request :post "/api/math/plus")
                              (json-body {:x 10, :y 6, :z 9})))]
      (is (= 400 (:status response)) "Should fail on closed spec"))))


(deftest test-app
  (testing "success"
    (let [response ((app) (-> (request :post "/api/math/plus")
                              (json-body {:x 10, :y 6})))]
      (is (= 200 (:status response)))
      (is (= {:total 16} (m/decode-response-body response)))))
  (testing "main route"
    (let [response ((app) (request :get "/"))]
      (is (= 301 (:status response)))))

  (testing "not-found route"
    (let [response ((app) (request :get "/invalid"))]
      (is (= 404 (:status response)))))
  (testing "services"

    #_ (testing "parameter coercion error"
         (let [response ((app) (-> (request :post "/api/math/plus")
                                   (json-body {:x 10, :y "invalid"})))]
           (is (= 400 (:status response)))))

    #_ (testing "response coercion error"
         (let [response ((app) (-> (request :post "/api/math/plus")
                                   (json-body {:x -10, :y 6})))]
           (is (= 500 (:status response)))))

    #_(testing "content negotiation"
        (let [response ((app) (-> (request :post "/api/math/plus")
                                  (body (pr-str {:x 10, :y 6}))
                                  (content-type "application/edn")
                                  (header "accept" "application/transit+json")))]
          (is (= 200 (:status response)))
          (is (= {:total 16} (m/decode-response-body response)))))))
