(ns startrek.base.api.middleware.exceptions
  {:author ["David Harrigan"]}
  (:require
   [clojure.string :refer [split]]
   [clojure.tools.logging :as log]
   [reitit.ring.middleware.exception :as exception]
   [startrek.base.api.middleware.i18n :as i18n]
   [taoensso.tempura :as tempura])
  (:import
   [java.util UUID]))

(set! *warn-on-reflection* true)

(def ^:private double-colon (re-pattern "::"))
(def ^:private hypen (re-pattern "-"))
(def ^:private i18n (partial tempura/tr {:dict i18n/dictionary} [:en]))
(def ^:private internal-server-error 500)

(def ^:private bad-request 400)
(def ^:private not-found 404)
(def ^:private conflict 409)

(defn ^:private split-error-message
  [error]
  (let [code-and-error (split error double-colon)]
    {:code (first code-and-error) :msg (str (second code-and-error))}))

(defn ^:private exception-handler
  [status error _ request] ; _ (exception) and request come from reitit.
  (let [uuid (.toString (UUID/randomUUID))
        {:keys [code msg]} (split-error-message (i18n [error]))
        body {:code code :error msg :reference uuid :uri (:uri request)}]
    (log/error body)
    {:status status :body (assoc body :reference (first (split uuid hypen)))}))

(defn ^:private format-error
  [error msg cause]
  (case error
    ;; These come from the i18n/dictionary map
    :general.exception msg
    :service.unavailable msg
    :database.unavailable msg
    :resource.starship.exists (format msg (:id cause))
    :http.internal-server-error (format msg cause)))

(defn ^:private handle-application-error
  [reference {:keys [cause error] :or {error :general.exception} :as exception-data}]
  (let [{:keys [code msg]} (split-error-message (i18n [error]))
        formatted-error (format-error error msg cause)]
    {:code code :reference reference :error formatted-error}))

(defn ^:private exception-info-handler
  [exception-info _] ; exception(-info) and request (not-used) come from reitit.
  (let [uuid (.toString (UUID/randomUUID))
        reference (first (split uuid hypen))
        {:keys [type status] :as exception-data} (ex-data exception-info)
        body (case type
               ;; add more types here to handle, like clj-http exceptions etc...
               (handle-application-error reference exception-data))]
    (log/error body)
    (case status
      :404 {:status not-found :body body}
      :409 {:status conflict :body body}
      {:status bad-request :body body})))

(defn exception-middleware
  []
  (let [error-500 (partial exception-handler internal-server-error)]
    (exception/create-exception-middleware
     (merge
      exception/default-handlers
      {java.io.IOException (partial error-500 :service.unavailable)
       java.sql.SQLException (partial error-500 :database.unavailable)
       clojure.lang.ExceptionInfo exception-info-handler
       ::exception/default (partial error-500 :general.exception)
       ::exception/wrap (fn [handler exception request]
                          (log/error exception)
                          (handler exception request))}))))
