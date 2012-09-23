(ns pacman.util.dom
  (:require [clojure.browser.dom :as dom]
            [goog.dom :as gdom]
            [goog.style :as style]))

(def append! dom/append)
(def element! dom/element)
(def get-element dom/get-element)

(defn content! [e & vals]
  (gdom/setTextContent e (apply str vals)))

(defn style! [e k v]
  (style/setStyle e (name k) v)
  e)

(defn styles!
  [e m]
  (doseq [[k v] m]
    (style! e k v)))
