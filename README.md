# Introduction 


**`flowgic` is a clojure DSL to describe the internal logic flow of controller like fns.**   

Controller-fns are those that play a *Controller Role*, managing different services, inputs, outputs, steps, decisions and rules.   
In this complected scenario, our understanding decreases exponentially. The `flowgic` DSL tries to improve the readability and understading of this logic flows 

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
* your fns must receive a map and return a map. Of course you could always adapt your *controller-fns* to this great pattern => [Prismatic/Graph](https://github.com/Prismatic/plumbing#graph-the-functional-swiss-army-knife) , [Prismatic/fnk](https://github.com/Prismatic/plumbing#fnk) 


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
###controller-fn
A `controller-fn` is a **sequence of logics to be evaluated** having a context.

Something like: `[logic1 logic2 logic3]`
and logic1, logic2, and logic3 implements flowgic.core/Logic

```clojure
(defprotocol flowgic.core/Logic
  (evaluate [this  ^clojure.lang.PersistentArrayMap context]))
```

In other words and to clarify a bit more: **A `flowgic/Logic` needs a logical-context to be able to solve an evaluation**


##flowgic.core/Logic impls
```clojure
(require '[ch.deepimpact.flowgic :as f])
```
### core/Continuation => flowgic/continue
A Continuation means: after a Logic/evaluation, flow must continue on next logic (except core/Return, see next impl)


Typically, Continuation is represented as a intermediate box between 2 others (other of same or different type). IN following graph examples, `b` represents a Continuation

<img width="400" alt="screen shot 2015-10-09 at 11 35 58" src="https://cloud.githubusercontent.com/assets/731829/10390590/f3a468a8-6e79-11e5-91b9-c3f121ed13eb.png">
<img width="400" alt="screen shot 2015-10-09 at 11 39 32" src="https://cloud.githubusercontent.com/assets/731829/10390672/74a5f21e-6e7a-11e5-89ce-a4152b4f771e.png">

**Three ways to create a core/Continuation**


```clojure
;; if you don't need to select anything of the result or add any static data to the result
(def cont (f/continue (defnk a [])))
(= [:continue {:a 1}] (f/evaluate cont {:a 1}))
          
;;if you need to select some values from the result
  (def cont1 (f/continue (fn [map] {:cont1 "A"}) [:cont1])) 
  (= [:continue {:a 1 :cont1 "A"}] (f/evaluate cont1 {:a 1}))

;;if you need to select some values and add some others  
(def cont2 (f/continue (defnk c2 [:as map] {:cont2 "B"}) [:cont2] {:cont2-flag true}))
(= [:continue {:a 1 :cont2 "B" :cont2-flag true}] (f/evaluate cont2 {:a 1}))
```


##  core/Return => flowgic/exit
*Return is the break circuit in a flow.*

A Return means: after logic evaluation, flow must end returning the fn return value
The behaviour is similar to exception but just sending its own result to the end.   
In the following picture, red boxes represents core/Return

<img width="400" alt="screen shot 2015-10-09 at 11 54 27" src="https://cloud.githubusercontent.com/assets/731829/10391039/85aef5f4-6e7c-11e5-8fac-680107702e7a.png">

```clojure  
(def step-exit (f/exit (fn [map] {:res "EXIT"})))
(= [:exit {:res "EXIT"}] (f/evaluate step-exit {}))              
```

### clojure.lang.PersistentVector => [ logic1 logic2 logic3]
**This is the logic flow container, and can be nested**.    
A clojure vector means: evaluate the context with the first logic, then if Continuation pass the res of first evaluation merged with the initial context to the second logic and repeat until the end (that it returns the context merged with all the continuation results).
If some logic is of Return type, then we return the result of Return.


```clojure  
(def logics [cont cont1 cont2])

(= [:continue {:initial-data "hello", :cont1 "A", :cont2 "B", :cont2-flag true}]
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


<img width="971" alt="screen shot 2015-10-09 at 12 09 15" src="https://cloud.githubusercontent.com/assets/731829/10391326/9647bc00-6e7e-11e5-9cd6-18120daeb6d1.png">


##TODO
explain following types (flowgit/Evaluation impls)

* core/Rule
 * flowgic/true?
 * flowgic/>true?
 * flowgic/>false?
 * flowgic/emtpy?
 * flowgic/>emtpy?
 * flowgic/>not-emtpy?

more   
   
* core/Merge => flowgic/merge
* core/APIFn => flowgic/api

and more

* flowgic/just


##More things interesting


###fn & DI
`flowgic` is really very influenced by [Prismatic/graph](link) and its Dependency Injection at the *function&args* level


yep, `flowgic` remains a bit similar to [pipeline programming](https://en.wikipedia.org/wiki/Pipeline_(software)) 

* similar to ring protocol, 
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
