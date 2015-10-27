(ns ch.deepimpact.flowgic.graph
  (:require [plumbing.core :refer (?>)]
            [ch.deepimpact.flowgic.meta :as m]
            [ch.deepimpact.flowgic.core :as c*]
            [rhizome.viz :as viz])

  (:import [ch.deepimpact.flowgic.core Merge Continuation Return Rule]
           [ch.deepimpact.flowgic.meta Vertice]))

;; this ns should live in another lib
(defprotocol Graph
  (relations [_ result b n])
  (color [_])
  )

(def END (Vertice. :END))
(def START (Vertice. :START))
;; this fn should be moved to protocol and lets polymorphism do the rest
(defn add* [c k v]
  (if (and (not= clojure.lang.PersistentVector (type  k)) (not= clojure.lang.PersistentVector (type  v)))
    (if-let [e (get c k)]
      (if-let [v* (get e v)]
        (let [e (disj e v)]
          (if (= v END) ;; end step cant' contain metadata :+ . this should be improved
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
  (color [_]
    "black")
  Rule
  (relations [this result b n]
    (reduce (fn [c [k v]]
              (relations v c this n))
            (->  (if (full-boolean-mapping? (:possibilities this))
                   (reduce (fn [c [_ v] ]
                             (add* c this v)
                             ) result (:possibilities this))
                   (add* result this  (with-meta n {:rule-val (else-option (:possibilities this))}) ))
                 (add* b this)
                 )
            (:possibilities this)))
  (color [_]
    "black")
  Continuation
  (relations [this result b n]
    (->
     (add* result this  n)
     (?> (not (full-boolean-mapping? (:possibilities b)))
         (add* b this))))
  (color [_]
    "black")
  Return
  (relations [this result b n]
    (-> (add* result b this)
        (add* this END)))
  (color [_]
      "red")
  Merge
  (relations [this result b n]
    (relations (:steps this) result b n))
  (color [_]
    "black")
  clojure.lang.Keyword
  (color [_]
    "black")
  Vertice
  (relations [this result b n]
    nil)
  (color [_]
    "black")

  )

(defn view [logic-container proyection]
  (let [g (relations logic-container {START #{} END #{}} START END)]
   (viz/view-graph (keys g) g
                    :vertical? (= proyection :vertical)
                    :options { :resolution 72 :bgcolor "#C6CFD532"}
                    :node->descriptor (fn [n*] (let [n (m/meta-name n*)]
                                                {:label n
                                                 :color (color n*)
                                                 :style :filled
                                                 :bgcolor (if (= "Return" (.getSimpleName (type n* )))
                                                            "red"
                                                            "black")
                                                 :fillcolor (condp = (.getSimpleName(type n*))
                                                              "Return" "red"
                                                              "Rule" "#81F7F3"
                                                              "Continuation" "#F8ECE0"
                                                              "none"
                                                              )
                                                 :shape (if (or (= n*  END) (= n*  START)) "none"
                                                            (if (= "Rule" (.getSimpleName(type n*)))
                                                              "diamond"
                                                              (if (= "Return" (.getSimpleName(type n* )))
                                                                (if (= proyection :vertical) "invhouse" "cds")
                                                                "box"
                                                                )))}))
                    :edge->descriptor (fn [e1 e2 ] (let [e*   (when (= "Rule" (.getSimpleName (type e1)))
                                                               (str (-> e2 meta :rule-val)))]
                                                    {:label  e*}))


                    )))
