(ns ch.deepimpact.flowgic.flow
  (:require [ch.deepimpact.flowgic.core :as logic]
            [plumbing.core :refer (?>)]))

(defrecord Continuation [add-context? action-fn result-keys flags]
  logic/Evaluation
  (logic/evaluate [this context]
    (let [res (action-fn context)
          e (if add-context?
              (merge context (select-keys res result-keys) flags)
              (merge res flags))]
      [:continue e]))
  (logic/relations [this result b n]
    (->
     (logic/add* result this  n)


     (?> (do
;           (println "??? " (not= :empty? (:type b)) "--"(:type b) "--" (:location-value-fn b) (when (nil? (:type b)) [(logic/meta-name b)  (logic/meta-name this) (logic/meta-name n)] ))
           (not= :empty? (:type b))
           true
           )
         (logic/add*   b  this))




     ))
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
  (logic/relations [this result b n]
    (-> (logic/add* result b this)
        (logic/add* this :+)))
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
(= (type (continue identity)) Continuation)
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

;; if you need to use an existent logic steps ....
;; given a sequence of steps, replace :just for :continue
(defrecord Branch [steps result-keys flags]
  logic/Evaluation
  (logic/evaluate [this context]
    (let [[k res] (logic/evaluate steps context)]
      [k (merge context (select-keys res result-keys) flags)]))
  (logic/relations [this result b n]
    (logic/relations steps result b n))
  logic/Meta
  (logic/meta-name [this]
    (logic/meta-name steps)))

;; maybe should it be renamed to `reuse` ?
(defn branch
  ([steps]
   (branch steps [] {}))
  ([steps result-keys]
   (branch steps result-keys {}))
  ([steps result-keys flags]
   (let [last-step (peek steps)
         r-k (into (:result-keys last-step) result-keys)
         r-f  (merge (:flags last-step) flags)
         last-step-mod (assoc last-step :result-keys r-k :flags r-f)]
     (Branch. (-> steps pop (conj last-step-mod)) r-k r-f))))


#_(defmethod clojure.core/print-method Continuation
  [this ^java.io.Writer writer]
  (.write writer (str  (logic/meta-name this))))

#_(defmethod clojure.core/print-method Return
  [this ^java.io.Writer writer]
  (println (:action-fn this))
  (println (meta (:action-fn this)))
  (.write writer (str  (logic/meta-name this)))
)
