(ns ch.deep-impact.flowgic.core-test
  (:require
   [rhizome.viz :refer :all]
   [ch.deepimpact.flowgic.core :as logic]
   [plumbing.core :refer (fnk sum ?> ?>> defnk)]
   [ch.deepimpact.flowgic.rules :as rules]
   [clojure.pprint :refer (pprint)]
   [ch.deepimpact.flowgic.flow :as flow]
   [clojure.test :refer (deftest use-fixtures is)])
  (:import [ch.deepimpact.flowgic.rules Rule]
           [ch.deepimpact.flowgic.flow Return Continuation])
  )

(defnk res-a [a]
  {:res (str "hello res: " a)})
(defnk res-b [b]
  {:res (str "bye res: " b)})
(defnk res-c [c]
  {:res (str "another res: " c)})
(defnk res-d [d]
  {:res (str "another res: " d)})
(defnk res-e [e]
  {:res (str "another res: " e)})

(deftest ruling+merging
  ;; evaluate a vector
  (is (= [:continue {:a "a" :b "b" :res "hello res: a" :x true :z false}]
         (logic/evaluate
          (rules/>not-empty? :a
                             (logic/merge [(flow/just #'res-a [] {:x true})]
                                          [:res]
                                          {:z false}))
          {:a "a" :b "b"})))
  )

(deftest merging

  ;; -----------------------------
  ;; evaluate a branch with vector
  ;; -----------------------------
  (is (= [:continue {:a "a" :b "b" :res "hello res: a"}]
         (logic/evaluate (logic/merge
                          [(flow/just #'res-a)]
                          [:res] {})
                         {:a "a" :b "b"})))

  (is (= [:continue {:a "a" :b "b"}]
         (logic/evaluate (logic/merge
                          [(flow/just res-a)]
                          [] {})
                         {:a "a" :b "b"})))

  (is (= [:continue {:a "a" :b "b" :res "hello res: a" :z 5 :y true}]
         (logic/evaluate (logic/merge
                          [(flow/continue res-a
                                          []
                                          {:z 5})]
                          [:res] {:y true})
                         {:a "a" :b "b"}))))

(deftest steping
  ;; evaluate a vector
  (is (= [:continue { :res "hello res: a"}]
         (logic/evaluate [(flow/just res-a)]
                         {:a "a" :b "b"})))
  (is (= [:exit { :res "hello res: a"}]
         (logic/evaluate [(flow/exit res-a)]
                         {:a "a" :b "b"})))

  (is (= [:continue {:a "a" :b "b"}]
         (logic/evaluate [(flow/continue res-a)]
                         {:a "a" :b "b"})))

  (is (= [:continue {:a "a" :b "b" :res "hello res: a"}]
         (logic/evaluate [(flow/continue res-a [:res])]
                         {:a "a" :b "b"})))

  (is (= [:continue {:a "a" :b "b" :res "hello res: a" :x true}]
         (logic/evaluate [(flow/continue res-a [:res] {:x true})]
                         {:a "a" :b "b"})))
  )


(def g (let [g (logic/relations
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
               :node->descriptor (fn [n*] (let [n (logic/meta-name n*)]
                                           {:label n
                                            :color (if (= Return (type n* ))
                                                     "red"
                                                     "black"
                                                     )
                                            :style :filled
                                            :bgcolor (if (= Return (type n* ))
                                                     "red"
                                                     "black"
                                                     )
                                            :fillcolor (condp = (type n*)
                                                     Return "red"
                                                     Rule "#81F7F3"
                                                     Continuation "#F8ECE0"
                                                     "none"
                                                     )
                                            :shape (if (or (= n ":+") (= n ":*")) "circle"
                                                      (if (= Rule (type n*))
                                                        "diamond"
                                                        (if (= Return (type n* ))
                                                          "invhouse"
                                                          "box"
                                                                )))}))
               :edge->descriptor (fn [e1 e2 ] (let [e*   (when (= Rule (type e1))
                                                          (str (-> e2 meta :rule-val)))]
                                               {:label  e*}))


               )

   g

   ))
