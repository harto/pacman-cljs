(ns pacman.util
  (:require [goog.string :as gs]
            [goog.string.format :as _]))

;; ## Math helpers

(defn round-half-down [n]
  (Math/ceil (- n 0.5)))

(defn sign [n]
  (cond (< 0 n) 1
        (< n 0) -1
        :else 0))

;; ## Miscellany

(defn dissoc-in
  "Stolen from old clojure.contrib.core"
  [m [k & ks]]
  (if ks
    (if-let [nested (get m k)]
      (let [updated (dissoc-in nested ks)]
        (if (seq updated)
          (assoc m k updated)
          (dissoc m k)))
      m)
    (dissoc m k)))

(defn now
  "Returns current time in milliseconds."
  []
  (. (js/Date.) (getTime)))

;; printf-style formatting
(def format gs/format)

