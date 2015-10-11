(ns example
  (:require
   [ch.deepimpact.flowgic.graph :as g]
   [ch.deepimpact.flowgic.meta :as m]
   [ch.deepimpact.flowgic :as f]
   [ch.deepimpact.flowgic.core :as c]

   [plumbing.core :refer (fnk sum ?> ?>> defnk)]
   [rhizome.viz :refer :all]
   ))
(defn example-fn [{:keys [a b c] :as data-map}]
  (println a b c)
  (println data-map))

(example-fn {:a 1 :b 2 :c 3})

(defnk example-fnk [a b c :as data-map]
  (println a b c)
  (println data-map)
  )
(example-fnk {:a 1 :b 2 :c 3})

(defnk a [] {})
(defnk b [] {})
(defnk c [] {})
(defnk d [] {})

(g/view [(f/continue (defnk a [:as map] nil))
         (f/continue #'b)
         (f/continue #'c)] :horizontal)

(g/view [
                  (f/>true? :other-true? (f/exit #'d))

         (f/continue #'a)

         (f/>true? :is-sth-true? (f/exit #'b))
         (f/continue #'c)] :horizontal)

(g/view [(f/continue #'a)
         (f/exit #'b)] :horizontal)

(def cont (f/continue (defnk c [:as map])))
(= [:continue {:a 1}] (f/evaluate cont {:a 1}))
(def cont1 (f/continue (defnk c1 [:as map] {:cont1 "A"}) [:cont1]))
(= [:continue {:a 1 :cont1 "A"}] (f/evaluate cont1 {:a 1}))


(def cont2 (f/continue (defnk c2 [:as map] {:cont2 "B"}) [:cont2] {:cont2-flag true}))
(= [:continue {:a 1 :cont2 "B" :cont2-flag true}] (f/evaluate cont2 {:a 1}))


(def step-exit (f/exit (defnk exit [:as map] {:res "EXIT"})))
(= [:exit {:res "EXIT"}] (f/evaluate step-exit {}))

(def logics [cont cont1
             [(f/true? :fn-to-locate-value-in-a-context
                       (f/continue (defnk on-true-fn [:as map]))
                       (f/continue (defnk on-false-fn [:as map]))
                       )
              (f/continue (defnk d1 [:as map]))
              (f/continue (defnk d2 [:as map]))]
             cont2])

(g/view (f/true? :fn-to-locate-value-in-a-context
                       (f/continue (defnk on-true-fn [:as map]) [] {:condition-was true})
                       (f/continue (defnk on-false-fn [:as map]) :condition-was false )
                       ) :horizontal)

(= [:continue {:initial-data "hello", :fn-to-locate-value-in-a-context 1, :condition-was false}]
   (f/evaluate (f/>not-empty? :fn-to-locate-value-in-a-context
                       (f/continue (defnk on-false-fn [:as map]) []{:condition-was false}))
               {:initial-data "hello" :fn-to-locate-value-in-a-context 1}))

(= [:continue {:initial-data "hello", :cont1 "A", :cont2 "B", :cont2-flag true}]
   (f/evaluate [cont cont1 cont2] {:initial-data "hello"}))

(= [:continue {:initial-data "hello", :fn-to-locate-value-in-a-context true, :condition-was true}]
   (f/evaluate (f/>true? :fn-to-locate-value-in-a-context
                        (f/continue (defnk on-true-fn [:as map]) [] {:condition-was true}))
               {:initial-data "hello" :fn-to-locate-value-in-a-context true}))

(= [:continue {:initial-data "hello", :fn-to-locate-value-in-a-context false, :condition-was false}]
   (f/evaluate (f/>false? :fn-to-locate-value-in-a-context
                        (f/continue (defnk on-false-fn [:as map]) [] {:condition-was false}))
               {:initial-data "hello" :fn-to-locate-value-in-a-context false}))
(def context {:a true :b false})
(def your-fn :a)
(def your-complex-fn #(= 2 (count (keys %))))
(= true (your-complex-fn context))
(keys context)


(= [:continue {:initial-data "hello"
               :rule-fn true
               :condition-was true}]
   (g/view (f/true? :rule-fn
                        (f/continue (defnk on-true-fn [:as map]) [] {:condition-was true})
                        (f/continue (defnk on-false-fn [:as map]) :condition-was false ))
               {:initial-data "hello"
                :rule-fn true}))


(= [:continue {:initial-data "hello"
               :rule-fn false
               :condition-was false}]
   (g/view (f/>false? :rule-fn
                        (f/continue (defnk on-false-fn [:as map]) [] {:condition-was false}))
               {:initial-data "hello" :rule-fn false}))


(= [:continue {:initial-data "hello"
               :rule-fn nil
               :condition-was true}]
   (g/view (f/empty? :rule-fn
                       (f/continue (defnk on-true-fn [:as map]) []{:condition-was true})
                       (f/continue (defnk on-false-fn [:as map]) []{:condition-was false})
                       )
               {:initial-data "hello" :rule-fn nil}))

(= [:continue {:initial-data "hello"
               :rule-fn nil
               :condition-was true}]
   (f/evaluate (f/>empty? :rule-fn
                       (f/continue (defnk on-true-fn [:as map]) []{:condition-was true}))
               {:initial-data "hello" :rule-fn nil}))

(= [:continue {:initial-data "hello"
               :rule-fn "not empty!"
               :condition-was false}]
   (f/evaluate (f/>not-empty? :rule-fn
                       (f/continue (defnk on-false-fn [:as map]) []{:condition-was false}))
               {:initial-data "hello" :rule-fn "not empty!"}))
