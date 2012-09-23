(ns pacman.util.canvas)

(defmacro with-state
  "Saves context state, executes `body` then restores previous state."
  [ctx & body]
  `(~'pacman.util.canvas/with-state* ~ctx (fn [] ~@body)))

(defmacro with-properties
  "Saves context state, applies context properties, executes `body` then
   restores previous state."
  [ctx props & body]
  `(~'pacman.util.canvas/with-properties* ~ctx ~props (fn [] ~@body)))

(defmacro with-path
  "Opens a path, executes `body` then closes path."
  [ctx & body]
  `(~'pacman.util.canvas/with-path* ~ctx (fn [] ~@body)))
