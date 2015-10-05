(ns ch.deepimpact.flowgic.core
 (:refer-clojure :exclude [merge]))

(defprotocol Evaluation
  (evaluate [_  context]))

(extend-protocol Evaluation
  clojure.lang.PersistentVector
  (evaluate [rules flow-context]
    (let [rules (flatten rules)]
        (loop [a (first rules) n* (next rules) c flow-context]
          (let [[kcontinue res] (evaluate a c)]
            (if (and n* (= :continue kcontinue))
              (recur (first n*) (next n*) res)
              [kcontinue res]))))))

(defrecord APIFn [steps flow-context-fn api-key]
  Evaluation
  (evaluate [this initial-data]
    (let [initial-data (clojure.core/merge initial-data {:error-key api-key})
          context (clojure.core/merge (flow-context-fn initial-data) initial-data)]
      (last (evaluate steps context)))))

(defrecord Merge [steps result-keys flags]
  Evaluation
  (evaluate [this context]
    (let [[k res] (evaluate steps context)]
      (if (= k :exit)
        [k res]
        [k (clojure.core/merge context (select-keys res result-keys) flags)]))))

(defrecord Continuation [add-context? action-fn result-keys flags]
  Evaluation
  (evaluate [this context]
    (let [res (action-fn context)
          e (if add-context?
              (clojure.core/merge context (select-keys res result-keys) flags)
              (clojure.core/merge res flags))]
      [:continue e])))

(defrecord Return [action-fn]
  Evaluation
  (evaluate [this context]
    [:exit (action-fn context)]))


(defrecord Rule [type location-value-fn evaluation-fn possibilities]
  Evaluation
  (evaluate [this context]
    (let [value (location-value-fn context)
          evaluation (evaluation-fn value)]
      (if ((complement nil?) evaluation)
        (let [action-fn  (get possibilities evaluation)]
          (if ((complement nil?) action-fn)
            (evaluate action-fn context)
            [:continue context]))
        [:continue context])))


  )
