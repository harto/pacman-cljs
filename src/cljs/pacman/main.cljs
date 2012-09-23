(ns pacman.main
  (:require [clojure.browser.repl :as repl]
            [goog.events :as event]
            [goog.events.KeyCodes :as key]
            [goog.Timer :as timer]
            [pacman.util.canvas :as gfx]
            [pacman.util.dom :as dom]
            [pacman.util.logging :as logging]            
            [pacman.maze :as maze]
            [pacman.pacman :as pacman]
            [pacman.resources :as res])
  (:require-macros [pacman.util.canvas :as gfxm]
                   [pacman.util.logging :as log])
  (:use [pacman.core :only [draw-sprite! hz invalidate-region
                            world-width world-height]]
        [pacman.util :only [dissoc-in format now]]))

(defn- tick-delay [] (/ 1000 hz))

(defn- initial-state []
  {:score 0
   :level 1
   :lives 3
   :dots (maze/init-dots)
   :pacman (pacman/init)})

;; ## Profiling

;; The duration of each tick is recorded. When profiling is enabled, the display
;; updates once a second with statistics about that data.

(def ^:private profile? (atom true))

(def ^:private stats-panel
  (delay
   (let [panel (dom/element! "pre" {:id "stats"})]
     (dom/styles! panel {:position "fixed" :right 0 :top 0})
     (dom/append! (dom/get-element "cruft") panel)
     panel)))

(defn- print-stats!
  "Generates and prints timing statistics from the last batch of tick durations."
  [tick-durations]
  (let [tick-count (count tick-durations)
        avg-duration (/ (reduce + tick-durations) tick-count)]
    (dom/content! @stats-panel
                  (format "fps=%d\nmin=%2.2fms\navg=%2.2fms (%.2f%%)\nmax=%2.2fms"
                          tick-count
                          (if (zero? tick-count) 0 (apply min tick-durations))
                          avg-duration
                          (* 100 (/ avg-duration (tick-delay)))
                          (if (zero? tick-count) 0 (apply max tick-durations))))))

;; ## Key handling

;; Track the direction the user wants to go in
(def ^:private requested-direction (atom nil))

(def ^:private key-directions
  {key/UP :north
   key/DOWN :south
   key/LEFT :west
   key/RIGHT :east})

(defn- key-pressed [e]
  (let [code (.-keyCode e)]
    (when-let [direction (key-directions code)]
      (reset! requested-direction direction)
      (.preventDefault e))))

(defn- key-released [e]
  (let [code (.-keyCode e)]
    (if-let [direction (key-directions code)]
      (when (= direction @requested-direction)
        (reset! requested-direction nil)
        (.preventDefault e)))))

;; ## Update

(defn- update [state]
  (-> state
      (dissoc :invalidated)
      ;; TODO: mark all entities valid
      (dissoc-in [:pacman :invalidated?])
      (pacman/update @requested-direction)))

;; ## Redraw

(def ^:private screen
  (delay (dom/get-element "screen")))

(defn- screen-width []
  (.-width @screen))

(defn- screen-height []
  (.-height @screen))

(defn- invalidated-entities [clean dirty]
  ;; TODO
  )

(defn- repaint-bg! [ctx x y w h]
  (gfx/draw-image! ctx (res/image :maze) x y w h x y w h))

(defn- draw! [{invalidated-regions :invalidated p :pacman dots :dots} ctx]
  (gfxm/with-state ctx
    (gfx/scale! ctx (/ (screen-width) world-width) (/ (screen-height) world-height))
    (doseq [{:keys [x y w h]} invalidated-regions]
      (repaint-bg! ctx x y w h))
    (doseq [{:keys [type col row]} (maze/invalidated-dots dots invalidated-regions)]
      (maze/draw-dot! ctx type col row))
    ;; FIXME: check if externally invalidated
    (if (:invalidated? p)
      (draw-sprite! p ctx))))

;; ## Loop

(defn- tick [state]
  (let [start-time (now)
        stats (:stats state)
        reset-stats? (> (- start-time (:last-printed stats)) 1000)]
    (if (and reset-stats? @profile?)
      (print-stats! (:tick-durations stats)))
    (draw! state (gfx/context @screen))
    (let [new-state (-> (update state)
                        (assoc :stats (if reset-stats?
                                        {:last-printed start-time}
                                        (update-in stats [:tick-durations]
                                                   conj (- (now) start-time)))))]
      (timer/callOnce #(tick new-state)
                      (max 0 (- (tick-delay) (- (now) start-time)))))))

(defn- start []
  (tick (-> (initial-state)
            (invalidate-region 0 0 world-width world-height))))

(defn ^:export init []
  (dom/style! @screen :background-color "#000")

  (logging/init "pacman" :debug)
  (log/info "initing")

  (repl/connect "http://localhost:9000/repl")
  
  (event/listen js/document (.-KEYDOWN event/EventType) key-pressed)
  (event/listen js/document (.-KEYUP event/EventType) key-released)

  ;; TODO: audio toggle

  (res/load-resources! {:images {:maze   "bg.png"
                                 :inky   "inky.png"
                                 :pinky  "pinky.png"
                                 :blinky "blinky.png"
                                 :clyde  "clyde.png"}}
                       start))
