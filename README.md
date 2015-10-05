# ch.deepimpact/flowgic

### Premises:  
* your flow logic is a [complex and nested one](https://cloud.githubusercontent.com/assets/731829/10277888/8a5bf848-6b59-11e5-96de-1b67fab4981b.png)
* your fns controller suck :) [example](https://gist.github.com/tangrammer/b8fc6687f051ab059ac2#file-old_api-clj)
* your fns receive and return a map

This is an attempt to write flow logic code in a different manner. Based (a bit) on the idea of [pipe-lines](https://en.wikipedia.org/wiki/Pipeline_(software)) data transformations



## Basic elements

###Steps
in the same way as diagram flows we have 2 steps option at the end of  a step evaluation: 

* **Continue**:  you have to continue to next step or exit if noone is available
* **Return**: circuit breaker, you go to the exit point


### Step containers
you can organise the logic steps into containers. 

* **Vector**: the simplest one is `[]`
* **Rule**: adding evaluation condition to group of steps.  `{:true step1 :false step2}` 


###Example
```clojure
(require '[ch.deepimpact.flowgic :as flowgic])

[(flowgic/continue (fn [a] {}))
 (flowgic/continue (fn [b] {}))
 (flowgic/just (fn [c] {}))]

```









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



## Usage

TODO



## Change Log

* Version 0.1.0-SNAPSHOT



## Copyright and License

Copyright © 2015 TODO_INSERT_NAME

TODO: [Choose a license](http://choosealicense.com/)
