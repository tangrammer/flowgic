# ch.deepimpact/flowgic


`flowgic` is a coding style proposal (or DSL) focused in highlighting the logical internal flow of *API fns*. 

###*API fns* = *controller fns*

API fns usually play the role of Controllers that have to *manage* different services regarding inputs, outputs, steps, rules and decisions.


### The existent solution doesn't *usually* work
Although **[flow diagramms](https://duckduckgo.com/?q=flow+diagram&iax=1&ia=images&iai=http%3A%2F%2Fstar-w.kir.jp%2Fgrp%2F9%2Fflow-chart-diagram-software-i1.png)** is the common solution to express logical flows that happens inside our API fns, the situation doen't work normally...  the developer usually prefers to write the code directly once he/she has the use case description and, the PM usually doesn't know or remember all the differents inputs, outputs, conditions, rules ... 


###our solution and goals
`flowgit` proposes a **declarative DSL** to the developer (and PM too!) to specify the logical flow of its API fns. 

* the code becomes clearer :)
* task's communication between devs and clients or project managers is improved :)
* `flowgic` code can be easily parsed and analysed to generate dynamic [flow diagrams](https://cloud.githubusercontent.com/assets/731829/10277888/8a5bf848-6b59-11e5-96de-1b67fab4981b.png)
* once that you can see what your code does you can know which parts need to be tested from the others that dont' need (you don't need to test the conditions just the actions)
* is easy to add any middleware too :)


### this lib can be useful if...  
* your logic flow is a [complex and nested one](https://cloud.githubusercontent.com/assets/731829/10277888/8a5bf848-6b59-11e5-96de-1b67fab4981b.png)
* your API fns are tricky to understand, even by the author after a few days [example](https://gist.github.com/tangrammer/b8fc6687f051ab059ac2#file-old_api-clj)
* your fns receive a map and return a map. Of course you could always adapt your *APIs/controller fns* to this great pattern => [Prismatic/fnk](https://github.com/Prismatic/plumbing#fnk) 

#Let's dive into `flowgic`!

## Main protocol: core/Evaluation

```clojure
(defprotocol Evaluation
  (evaluate [this  context]))
```

As you can see, any `core/Evaluation` can be evaluated with a context. The **`context` is a clojure map.**


##The `flowgic` impls and fns
Flowgic comes with a bunch of Evaluation impls and fns constructors. So each one can be evaluated with a context but the result will depend of each impl

### core/Continuation => flowgic/continue
The typical box that **represents an action-fn**. Always between 2 entities    

<img width="150"  src="https://cloud.githubusercontent.com/assets/731829/10295406/d13a0cb6-6bc0-11e5-83eb-49eb65a4e95c.png">   

3 ways to get a core/Continuation 

* if you don't need to select anything of the result or add any static data to the result
 * `(flowgit/continue action-fn result-keys)`
* if you need to select some values from the result
 * `(flowgit/continue action-fn result-keys)`
* if you need to select some values and add some others
 * `(flowgit/continue action-fn result-keys flags)` 



```clojure
  (def step (flowgic/continue (fn [map] {:res "A"})))            

  (def step1 (flowgic/continue (fn [map] {:res1 "B"}) [:res1])) 
  
  (def step2 (flowgic/continue (fn [map] {:res1 "B"}) [:res1] {:step2 true}))
                                  
```

Now let's evaluate the Continuation 

```clojure
(= [:continue {}] (flowgic/evaluate step {}))
(= [:continue {:res1 "1"}] (flowgic/evaluate step1 {}))
(= [:continue {:res2 "2" :step2 true}] (flowgic/evaluate step2 {}))

```

##  core/Return => flowgic/exit
Return is the break circuit in a flow. Similar to exception but just sending data to the end.

<img width="150" src="https://cloud.githubusercontent.com/assets/731829/10295571/cc5eb56a-6bc1-11e5-97b7-1c4d1ba20e1d.png">

```clojure  
(def step-exit (flowgic/exit (fn [map] {:res "EXIT"})))
(= [:exit {:res "EXIT"}] (flowgic/evaluate step-exit {}))              
```


### Continuation Seqs, Steps, Pipeline => [ ]
<img width="50" alt="screen shot 2015-10-06 at 01 00 13" src="https://cloud.githubusercontent.com/assets/731829/10296077/b1a162f0-6bc5-11e5-9d33-9a8a40aaa15a.png">

Chaining Continuations is the same as putting theme in a vector. Of course, you can evaluate pipelines too!


```clojure  
(def steps [step step1])

(= [:continue {:initial-data "hello", :res1 "1", :res2 "2", :step2 true}]
   (flowgic/evaluate steps {:initial-data "hello"}))   
```
As you can see we always get the initial-context merged with all actions selections. And the evaluation is ordered based in vector position 

TODO: hightlight that the context is merged/updated in each Continuation


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