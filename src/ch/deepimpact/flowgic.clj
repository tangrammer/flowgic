(ns ch.deepimpact.flowgic
  (:require [ch.deepimpact.flowgic.core :as core])
  (:import [ch.deepimpact.flowgic.core Continuation Return Merge Controller Rule])
  (:refer-clojure :exclude [true? empty? merge]))

;; main protocol fn
(defn evaluate [this context]
  (core/evaluate this context))

;; this is an API logic, it helps on api fn definition
(defn controller [api-key steps context-fn]
  (Controller. steps context-fn api-key))

;; if you need to use an existent logic steps ....
;; given a sequence of steps, replace :just for :continue
;; and merge context in the result
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


;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;; FLOWS ;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

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

;; circuit-breaker,
;; similar to exception but beahviour&data oriented
(defn exit
  [action-fn]
  (Return. action-fn))


;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;; RULES ;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn true?
  ([location-value-fn  true-fn false-fn]
   (Rule. :true? location-value-fn identity
          {true (with-meta true-fn {:rule-val true})
           false (with-meta false-fn {:rule-val false})})))

(defn >true?
  [location-value-fn true-fn]
  (Rule. :>true? location-value-fn identity
         {true (with-meta true-fn {:rule-val true})}))

(defn >false?
  [location-value-fn false-fn]
  (Rule. :>false? location-value-fn identity
         {false (with-meta false-fn {:rule-val false})}))

(defn empty?
  ([location-value-fn  true-fn false-fn]
   (Rule. :empty? location-value-fn nil?
          {true (with-meta true-fn {:rule-val true})
           false (with-meta false-fn {:rule-val false})})))

(defn >empty?
  [location-value-fn  true-fn]
  (Rule. :>empty? location-value-fn nil?
         {true (with-meta true-fn {:rule-val true})}))

(defn >not-empty?
  [location-value-fn  false-fn]
  (Rule. :>not-empty? location-value-fn nil?
         {false (with-meta false-fn {:rule-val false})}))
