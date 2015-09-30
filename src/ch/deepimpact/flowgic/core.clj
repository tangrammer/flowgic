(ns ch.deepimpact.flowgic.core)

(defn add* [c k v]
    (if-let [e (get c k)]
      (assoc c k (conj e v))
      (assoc c k #{v})
      )
    )

(defn *mname [x]
  (if (keyword? x)
    x
    (keyword
     (or
      (-> x  :name )
      (-> x  meta :name )
      ;(-> (read-string  (pr-str x)) :name  )

      "nil"))))


(defprotocol Evaluation
  (evaluate [_  context])
  (relations [_ result b n])

  )




(defprotocol Meta
  (meta-name [_])
  )

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
    "p-vector")
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
              (relations v c  (or b1 b)  (or n1 n)))
            (add* result b (first rules)) (map #(vector % %2 %3 )
                        rules
                        (butlast (conj (seq rules) nil))
                        (next (conj  rules nil)))))
  )


(meta-name :+)
(comment "you can remove whenever you need"
  (butlast (conj (seq  [:a :b]) nil))
          (next (conj  [:a :b] nil))
          (or nil true))
