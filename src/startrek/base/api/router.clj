(ns startrek.base.api.router
  {:author ["David Harrigan"]}
  (:require
   [muuntaja.core :as m]
   [reitit.coercion.malli :as rcm]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   [reitit.spec :as rs]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.cors :refer [wrap-cors]]
   [startrek.base.api.general.actuator :as actuator-api]
   [startrek.base.api.general.favicon :as favicon-api]
   [startrek.base.api.general.health :as health-api]
   [startrek.base.api.general.metrics :as metrics]
   [startrek.base.api.general.swagger :as swagger-api]
   [startrek.base.api.middleware.exceptions :as exceptions]
   [startrek.base.api.starship.routes :as starship-api])
  (:import
   [org.eclipse.jetty.server Server]))

(set! *warn-on-reflection* true)

(defn ^:private router
  [app-config]
  (ring/router
   [(merge ["/api" {:middleware [[metrics/prometheus]]}]
           (starship-api/routes app-config))
    (actuator-api/routes app-config)
    swagger-api/routes
    health-api/routes
    favicon-api/routes]
   {:validate rs/validate
    :data {:coercion rcm/coercion
           :muuntaja m/instance
           :middleware [swagger/swagger-feature
                        muuntaja/format-middleware
                        (exceptions/exception-middleware)
                        [wrap-cors :access-control-allow-origin [#".*"]
                                   :access-control-allow-methods [:get :put :post :patch :delete]]
                        parameters/parameters-middleware
                        coercion/coerce-exceptions-middleware
                        coercion/coerce-request-middleware
                        coercion/coerce-response-middleware]}}))

;; CLIP Lifecycle Functions

(defn start
  [app-config opts]
  (jetty/run-jetty
   (ring/ring-handler (router app-config)
                      (ring/routes
                       (swagger-ui/create-swagger-ui-handler
                        {:path "/"
                         :operationsSorter "alpha"
                         :docExpansion "full"
                         :validatorUrl nil})
                       (ring/create-default-handler)))
   (assoc opts
          :send-server-version? false
          :send-date-header? false
          :join? false))) ;; false so that we can stop it at the repl!

(defn stop
  [^Server server]
  (.stop server) ; stop is async
  (.join server)) ; so let's make sure it's really stopped!
