# Introduction 


**`flowgic` is a clojure DSL to describe and [visualise](https://cloud.githubusercontent.com/assets/731829/10277888/8a5bf848-6b59-11e5-96de-1b67fab4981b.png) the internal logic flow of controller like fns.**

    [ch.deepimpact/flowgic "0.1.0"]

###Controller-fns
Controller-fns are those that play a *Controller Role*, managing different services, inputs, outputs, steps, decisions and rules. 
With so many conditions and vars our understanding of the behaviour of the fn decreases exponentially. 


###Goals

* the code becomes clearer :)
* the code is so clear that task's communication between devs and clients or project managers is improved :)
* `flowgic` code can be easily parsed and analysed to generate dynamic [flow diagrams](https://cloud.githubusercontent.com/assets/731829/10277888/8a5bf848-6b59-11e5-96de-1b67fab4981b.png)
* you can derive from flowgic code wich parts/fns need to be tested from the others that dont' need (usually you have to focus on actions instead of flow rules)
* is easy to add any middleware due as you always work with fns 


### this lib can be useful if...  
* your logic flow is a [complex and nested one](https://cloud.githubusercontent.com/assets/731829/10277888/8a5bf848-6b59-11e5-96de-1b67fab4981b.png)
* your controller-fns are tricky to understand, even by the author after a few days [example](https://gist.github.com/tangrammer/b8fc6687f051ab059ac2#file-old_api-clj)
* your fns receive a map and return a map. Of course you could always adapt your *controller-fns* to this great pattern => [Prismatic/fnk](https://github.com/Prismatic/plumbing#fnk) 

#Let's say that ...
A controller-fn is a **sequence of logics to be evaluated** having a context.

Something like: `[logic1 logic2 logic3]`
and logic1, logic2, and logic3 implements flowgic.core/Logic

```clojure
(defprotocol flowgic.core/Logic
  (evaluate [this  ^clojure.lang.PersistentArrayMap context]))
```

In other words and to clarify a bit more: **A `flowgic/Logic` needs a logical-context to be able to solve an evaluation**


##flowgic.core/Logic impls

### core/Continuation => flowgic/continue
A Continuation means: after a Logic/evaluation, flow must continue on next logic (except core/Return, see next impl)


Typically, Continuation is represented as a intermediate box between 2 others (other of same or different type)

<img width="150"  src="https://cloud.githubusercontent.com/assets/731829/10295406/d13a0cb6-6bc0-11e5-83eb-49eb65a4e95c.png">   


There are three ways to get a core/Continuation 


```clojure
;; if you don't need to select anything of the result or add any static data to the result
  (def cont (flowgic/continue (fn [map] nil))    
  (= [:continue {:a 1}] (flowgic/evaluate cont {:a 1}))
          
;;if you need to select some values from the result
  (def cont1 (flowgic/continue (fn [map] {:cont1 "A"}) [:res1])) 
  (= [:continue {:a 1 :cont1 "A"}] (flowgic/evaluate cont1 {:a 1}))

;;if you need to select some values and add some others  
  (def cont2 (flowgic/continue (fn [map] {:cont2 "B"}) [:cont2] {:flag2 "cont2-flag"}))
(= [:continue {:a 1 :cont2 "B" :flag2 "cont2-flag"}] (flowgic/evaluate cont2 {:a 1}))
```


##  core/Return => flowgic/exit
*Return is the break circuit in a flow.*

A Return means: after logic evaluation, flow must end returning the fn return value
The behaviour is similar to exception but just sending its own result to the end.

<img width="150" src="https://cloud.githubusercontent.com/assets/731829/10295571/cc5eb56a-6bc1-11e5-97b7-1c4d1ba20e1d.png">

```clojure  
(def step-exit (flowgic/exit (fn [map] {:res "EXIT"})))
(= [:exit {:res "EXIT"}] (flowgic/evaluate step-exit {}))              
```

### Logic Seqs, => [ logic1 logic2 logic3]
A vector means: evaluate the context with the first logic, then if Continuation pass the res of first evaluation merged with the initial context to the second logic and repeat until the end (that it returns the context merged with all the continuation results).
If instead of Continuation we get Return, then we return the result of Return.


```clojure  
(def logics [cont cont1 cont2])

(= [:continue {:initial-data "hello", :cont1 "A", :cont2 "B", :flag2 "cont2-flag"}]
   (flowgic/evaluate steps {:initial-data "hello"}))   
```
<img width="50" alt="screen shot 2015-10-06 at 01 00 13" src="https://cloud.githubusercontent.com/assets/731829/10296077/b1a162f0-6bc5-11e5-9d33-9a8a40aaa15a.png">



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
