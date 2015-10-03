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


  logic/Meta
  (logic/meta-name [this]
    (str (name type) "\n" (logic/meta-name location-value-fn)) )
  )

(defn true?
  ([location-value-fn  true-fn false-fn]
   (Rule. :true? location-value-fn identity {true (with-meta true-fn {:rule-val true}) false (with-meta false-fn {:rule-val false})})))

(defn >true?
  [location-value-fn true-fn]
  (Rule. :>true? location-value-fn identity {true (with-meta true-fn {:rule-val true})}))

(defn >false?
  [location-value-fn false-fn]
  (Rule. :>false? location-value-fn identity {false (with-meta false-fn {:rule-val false})}))

(defn empty?
  ([location-value-fn  true-fn false-fn]
   (Rule. :empty? location-value-fn nil? {true (with-meta true-fn {:rule-val true}) false (with-meta false-fn {:rule-val false})})))

(defn >empty?
  [location-value-fn  true-fn]
  (Rule. :>empty? location-value-fn nil? {true (with-meta true-fn {:rule-val true})}))

(defn >not-empty?
  [ location-value-fn  false-fn]
  (Rule. :>not-empty? location-value-fn nil? {false (with-meta false-fn {:rule-val false})}))

(defmethod clojure.core/print-method Rule
  [this ^java.io.Writer writer]
  (.write writer (str  (logic/meta-name this)))
  )
