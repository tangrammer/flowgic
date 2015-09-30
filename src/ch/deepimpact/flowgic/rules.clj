(ns ch.deepimpact.flowgic.rules
  (:require [ch.deepimpact.flowgic.core :as logic])
  (:refer-clojure :exclude [update true? empty?]))

(defrecord Rule [type location-value-fn evaluation-fn possibilities]
  logic/Evaluation
  (logic/evaluate [this context]
    (let [value (location-value-fn context)
          evaluation (evaluation-fn value)]
      (if ((complement nil?) evaluation)
        (let [action-fn  (get possibilities evaluation)]
          (if ((complement nil?) action-fn)
            (logic/evaluate action-fn context)
            [:continue context]))
        [:continue context])))
  (logic/relations [this result b n]
   (reduce (fn [c [k v]]
                 (logic/relations v c this n))
           (do
             (println ">> type " (= type :empty?) "**" type "**" )
             (println [(logic/meta-name b) "*******"(logic/meta-name this) "*******"(logic/meta-name n)])
             #_(if (= type :empty?)
               result
               (logic/add* result this n))
             (logic/add* result this n)
             )
               possibilities)

    )
  logic/Meta
  (logic/meta-name [this]
     (str  (name type) "\n" (logic/meta-name location-value-fn)) )
  )

(defn true?
  ([location-value-fn  true-fn false-fn]
   (Rule. :true? location-value-fn identity {true true-fn false false-fn})))

(defn >true?
  [location-value-fn true-fn]
  (Rule. :>true? location-value-fn identity {true true-fn}))

(defn >false?
  [location-value-fn false-fn]
  (Rule. :>false? location-value-fn identity {false false-fn}))

(defn empty?
  ([location-value-fn  true-fn false-fn]
   (Rule. :empty? location-value-fn nil? {true true-fn false false-fn})))

(defn >empty?
  [location-value-fn  a]
  (Rule. :>empty? location-value-fn nil? {true a}))

(defn >not-empty?
  [ location-value-fn  a]
  (Rule. :>not-empty? location-value-fn nil? {false a}))

;; utils for drawing/introspecting rules
(defn- get-opts [this]
  (reduce (fn [c [_ v]] (str c (.getSimpleName (type v)))) "" (:possibilities this)) )

#_(defmethod clojure.core/print-method Rule
  [this ^java.io.Writer writer]
  (.write writer (str  (logic/meta-name this)))
)
