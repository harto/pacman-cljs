(ns pacman.util.logging)

;; Define macros that defer to logging functions in the parallel ClojureScript
;; namespace. Using macros instead of functions allows us to defer evaluation
;; of messages until we know they should be logged.
;;
;; XXX: we have no way to ensure inclusion of required ClojureScript namespaces

(defmacro ^:private log
  "Delegates logging to helper functions in the ClojureScript namespace."
  [level msg args]
  `(~'pacman.util.logging/log* ~level #(~'pacman.util/format ~msg ~@args)))

(defmacro debug [msg & args]
  `(log :debug ~msg ~args))

(defmacro info [msg & args]
  `(log :info ~msg ~args))

(defmacro warn [msg & args]
  `(log :warn ~msg ~args))

(defmacro error [msg & args]
  `(log :error ~msg ~args))
