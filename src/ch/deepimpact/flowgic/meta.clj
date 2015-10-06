(ns ch.deepimpact.flowgic.meta
  (:require [ch.deepimpact.flowgic.core :as core])
  (:import [ch.deepimpact.flowgic.core Continuation Return Merge Rule])
  (:refer-clojure :exclude [meta]))

(defprotocol Meta
  (meta-name [_]))


(defmethod clojure.core/print-method Continuation
  [this ^java.io.Writer writer]
  (.write writer (str  (meta-name this))))

(defmethod clojure.core/print-method Return
  [this ^java.io.Writer writer]
  (.write writer (str  (meta-name this)))
  )


(defmethod clojure.core/print-method Rule
  [this ^java.io.Writer writer]
  (.write writer (str  (meta-name this)))
  )

(extend-protocol Meta
  Merge
  (meta-name [this]
    (meta-name (first this)))
  Continuation
  (meta-name [this]
    (let [m (clojure.core/meta  (:action-fn this))]
      (str  (last (clojure.string/split  (str  (:ns  m)) #"\."))
            (when (:ns m) "\n")
            (:name m))
      (:name m)
      ))
  Return
  (meta-name [this]
    (let [m (clojure.core/meta  (:action-fn this))]
      (str  (last (clojure.string/split  (str  (:ns  m)) #"\."))
            (when (:ns m) "\n")
            (:name m))
      (:name m)))

  Rule
  (meta-name [this]
    (str (name (:type this)) "\n" (meta-name (:location-value-fn this))) )


  clojure.lang.Fn
  (meta-name [this]
    (let [m (clojure.core/meta  this)]
      (str  (last (clojure.string/split  (str  (:ns  m)) #"\."))
            (when (:ns m) "\n")
            (:name m))
      (:name m)
      ))
  clojure.lang.Var
  (meta-name [this]
    (let [m (clojure.core/meta  this)]
      (str  (last (clojure.string/split  (str  (:ns  m)) #"\."))
            "\n"
            (:name m))))
  clojure.lang.Keyword
  (meta-name [this]
    (str  (name this)))
  clojure.lang.PersistentVector
  (meta-name [this]
    nil)
  String
  (meta-name [this]
    (str  this))
  )
