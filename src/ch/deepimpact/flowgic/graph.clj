(ns ch.deepimpact.flowgic.graph
  (:require [plumbing.core :refer (?>)]
            [ch.deepimpact.flowgic.core :as c]
            [ch.deepimpact.flowgic.flow :as f]
            [ch.deepimpact.flowgic.rules :as ru])

  (:import
           [ch.deepimpact.flowgic.flow Continuation Return]
           [ch.deepimpact.flowgic.core Merge]
           [ch.deepimpact.flowgic.rules Rule]))

;; this ns should live in another lib


;; this fn should be moved to protocol and lets polymorphism do the rest
(defn add* [c k v]
  (if (and (not= clojure.lang.PersistentVector (type  k)) (not= clojure.lang.PersistentVector (type  v)))
    (if-let [e (get c k)]
      (if-let [v* (get e v)]
        (let [e (disj e v)]
          (if (= v :+) ;; end step cant' contain metadata :+ . this should be improved
            (assoc c k (conj e v))
            (assoc c k (conj e (vary-meta v* clojure.core/merge (meta v))))))
        (assoc c k (conj e v)))
      (assoc c k #{v}))
    c))

(defn full-boolean-mapping? [poss]
  (= (apply hash-set (keys poss)) #{true false}))

(defn else-option [poss]
  (if (=  1(count poss)  )
    (let [f (first (first poss))]
      (if (= Boolean (type f))
        (if f "false" "true")
        "else"))))

(defprotocol Graph
  (relations [_ result b n])
  )

(extend-protocol Graph
  clojure.lang.PersistentVector
  (relations [rules result b n]
    (reduce (fn [c [v b1 n1]]
              (if b1
                (relations v c  (or b1 b)  (or n1 n))
                (relations (with-meta  v (meta rules)) c  (or b1 b)  (or n1 n))
                ))
            #_(if  (= clojure.lang.PersistentVector (type b))
              result
            (add* result b (first rules))  )
            (add* result b (first rules))
            (map #(vector % %2 %3 )
                        rules
                        (butlast (conj (seq rules) nil))
                        (next (conj  rules nil)))))
  Rule
  (relations [this result b n]
    (reduce (fn [c [k v]]
              (relations v c this n))
            (->  (if (full-boolean-mapping? (:possibilities this))
                   (reduce (fn [c [_ v] ]
                             (add* c this v)
                             ) result (:possibilities this))
                   (add* result this (with-meta n {:rule-val (else-option (:possibilities this))})))
                 (add* b this)
                 )
            (:possibilities this)))
  Continuation
  (relations [this result b n]
    (->
     (add* result this  n)
     (?> (not (full-boolean-mapping? (:possibilities b)))
         (add* b this))))
  Return
  (relations [this result b n]
    (-> (add* result b this)
        (add* this :+)))
  Merge
  (relations [this result b n]
    (relations (:steps this) result b n)))
