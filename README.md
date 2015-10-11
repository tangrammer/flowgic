# Introduction 


**`flowgic` is a clojure DSL to describe the internal logic flow of controller like fns.**   

Controller-fns are those that play a *Controller Role*, managing different services, inputs, outputs, steps, decisions and rules.   
In this complected scenario, our understanding decreases exponentially. `flowgic` tries to improve the readability and understading of this logic flows 

    [ch.deepimpact/flowgic "0.1.0"]


###Goals

* the code becomes clearer :)
* the code is so clear that task's communication between devs and clients or project managers is improved :)
* `flowgic` code can be easily parsed and analysed to generate dynamic [flow diagrams](https://cloud.githubusercontent.com/assets/731829/10277888/8a5bf848-6b59-11e5-96de-1b67fab4981b.png)
* you could logically derive wich parts/fns need to be tested from the others that dont' need. 
* is easy to add any middleware due as you always work with fns 


### this lib can be useful if...  
* your logic flow is a [complex and nested one](https://cloud.githubusercontent.com/assets/731829/10277888/8a5bf848-6b59-11e5-96de-1b67fab4981b.png)
* your controller-fns are tricky to understand, even by the author after a few days [example](https://gist.github.com/tangrammer/b8fc6687f051ab059ac2#file-old_api-clj)
* your fns must receive a map and return a map (yep! like a ring handler). Of course you could always adapt your *controller-fns* to this great pattern => [Prismatic/Graph](https://github.com/Prismatic/plumbing#graph-the-functional-swiss-army-knife) , [Prismatic/fnk](https://github.com/Prismatic/plumbing#fnk) 


###`prismatic/defnk` and `prismatic/fnk`
Starting here the example code will use `prismatic/defnk` and `prismatic/fnk` to simplify working with fns that receive a map of keys/values. it's not required to use these macros but we think they are great syntatic sugar focused in clarify too.


```clojure
;; example of clojure fn that receives a map and you are interested in 3 keys of this map
(defn example-fn [{:keys [a b c] :as data-map}]
  (println a b c)
  (println data-map))

(example-fn {:a 1 :b 2 :c 3})

```
```clojure
;; the same but using prismatic/defnk
(defnk example-fnk [a b c :as data-map]
  (println a b c)
  (println data-map)
  )
(example-fnk {:a 1 :b 2 :c 3})
```

#Concepts
###`flowgic.core/Logic` Protocol
Agreeing that a `controller-fn` is a **sequence of logics to be evaluated** having a context, we can derive following `Logic` protocol

```clojure
(defprotocol flowgic.core/Logic
  (evaluate [this  ^clojure.lang.PersistentArrayMap context]))
```

In other words and to clarify a bit more: **A `flowgic.core/Logic` needs a logical-context to be able to solve an evaluation**. As you can realise, the `context` is a common clojure map.


## `flowgic.core/Logic`  Impls 

```clojure
(require '[ch.deepimpact.flowgic :as f]) 
;; f stands for flowgic in this doc
```
### `core/Continuation` 
A `Continuation` means: after a Logic/evaluation, flow must continue on next logic (except core/Return, see next impl)


Typically, `Continuation` is represented as a intermediate box between 2 others (other of same or different type). In following graph examples: `a`, `b` and `c` represent a Continuation in a logic flow

<img width="400" alt="screen shot 2015-10-09 at 11 35 58" src="https://cloud.githubusercontent.com/assets/731829/10390590/f3a468a8-6e79-11e5-91b9-c3f121ed13eb.png">
<img width="400" alt="screen shot 2015-10-09 at 11 39 32" src="https://cloud.githubusercontent.com/assets/731829/10390672/74a5f21e-6e7a-11e5-89ce-a4152b4f771e.png">

**`f/continue`: Three ways to create a core/Continuation**


```clojure
;; if you don't need to select anything of the result or add any static data to the result
(def cont (f/continue (defnk a [])))
(= [:continue {:a 1}] 
   (f/evaluate cont {:a 1}))
          
;;if you need to select some values from the result
  (def cont1 (f/continue (fn [map] {:cont1 "A"}) [:cont1])) 
  (= [:continue {:a 1 :cont1 "A"}] 
     (f/evaluate cont1 {:a 1}))

;;if you need to select some values and add some others  
(def cont2 (f/continue (defnk c2 [:as map] {:cont2 "B"}) [:cont2] {:cont2-flag true}))
(= [:continue {:a 1 :cont2 "B" :cont2-flag true}] 
   (f/evaluate cont2 {:a 1}))
```


###  `core/Return `
*`Return` is the break circuit in a flow.*

A Return means: after logic evaluation, flow must end returning the fn return value
The behaviour is similar to exception but just sending its own result to the end.   
In the following picture, red boxes represents core/Return

<img width="400" alt="screen shot 2015-10-09 at 11 54 27" src="https://cloud.githubusercontent.com/assets/731829/10391039/85aef5f4-6e7c-11e5-8fac-680107702e7a.png">

**`f/exit`: creating  a new `Return`**

```clojure  
(def step-exit (f/exit (fn [map] {:res "EXIT"})))
(= [:exit {:res "EXIT"}] 
   (f/evaluate step-exit {}))              
```

### `clojure.lang.PersistentVector` 
**Clojure vector is used as the logic flow container.** It also can be nested.    

In `flowgic`, clojure vector means: evaluate the context with the first logic, then if result is a `Continuation` pass the result of first evaluation merged with the initial context to the second logic and repeat until the end (that it returns the context merged with all the continuation results).
If some logic is of Return type, then we return the result of Return.


```clojure  
(def logics [cont cont1 cont2])

(= [:continue {:initial-data "hello", 
               :cont1 "A", 
               :cont2 "B", 
               :cont2-flag true}]
   (f/evaluate logics {:initial-data "hello"}))
```
<img width="400" alt="screen shot 2015-10-09 at 11 59 43" src="https://cloud.githubusercontent.com/assets/731829/10391143/41e17ba2-6e7d-11e5-81f1-9e27f0d43b58.png">
   
**Nested declaration examples:**

```clojure
;; with another vector at the end
(def logics1 [cont cont1 cont2
             [(f/continue (defnk d [:as map]))
              (f/continue (defnk d1 [:as map]))
              (f/continue (defnk d2 [:as map]))]])

```
<img width="400" alt="screen shot 2015-10-09 at 12 08 12" src="https://cloud.githubusercontent.com/assets/731829/10391299/71621926-6e7e-11e5-847d-7ccaac322d8d.png">

```clojure
;; with another vector in third position
(def logics2 [cont cont1
             [(f/continue (defnk d [:as map]))
              (f/continue (defnk d1 [:as map]))
              (f/continue (defnk d2 [:as map]))]
             cont2])

```


Inserting the nested vector in other position

<img width="400" alt="screen shot 2015-10-09 at 12 09 15" src="https://cloud.githubusercontent.com/assets/731829/10391326/9647bc00-6e7e-11e5-9cd6-18120daeb6d1.png">

### `core/Rule`

A `Rule` means: flow will `evaluate` the provided `Logic` if matching condition on the result of `your-fn-rule` with current `context`. On the contrary evaluate next `container` logic. 

*Simple example of "the result of calling `(your-fn-rule context)`*"

```clojure
(def context {:a true :b false})
(def your-fn-rule :a)
(= true (your-fn-rule context))

;; of course you can use complex fns instead of keyworks, 
;; but for the sake of simplicity we will use keywords in following examples

(def your-complex-fn #(= 2 (count (keys %))))
(= true (your-complex-fn context))

```


In flow diagrams, rules are commonly represented with diamond shapes.

<img width="400" alt="screen shot 2015-10-09 at 11 54 27" src="https://cloud.githubusercontent.com/assets/731829/10391039/85aef5f4-6e7c-11e5-8fac-680107702e7a.png">

### Rules included in `flowgic`
Although you can easily create your own `Rule`, this lib currently provides following ones:

`f/true?` `f/>true?` `f/>false?` `f/emtpy?`  `f/>emtpy?` `f/>not-emtpy?`


### `(f/true? fn-rule logic-on-true logic-on-false)`

You want to specify what ought to happen on true and on false your fn-rule result.

<img width="400" alt="screen shot 2015-10-11 at 21 54 44" src="https://cloud.githubusercontent.com/assets/731829/10419577/04bafa2c-707c-11e5-9de8-316692a530f5.png">

```clojure  
(= [:continue {:initial-data "hello"
               :rule-fn true
               :condition-was true}]
   (f/evaluate (f/true? :rule-fn
                        (f/continue (defnk on-true-fn [:as map]) [] {:condition-was true})
                        (f/continue (defnk on-false-fn [:as map]) :condition-was false ))
               {:initial-data "hello"
                :rule-fn true}))

```

### `(f/>true? rule-fn logic-on-true)`

The same as previous,but if `false` you always want to continue next container `Logic` 

<img width="400" alt="screen shot 2015-10-11 at 22 18 55" src="https://cloud.githubusercontent.com/assets/731829/10419587/44744f6a-707c-11e5-9682-0abff578c134.png">

```clojure  
(= [:continue {:initial-data "hello"
               :rule-fn true
               :condition-was true}]
   (f/evaluate (f/>true? :rule-fn
                        (f/continue (defnk on-true-fn [:as map]) [] {:condition-was true}))
               {:initial-data "hello" :rule-fn true}))
```




### `flowgic/>false?`
`(f/>false? fn-passing-current-context logic-on-false)`

The same as previous but, if `true` you always want to continue next container `Logic`.


<img width="400" alt="screen shot 2015-10-12 at 00 59 02" src="https://cloud.githubusercontent.com/assets/731829/10419595/737f3ea0-707c-11e5-985d-98739946535b.png">

```clojure  
(= [:continue {:initial-data "hello"
               :rule-fn false
               :condition-was false}]
   (f/evaluate (f/>false? :rule-fn
                        (f/continue (defnk on-false-fn [:as map]) [] {:condition-was false}))
               {:initial-data "hello" :rule-fn false}))
```

### `flowgic/empty? flowgic/>empty?  flowgic/>not-empty? `
the same as `flowgic/true? flowgic/>true?  flowgic/>false? ` but matching nil instead of true/false

```clojure
(= [:continue {:initial-data "hello"
               :rule-fn nil
               :condition-was true}]
   (f/evaluate (f/empty? :rule-fn
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
```



##TODO
explain following types (flowgit/Evaluation impls)

more   
   
* core/Merge => f/merge
* core/ControllerFn => f/controller-fn


* f/just


##More things interesting


###fn & DI
`flowgic` is really very influenced by [Prismatic/graph](link) and its Dependency Injection at the *function&args* level

###pipeline programming
yep, `flowgic` remains a bit similar to [pipeline programming](https://en.wikipedia.org/wiki/Pipeline_(software)) 

### similar to ring protocol, 
Yes, indeed I realised that it was the same as ring handler `(handler request)` but adding more data to the handler fn, something like `(#(evaluate (FlowgicImpl. data) %) request)` 



## Releases and Dependency Information

* Releases are published to TODO_LINK

* Latest stable release is TODO_LINK

* All released versions TODO_LINK

[Leiningen] dependency information:

    [ch.deepimpact/flowgic "0.1.0"]

[Maven] dependency information:

    <dependency>
      <groupId>ch.deepimpact</groupId>
      <artifactId>flowgic</artifactId>
      <version>0.1.0</version>
    </dependency>

[Leiningen]: http://leiningen.org/
[Maven]: http://maven.apache.org/



## Change Log

* Version 0.1.0
 * extracted Evaluation protocol from graph and meta protocols
* Version 0.1.1-SNAPSHOT
 * using one public namespace `flowgit` to get all functionality. Move the rest to `core`, `graph`, and `meta` 	



## Copyright and License

Copyright © 2015 DEEPIMPACT.ch

Distributed under the Eclipse Public License, the same as Clojure.
