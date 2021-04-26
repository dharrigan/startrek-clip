(ns startrek.base.api.starship.transformations
  {:author "David Harrigan"}
  (:require
   [clojure.set :as s]
   [com.rpl.specter :refer [declarepath providepath if-path compact setval MAP-VALS NONE STAY]]))

(set! *warn-on-reflection* true)

(declare DEEP-MAP-VALS)
(declarepath DEEP-MAP-VALS)
(providepath DEEP-MAP-VALS (if-path map? [(compact MAP-VALS) DEEP-MAP-VALS] STAY))

(def transform-starships
  (comp
   (map #(s/rename-keys % {:starship/affiliation :affiliation
                           :starship/captain :captain
                           :starship/class :class
                           :starship/created :created
                           :starship/image :image
                           :starship/launched :launched
                           :starship/registry :registry
                           :starship/uuid :id}))
   (map #(dissoc % :starship/starship_id))
   (map #(setval [DEEP-MAP-VALS nil?] NONE %))))

(defn create
  [uuid]
  {:id uuid})

(defn delete
  [uuid]
  (create uuid))

(defn search
  [starships]
  (into [] transform-starships starships))

(defn find-by-id
  [starship]
  (search [starship]))

(defn modify
  [starship]
  (search [starship]))
