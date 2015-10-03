(ns ch.deepimpact.flowgic.core
 (:refer-clojure :exclude [merge]))

(defprotocol Evaluation
  (evaluate [_  context]))

(defprotocol Meta
  (meta-name [_]))

(extend-protocol Meta
  clojure.lang.Fn
  (meta-name [this]
     (let [m (meta  this)]
                  (str  (last (clojure.string/split  (str  (:ns  m)) #"\."))
                        "\n"
                        (:name m))))
  clojure.lang.Var
  (meta-name [this]
    (let [m (meta  this)]
         (str  (last (clojure.string/split  (str  (:ns  m)) #"\."))
               "\n"
               (:name m))))
  clojure.lang.Keyword
  (meta-name [this]
    (str  this))
  clojure.lang.PersistentVector
  (meta-name [this]
    nil)
  String
  (meta-name [this]
    (str  this)))

(extend-protocol Evaluation
  clojure.lang.PersistentVector
  (evaluate [rules flow-context]
    (let [rules (flatten rules)]
        (loop [a (first rules) n* (next rules) c flow-context]
          (let [[kcontinue res] (evaluate a c)]
            (if (and n* (= :continue kcontinue))
              (recur (first n*) (next n*) res)
              [kcontinue res]))))))


;; this is an API logic, it helps on api fn definition
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
;; and merge context in the result
(defrecord Merge [steps result-keys flags]
  Evaluation
  (evaluate [this context]
    (let [[k res] (evaluate steps context)]
      (if (= k :exit)
        [k res]
        [k (clojure.core/merge context (select-keys res result-keys) flags)])))
  Meta
  (meta-name [this]
       (meta-name (first this))))

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
