(ns close-spec-test.handler
  (:require
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [clojure.spec.alpha :as s]
   [reitit.ring.coercion :as coercion]
   [reitit.spec :as rs]
   [reitit.coercion.spec :as spec-coercion]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.multipart :as multipart]
   [reitit.ring.middleware.parameters :as parameters]
   [close-spec-test.middleware.formats :as formats]
   [close-spec-test.middleware.exception :as exception]
   [ring.util.http-response :refer :all]
   [clojure.java.io :as io]
   [spec-tools.spell :as spell]
   [reitit.dev.pretty :as pretty]
   [close-spec-test.middleware :as middleware]
   [reitit.swagger-ui :as swagger-ui]
   [reitit.ring :as ring]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.webjars :refer [wrap-webjars]]
   [close-spec-test.env :refer [defaults]]
   [mount.core :as mount]))

(require '[reitit.core :as r])

(comment
(s/def ::description string?)
  (r/router
   ["/api" ]
   {:validate rs/validate
    :spec (s/merge ::rs/default-data
                   (s/keys :req-un [::description]))
    ::rs/wrap spell/closed
    :exception pretty/exception}) )

(s/def ::x int?)
(s/def ::y int?)
(s/def ::math-in (s/keys :req-un [::x ::y]))

(defn service-routes []
  ["/api"
   {:coercion spec-coercion/coercion
    :muuntaja formats/instance
    :swagger {:id ::api}
    :middleware [parameters/parameters-middleware ;; query-params & form-params
                 muuntaja/format-negotiate-middleware ;; content-negotiation
                 muuntaja/format-response-middleware  ;; encoding response body
                 exception/exception-middleware       ;; exception handling
                 muuntaja/format-request-middleware   ;; decoding request body
                 coercion/coerce-response-middleware  ;; coercing response bodys
                 coercion/coerce-request-middleware ;; coercing request parameters
                 multipart/multipart-middleware]}   ;; multipart
   ;; swagger documentation
   ["" {:no-doc true
        :swagger {:info {:title "my-api"
                         :description "https://cljdoc.org/d/metosin/reitit"}}}
    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]
    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
            {:url "/api/swagger.json"
             :config {:validator-url nil}})}]]
   ["/ping" {:get (constantly (ok {:message "pong"}))}]
   ["/math"
    {:swagger {:tags ["math"]}}
    ["/plus"
     {:post {:summary "plus with spec body parameters"
             :validate rs/validate
             :spec (s/merge ::rs/default-data
                            ::math-in)
             ::rs/wrap spell/closed
             :exception pretty/exception
             :parameters {:body {:x int?, :y int?}}
             :responses {200 {:body {:total pos-int?}}}
             :handler (fn [{{{:keys [x y]} :body} :parameters}]
                        {:status 200
                         :body {:total (+ x y)}})}}]]])


(mount/defstate init-app
  :start ((or (:init defaults) (fn [])))
  :stop  ((or (:stop defaults) (fn []))))

(mount/defstate app-routes
  :start
  (ring/ring-handler
    (ring/router
      [["/" {:get
             {:handler (constantly {:status 301 :headers {"Location" "/api/api-docs/index.html"}})}}
        {:validate rs/validate
         :exception pretty/exception}]
       (service-routes)])
    (ring/routes
      (ring/create-resource-handler
        {:path "/"})
      (wrap-content-type (wrap-webjars (constantly nil)))
      (ring/create-default-handler))))

(defn app []
  (middleware/wrap-base #'app-routes))


(comment
  (user/restart)
  )
