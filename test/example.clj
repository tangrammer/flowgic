(ns example
  (:require
   [ch.deepimpact.flowgic.graph :as g]
   [ch.deepimpact.flowgic.meta :as m]
   [ch.deepimpact.flowgic :as f]
   [ch.deepimpact.flowgic.core :as c]

   [plumbing.core :refer (fnk sum ?> ?>> defnk)]
   [rhizome.viz :refer :all]
   ))

(defnk a [] {})
(defnk b [] {})
(defnk c [] {})
(defnk d [] {})

(g/view [(f/continue #'a)
         (f/continue #'b)
         (f/continue #'c)])
