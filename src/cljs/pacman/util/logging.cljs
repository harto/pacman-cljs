(ns pacman.util.logging
  (:require [goog.debug.Console :as Console]
            [goog.debug.Logger.Level :as Level]
            [goog.debug.LogManager :as LogManager]
            ;; required by logging macros
            [pacman.util :as util]))

(def ^:private levels
  {:debug Level/FINE
   :info  Level/INFO
   :warn  Level/WARNING
   :error Level/SEVERE})

(def ^:private logger (atom nil))

;; ## Initialisation

(defn init [logger-name level]
  (LogManager/initialize)
  (.setCapturing (goog.debug.Console.) true)
  (.setLevel (LogManager/getRoot) (levels :warn))
  (reset! logger (LogManager/getLogger logger-name))
  (.setLevel @logger (levels level)))

;; ## Logging functions

(defn log* [level message-fn]
  (let [level (get levels level)
        logger @logger]
    (if (.isLoggable logger level)
      (.log logger level (str (message-fn))))))

