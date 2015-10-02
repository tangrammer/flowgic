(ns ch.deepimpact.flowgic.core
  (:refer-clojure :exclude [merge])
  )

(defprotocol Evaluation
  (evaluate [_  context])
  (relations [_ result b n]))


(defprotocol Meta
  (meta-name [_])
  )


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


(defn *mname [x]
  (if (keyword? x)
    x
    (keyword
     (or
      (-> x  :name )
      (-> x  meta :name )
      ;(-> (read-string  (pr-str x)) :name  )
      "nil"))))


(extend-protocol Meta
  clojure.lang.Fn
  (meta-name [this ]
     (let [m (meta  this)]
                  (str  (last (clojure.string/split  (str  (:ns  m)) #"\."))
                        "\n"
                        (:name m))))
  clojure.lang.Var
  (meta-name [this ]
    (let [m (meta  this)]
         (str  (last (clojure.string/split  (str  (:ns  m)) #"\."))
               "\n"
               (:name m))))
  clojure.lang.Keyword
  (meta-name [this ]
    (str  this))
  clojure.lang.PersistentVector
  (meta-name [this ]
    nil)
  String
  (meta-name [this]
    (str  this))
  )

(extend-protocol Evaluation
  clojure.lang.PersistentVector
  (evaluate [rules flow-context]
    (let [rules (flatten rules)]
        (loop [a (first rules) n* (next rules) c flow-context]
          (let [[kcontinue res] (evaluate a c)]
            (if (and n* (= :continue kcontinue))
              (recur (first n*) (next n*) res)
              [kcontinue res])))))
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
  )

(defrecord APIFn [steps flow-context-fn api-key]
  Evaluation
  (evaluate [this initial-data]
    (let [initial-data (clojure.core/merge initial-data {:error-key api-key})
          context (clojure.core/merge (flow-context-fn initial-data) initial-data)]
      (last (evaluate steps context)))))

(defn api [api-key steps flow-context-fn]
  (APIFn. steps flow-context-fn api-key))

;; if you need to use an existent logic steps ....
;; given a sequence of steps, replace :just for :continue
(defrecord Merge [steps result-keys flags]
  Evaluation
  (evaluate [this context]
    (let [[k res] (evaluate steps context)]
      (if (= k :exit)
        [k res]
        [k (clojure.core/merge context (select-keys res result-keys) flags)])))
  (relations [this result b n]
    (relations steps result b n))

  Meta
  (meta-name [this]
       (meta-name (first this))))

;; maybe should it be renamed to `reuse` ?
(defn merge
  ([steps]
   (merge steps [] {}))
  ([steps result-keys]
   (merge steps result-keys {}))
  ([steps result-keys flags]
   (let [last-step (peek steps)
         r-k (into (:result-keys last-step) result-keys)
         r-f  (clojure.core/merge (:flags last-step) flags)
         last-step-mod (assoc last-step :result-keys r-k :flags r-f)]
     (Merge. (-> steps pop (conj last-step-mod)) r-k r-f))))
