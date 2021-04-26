(ns startrek.base.api.starship.mapper
  {:author ["David Harrigan"]}
  (:require
   [com.rpl.specter :refer [declarepath providepath if-path compact setval MAP-VALS NONE STAY]]))

(set! *warn-on-reflection* true)

(declare DEEP-MAP-VALS)
(declarepath DEEP-MAP-VALS)
(providepath DEEP-MAP-VALS (if-path map? [(compact MAP-VALS) DEEP-MAP-VALS] STAY))

(defn request->create
  [request]
  (let [{{{:keys [captain affiliation launched class registry image]} :body} :parameters} request]
    {:captain captain
     :affiliation affiliation
     :launched launched
     :class class
     :registry registry
     :image image}))

(defn request->delete
  [request]
  (let [{{{:keys [id]} :path} :parameters} request]
    {:id id}))

(defn request->find-by-id
  [request]
  (let [{{{:keys [id]} :path} :parameters} request]
    {:id id}))

(defn request->modify
  [request]
  (let [{{{:keys [id]} :path} :parameters} request
        {{{:keys [captain affiliation launched class registry image]} :body} :parameters} request
        starship {:id id
                  :captain captain
                  :affiliation affiliation
                  :launched launched
                  :class class
                  :registry registry
                  :image image}]
    (setval [DEEP-MAP-VALS nil?] NONE starship)))

(defn request->search
  [request]
  (let [{{{:keys [id captain affiliation launched class registry image]} :query} :parameters} request
        starship {:id id
                  :captain captain
                  :affiliation affiliation
                  :launched launched
                  :class class
                  :registry registry
                  :image image}]
    (setval [DEEP-MAP-VALS nil?] NONE starship)))
