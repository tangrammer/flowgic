(defproject ch.deepimpact/flowgic "0.1.3-SNAPSHOT"
  :description "Flow logic goes declarative"
  :url "https://github.com/DEEP-IMPACT-AG/flowgic"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [prismatic/schema "0.4.3"]
                 [prismatic/plumbing "0.4.4"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [ch.deepimpact/flowgic.graph "0.1.1-SNAPSHOT"]
                                  ]
                   :source-paths ["dev"]}})
