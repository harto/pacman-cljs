(ns pacman.server
  "Single-page Ring application that bootstraps the ClojureScript environment."
  (:use [compojure.core :only (routes GET)]
        [ring.adapter.jetty :only (run-jetty)]
        [ring.middleware.file :only (wrap-file)]
        [ring.middleware.stacktrace :only (wrap-stacktrace)])
  (:require [clojure.string :as str]
            [net.cgrand.enlive-html :as html]))

;; ## Page generation

(defn- normalise-url
  "Normalises resource URLs, which are defined relative to the `templates`
   directory in unparsed snippets."
  [attr]
  (fn [node]
    (update-in node [:attrs attr] #(str/replace % #"^(../)+public/" "/"))))

(html/deftemplate index "templates/index.html" []
  [[:link (html/attr= :rel "stylesheet")]] (normalise-url :href)
  [:body] (html/append (html/html-snippet "<script src=\"/js/pacman.js\"></script>")
                       (html/html-snippet "<script>goog.require('pacman.main');</script>")
                       (html/html-snippet "<script>pacman.main.init();</script>")))

;; ## Ring application

(def ^:private app
  (-> (routes (GET "/" [] (index)))
      (wrap-file "resources/public")
      (wrap-stacktrace)))

;; ## Jetty server

(defonce ^:private server (atom nil))

(defn stop []
  (if-let [s @server]
    (.stop s)))

(defn start []
  (stop)
  (reset! server (run-jetty #'app {:port 8080 :join? false})))
