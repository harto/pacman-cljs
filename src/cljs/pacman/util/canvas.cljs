(ns pacman.util.canvas
  (:require [clojure.string :as str]))

(defn context [element]
  (.getContext element "2d"))

;; ## Context manipulation

(defn- with-state* [ctx body]
  (.save ctx)
  (try
    (body)
    (finally (.restore ctx))))

(defn- keyword->property
  ":foo-bar-baz => 'fooBarBaz'"
  [k]
  (let [words (str/split (name k) #"-")]
    (apply str (first words) (map str/capitalize (rest words)))))

(defn set-property! [ctx k v]
  (aset ctx (keyword->property k) v))

(defn set-properties! [ctx m]
  (doseq [[k v] m]
    (set-property! ctx k v)))

(defn with-properties* [ctx props body]
  (with-state* ctx (fn []
                     (set-properties! ctx props)
                     (body))))

;; ## Paths

(defn with-path* [ctx body]
  (.beginPath ctx)
  (try
    (body)
    (finally (.closePath ctx))))

(defn move! [ctx x y]
  (.moveTo ctx x y)
  ctx)

(defn arc!
  ([ctx x y radius start-angle end-angle]
     (arc! ctx x y radius start-angle end-angle nil))
  ([ctx x y radius start-angle end-angle anticlockwise?]
     (.arc ctx x y radius start-angle end-angle anticlockwise?)
     ctx))

(defn circle! [ctx x y radius]
  (arc! ctx x y radius 0 (* 2 Math/PI)))

(defn fill! [ctx]
  (.fill ctx)
  ctx)

(defn stroke! [ctx]
  (.stroke ctx)
  ctx)

;; ## Shapes

(defn clear-rect! [ctx x y w h]
  (.clearRect ctx x y w h)
  ctx)

(defn fill-rect! [ctx x y w h]
  (.fillRect ctx x y w h)
  ctx)

;; ## Images

(defn draw-image!
  ([ctx image x y]
     (.drawImage ctx image x y)
     ctx)
  ([ctx image x y w h]
     (.drawImage ctx image x y w h)
     ctx)
  ([ctx image sx sy sw sh dx dy dw dh]
     (.drawImage ctx image sx sy sw sh dx dy dw dh)
     ctx))

;; ## Transformations

(defn scale! [ctx x y]
  (.scale ctx x y)
  ctx)
