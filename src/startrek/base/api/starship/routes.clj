(ns startrek.base.api.starship.routes
  {:author ["David Harrigan"]}
  (:require
   [startrek.base.api.starship.mapper :as mapper]
   [startrek.base.api.starship.specs :as specs]
   [startrek.base.api.starship.transformations :as transformations]
   [startrek.components.starship.interface :as starship]))

(set! *warn-on-reflection* true)

(def ^:private starships-create-api-version "application/vnd.startrek.starships.create.v1+json;charset=utf-8")
(def ^:private starships-delete-api-version "application/vnd.startrek.starships.delete.v1+json;charset=utf-8")
(def ^:private starships-find-by-id-api-version "application/vnd.startrek.starships.find-by-id.v1+json;charset=utf-8")
(def ^:private starships-search-api-version "application/vnd.startrek.starships.search.v1+json;charset=utf-8")
(def ^:private starships-modify-api-version "application/vnd.startrek.starships.modify.v1+json;charset=utf-8")

(def ^:private ok 200)
(def ^:private created 201)
(def ^:private not-found 404)

(defn ^:private create
  [app-config]
  (fn [request]
    (when-let [uuid (some-> (mapper/request->create request)
                            (starship/create app-config)
                            (transformations/create))]
      {:status created :body uuid})))

(defn ^:private delete
  [app-config]
  (fn [request]
    (if-let [uuid (some-> (mapper/request->delete request)
                          (starship/delete app-config)
                          (transformations/delete))]
      {:status ok :body uuid}
      {:status not-found})))

(defn ^:private find-by-id
  [app-config]
  (fn [request]
    (if-let [starship (some-> (mapper/request->find-by-id request)
                              (starship/find-by-id app-config)
                              (transformations/find-by-id))]
      {:status ok :body starship}
      {:status not-found})))

(defn ^:private modify
  [app-config]
  (fn [request]
    (if-let [starship (some-> (mapper/request->modify request)
                              (starship/modify app-config)
                              (transformations/modify))]
      {:status ok :body starship}
      {:status not-found})))

(defn ^:private search
  [app-config]
  (fn [request]
    (if-let [starships (some-> (mapper/request->search request)
                               (starship/search app-config)
                               (transformations/search))]
      {:status ok :body starships}
      {:status not-found})))

(defn routes
  [app-config]
  ["/starships"
   ["" {:get {:handler (search app-config)
              :parameters {:query specs/search}
              :swagger {:produces [starships-search-api-version]}}
        :post {:handler (create app-config)
               :parameters {:body specs/create}
               :swagger {:produces [starships-create-api-version]}}}]
   ["/:id" {:parameters {:path specs/starship-id}}
    ["" {:get {:handler (find-by-id app-config)
               :swagger {:produces [starships-find-by-id-api-version]}}
         :delete {:handler (delete app-config)
                  :swagger {:produces [starships-delete-api-version]}}
         :patch {:handler (modify app-config)
                 :parameters {:body specs/modify}
                 :swagger {:produces [starships-modify-api-version]}}}]]])
