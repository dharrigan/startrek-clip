(ns startrek.api.router
  {:author "David Harrigan"}
  (:require
   [jsonista.core :as j]
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
   [startrek.api.general.actuator :as actuator-api]
   [startrek.api.general.favicon :as favicon-api]
   [startrek.api.general.health :as health-api]
   [startrek.api.general.metrics :as metrics]
   [startrek.api.general.swagger :as swagger-api]
   [startrek.api.middleware.exceptions :as exceptions]
   [startrek.api.starship.routes :as starship-api])
  (:import
   [org.eclipse.jetty.server Server]
   [com.fasterxml.jackson.annotation JsonInclude$Include]))

(set! *warn-on-reflection* true)

(def ^:private cors-middleware
  [wrap-cors
   :access-control-allow-origin [#".*"]
   :access-control-allow-methods [:delete :get :patch :post]])

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
           :muuntaja (m/create
                      (assoc-in m/default-options
                                [:formats "application/json" :opts]
                                {:mapper (-> (j/object-mapper {:decode-key-fn true})
                                             (.setSerializationInclusion JsonInclude$Include/NON_EMPTY))})) ;; strip away empty stuff!
           :middleware [cors-middleware
                        swagger/swagger-feature
                        muuntaja/format-middleware
                        (exceptions/exception-middleware)
                        parameters/parameters-middleware
                        coercion/coerce-exceptions-middleware
                        coercion/coerce-request-middleware
                        coercion/coerce-response-middleware]}}))

;; CLIP Lifecycle Functions

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn start
  [{:keys [runtime-config] :as app-config}]
  (jetty/run-jetty
   (ring/ring-handler (router app-config)
                      (ring/routes
                       (swagger-ui/create-swagger-ui-handler
                        {:path "/"
                         :operationsSorter "alpha"
                         :docExpansion "full"
                         :validatorUrl nil})
                       (ring/create-default-handler)))
   (merge (:jetty runtime-config) {:allow-null-path-info true
                                   :send-server-version? false
                                   :send-date-header? false
                                   :join? false}))) ;; false so that we can stop it at the repl!

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn stop
  [^Server server]
  (.stop server) ; stop is async
  (.join server)) ; so let's make sure it's really stopped!
