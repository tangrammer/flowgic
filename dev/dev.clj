(ns dev
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
  (:require
   [clojure.java.io :as io]
   [clojure.java.javadoc :refer [javadoc]]
   [clojure.pprint :refer [pprint]]
   [clojure.reflect :refer [reflect]]
   [clojure.repl :refer [apropos dir doc find-doc pst source]]
   [clojure.set :as set]
   [clojure.string :as str]
   [clojure.test :as test]
   [clojure.tools.namespace.repl :refer [refresh refresh-all]]
   [ch.deepimpact.flowgic.core]


   [ch.deepimpact.flowgic.meta :as flowgic.meta]
   [ch.deepimpact.flowgic :as flowgic]
   [ch.deepimpact.flowgic.graph :as flowgic.graph]
   [plumbing.core :refer (fnk)]

   [rhizome.viz :refer :all]))

(def system
  "A Var containing an object representing the application under
  development."
  nil)

(defn init
  "Creates and initializes the system under development in the Var
  #'system."
  []
  ;; TODO
  )

(defn start
  "Starts the system running, updates the Var #'system."
  []
  ;; TODO
  )

(defn stop
  "Stops the system if it is currently running, updates the Var
  #'system."
  []
  ;; TODO
  )

(defn go
  "Initializes and starts the system running."
  []
  (init)
  (start)
  :ready)

(defn reset
  "Stops the system, reloads modified source files, and restarts it."
  []
  (stop)
  (refresh :after `go))

#_(def g (let [g (gr/relations
                [(rules/>true? :user
                               (rules/true? :x
                                            [(flow/continue (with-meta identity {:name "A"}))
                                             (flow/continue (with-meta identity {:name "B"}))
                                             (flow/exit (with-meta identity {:name "C"}))]
                                            [(flow/continue (with-meta identity {:name "Y"}))
                                             (flow/continue (with-meta identity {:name "X"}))])

                               )
                 (flow/continue (with-meta identity {:name "AL"}))]
                {:+ #{} :* #{}} :* :+)]
                                        ;  (pprint g)
         (view-graph (keys g) g
                     :vertical? true
                     :options { :resolution 72 :bgcolor "#C6CFD532"}
                     :node->descriptor (fn [n*] (let [n (met/meta-name n*)]
                                                 {:label n
                                                  :color (if (= "Return" (.getSimpleName (type n* )))
                                                           "red"
                                                           "black"
                                                           )
                                                  :style :filled
                                                  :bgcolor (if (= "Return" (.getSimpleName (type n* )))
                                                             "red"
                                                             "black")
                                                  :fillcolor (condp = (.getSimpleName(type n*))
                                                               "Return" "red"
                                                               "Rule" "#81F7F3"
                                                               "Continuation" "#F8ECE0"
                                                               "none"
                                                               )
                                                  :shape (if (or (= n ":+") (= n ":*")) "circle"
                                                             (if (= "Rule" (.getSimpleName(type n*)))
                                                               "diamond"
                                                               (if (= "Return" (.getSimpleName(type n* )))
                                                                 "invhouse"
                                                                 "box"
                                                                 )))}))
                     :edge->descriptor (fn [e1 e2 ] (let [e*   (when (= "Rule" (.getSimpleName (type e1)))
                                                                (str (-> e2 meta :rule-val)))]
                                                     {:label  e*}))


                     )

         g

         ))
(comment
  README

  (require '[ch.deepimpact.flowgic :as flowgic])
  (require '[plumbing.core :refer (fnk)])

  (def steps [(flowgic/continue (fn [{:keys [input]}] {:res1 (inc input)}) )
              (flowgic/continue (fn [{:keys [input]}] {:res2 (+ input (inc res1))}))
              (flowgic/continue (fn [{:keys [input]}] {:res3 3}))])
  (= {:res 3} (flowgic/evaluate steps {:input 0}))

  )

#_(let [g(flowgic.graph/relations
        [(flowgic/continue (with-meta (fnk []) {:name "A"}) )
         (flowgic/continue (with-meta (fnk []) {:name "B"}))
;         (flowgic/just (with-meta (fnk []) {:name "C"}))
         ]
        {:+ #{} :* #{}} :* :+)]
           (view-graph (keys g) g
                     :vertical? true
                     :options { :resolution 72 :bgcolor "#C6CFD532"}
                     :node->descriptor (fn [n*] (let [n (flowgic.meta/meta-name n*)]
                                                 {:label n
                                                  :color (if (= "Return" (.getSimpleName (type n* )))
                                                           "red"
                                                           "black"
                                                           )
                                                  :style :filled
                                                  :bgcolor (if (= "Return" (.getSimpleName (type n* )))
                                                             "red"
                                                             "black")
                                                  :fillcolor (condp = (.getSimpleName(type n*))
                                                               "Return" "red"
                                                               "Rule" "#81F7F3"
                                                               "Continuation" "#F8ECE0"
                                                               "none"
                                                               )
                                                  :shape (if (or (= n ":+") (= n ":*")) "circle"
                                                             (if (= "Rule" (.getSimpleName(type n*)))
                                                               "diamond"
                                                               (if (= "Return" (.getSimpleName(type n* )))
                                                                 "invhouse"
                                                                 "box"
                                                                 )))}))
                     :edge->descriptor (fn [e1 e2 ] (let [e*   (when (= "Rule" (.getSimpleName (type e1)))
                                                                (str (-> e2 meta :rule-val)))]
                                                     {:label  e*}))


                     )
           g)
(def step (flowgic/continue (fn [map] {:res "0"})))

(def step1 (flowgic/continue (fn [map] {:res1 "1"}) [:res1]))

(def step2 (flowgic/continue (fn [map] {:res2 "2"}) [:res2] {:step2 true}))
(= [:continue {}] (flowgic/evaluate step {}))
(= [:continue {:res1 "1"}] (flowgic/evaluate step1 {}))
(= [:continue {:res2 "2" :step2 true}] (flowgic/evaluate step2 {}))

(def step-exit (flowgic/exit (fn [map] {:res "EXIT"})))
(= [:exit {:res "EXIT"}] (flowgic/evaluate step-exit {}))

(def steps [step1 step2])


(= [:continue {:initial-data "hello", :res1 "1", :res2 "2", :step2 true}]
   (flowgic/evaluate steps {:initial-data "hello"}))
