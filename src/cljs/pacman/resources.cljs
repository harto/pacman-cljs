(ns pacman.resources
  (:use [pacman.util :only [dissoc-in]])
  (:require [pacman.util.logging :as logging])
  (:require-macros [pacman.util.logging :as log]))

(def resources
  (atom {}))

(defn image [id]
  (get-in @resources [:images id]))

(defn- fetch-image
  "Asyncronously fetches an image."
  [url onload onerror]
  (let [image (js/Image.)]
    (aset image "onload" #(onload image))
    (aset image "onerror" onerror)
    (aset image "src" url)
    image))

(defn- fetch-sound
  "Asynchronously fetches an audio resource."
  [url onload onerror]
  )

(defn load-resources! [{:keys [images sounds] :as remaining} onload]
  (let [pending (atom remaining)
        fetch-resource (fn [path type id load-fn]
                         (let [url (str "/" path)]
                           (log/debug "fetching %s..." url)
                           (load-fn url
                                    (fn success [resource]
                                      (log/info "loaded %s" url)
                                      (swap! resources assoc-in [type id] resource)
                                      (swap! pending dissoc-in [type id])
                                      (if (empty? @pending)
                                        (onload)))
                                    (fn error []
                                      (-> (str "Failed to load " url)
                                          (js/Error.)
                                          (throw))))))]
    (doseq [[id path] images]
      (fetch-resource path :images id fetch-image))
    (doseq [[id path] sounds]
      (fetch-resource path :sounds id fetch-sound))))
