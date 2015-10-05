(ns ch.deepimpact.flowgic.flow
  (:require [ch.deepimpact.flowgic.core :as logic]))

(defrecord Continuation [add-context? action-fn result-keys flags]
  logic/Evaluation
  (logic/evaluate [this context]
    (let [res (action-fn context)
          e (if add-context?
              (merge context (select-keys res result-keys) flags)
              (merge res flags))]
      [:continue e]))

  )

(defrecord Return [action-fn]
  logic/Evaluation
  (logic/evaluate [this context]
    [:exit (action-fn context)])
  )

(defn continue
  ([action-fn]
   (continue action-fn [] {}))
  ([action-fn result-keys]
   (continue action-fn result-keys {}))
  ([action-fn result-keys flags]
   (Continuation. true action-fn result-keys flags)))

;; doesn't merge with initial data the result
(defn just
  ([action-fn]
   (just action-fn [] {}))
  ([action-fn result-keys]
   (just action-fn result-keys {}))
  ([action-fn result-keys flags]
   (Continuation. false action-fn result-keys flags)))

;; short-circuit, similar to exception but beahviour&data oriented
(defn exit
  [action-fn]
  (Return. action-fn))
