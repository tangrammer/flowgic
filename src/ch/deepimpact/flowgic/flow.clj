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

  logic/Meta
  (logic/meta-name [this]
    (let [m (meta  action-fn)]
      (str  (last (clojure.string/split  (str  (:ns  m)) #"\."))
            "\n"
            (:name m)))))

(defrecord Return [action-fn]
  logic/Evaluation
  (logic/evaluate [this context]
    [:exit (action-fn context)])
  logic/Meta
  (logic/meta-name [this]
    (let [m (meta  action-fn)]
      (str  (last (clojure.string/split  (str  (:ns  m)) #"\."))
            "\n"
            (:name m))))
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

(defmethod clojure.core/print-method Continuation
  [this ^java.io.Writer writer]
  (.write writer (str  (logic/meta-name this))))

(defmethod clojure.core/print-method Return
  [this ^java.io.Writer writer]
  (.write writer (str  (logic/meta-name this)))
  )
