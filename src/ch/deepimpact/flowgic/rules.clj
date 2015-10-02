(ns ch.deepimpact.flowgic.rules
  (:require [ch.deepimpact.flowgic.core :as logic])
  (:refer-clojure :exclude [update true? empty?]))


(defn full-boolean-mapping? [poss]
  (= (apply hash-set (keys poss)) #{true false}))


(defn else-option [poss]
  (if (=  1(count poss)  )
    (let [f (first (first poss))]
      (if (= Boolean (type f))
        (if f "false" "true")
        "else"))))

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
            (->  (if (full-boolean-mapping?  possibilities)
                   (reduce (fn [c [_ v] ]
                             (logic/add* c this v)
                             ) result possibilities)
                   (logic/add* result this (with-meta n {:rule-val (else-option possibilities)})))
                 (logic/add* b this)
                 )
            possibilities))

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
