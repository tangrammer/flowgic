(ns ch.deep-impact.flowgic.core-test
  (:require
   [rhizome.viz :refer :all]
 ;  [posting-engine.stack-configurator.api.account :as account]
   [ch.deepimpact.flowgic.core :as logic]
   [plumbing.core :refer (fnk sum ?> ?>> defnk)]
   [ch.deepimpact.flowgic.rules :as rules]
;   [posting-engine.stack-configurator.api :as api]
   [ch.deepimpact.flowgic.flow :as flow]
   [clojure.test :refer (deftest use-fixtures is)]))

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

(deftest ruling+branching
  ;; evaluate a vector
  (is (= [:continue {:a "a" :b "b" :res "hello res: a" :x true :z false}]
         (logic/evaluate
          (rules/>not-empty? :a
                             (flow/branch [(flow/just #'res-a [] {:x true})]
                                          [:res]
                                          {:z false}))
          {:a "a" :b "b"})))
)

(deftest branching

  ;; -----------------------------
  ;; evaluate a branch with vector
  ;; -----------------------------
  (is (= [:continue {:a "a" :b "b" :res "hello res: a"}]
         (logic/evaluate (flow/branch
                          [(flow/just #'res-a)]
                          [:res] {})
                         {:a "a" :b "b"})))

  (is (= [:continue {:a "a" :b "b"}]
         (logic/evaluate (flow/branch
                          [(flow/just res-a)]
                          [] {})
                         {:a "a" :b "b"})))

  (is (= [:continue {:a "a" :b "b" :res "hello res: a" :z 5 :y true}]
                  (logic/evaluate (flow/branch
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
