(ns startrek.api
  (:require
   [muuntaja.core :as m]
   [reitit.coercion.schema :as rcs]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [ring.adapter.jetty :as jetty]
   [schema.core :as s]
   [startrek.db :as db])
  (:import
   [org.eclipse.jetty.server Server]))

(def ^:const OK 200)
(def ^:const NOT-FOUND 404)

(defn jetty-start
  [handler opts]
  (jetty/run-jetty handler (-> opts (assoc :join? false))))

(defn jetty-stop
  [^Server server]
  (.stop server) ; stop is async
  (.join server)) ; so let's make sure it's really stopped!

(defn get-starships
  [app-config]
  (fn [_] ; return a function that takes the "request" as the input, although we don't do anything with it.
    (if-let [results (seq (db/find-starships app-config))]
      {:headers {"Access-Control-Allow-Origin" "*"}
       :status OK
       :body results}
      {:status NOT-FOUND})))

(defn get-starship-by-id
  [app-config]
  (fn [{{{:keys [id]} :path} :parameters}] ; return a function that will take the "request" as the input
    (if-let [results (db/find-starship-by-id id app-config)]
      {:headers {"Access-Control-Allow-Origin" "*"}
       :status OK :body results}
      {:status NOT-FOUND})))

(defn router
  [app-config]
  (ring/router
   ["/api"
    ["/ping" {:get (fn [_] {:status 200 :body "Pong!"})}]
    ["/starships" (get-starships app-config)]
    ["/starships/:id" {:get (get-starship-by-id app-config)
                       :parameters {:path {:id s/Int}}}]]
   {:data {:coercion rcs/coercion
           :muuntaja m/instance
           :middleware [muuntaja/format-middleware
                        exception/exception-middleware
                        coercion/coerce-request-middleware
                        coercion/coerce-response-middleware]}}))

(defn ring-handler
  [app-config]
  (ring/ring-handler (router app-config)))
