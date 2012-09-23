(ns pacman.pacman
  (:require [pacman.maze :as maze]
            [pacman.util.canvas :as gfx])
  (:require-macros [pacman.util.canvas :as gfxm])
  (:use [pacman.core :only [Sprite boundary hz seconds->ticks tile-size invalidate-region
                            direction->x direction->y x->col col->x y->row row->y]]
        [pacman.util :only [sign]]))

;; ## Pac-Man character

(defn- animate
  "Increases or decreases `:open` (and possibly changes `:opening?`) according
   to the current state. We keep this state because Pac-Man's mouth only opens/
   closes while he's moving."
  [{was-opening? :opening? prev-open :open :as p}]
  (let [max-opening 0.4
        increment (/ max-opening (seconds->ticks 0.08))
        open (if was-opening?
               (min max-opening (+ prev-open increment))
               (max 0 (- prev-open increment)))
        opening? (or (and was-opening? (< open max-opening))
                     (and (not was-opening?) (<= open 0)))]
    (assoc p :open open :opening? opening?)))

(defn- speed [level frightened?]
  (* (/ 60 hz)
     (if frightened?
       (cond (= level 1) 0.9
             (< level 5) 0.95
             :else 1)
       (cond (= level 1) 0.8
             (or (< level 5) (< 20 level)) 0.9
             :else 1))))

(defn- tile-centre [col row]
  {:col (+ (Math/floor col) 0.5)
   :row (+ (Math/floor row) 0.5)})

(defn- tile-beyond
  "Find tile half-a-step beyond `[col, row]` in `direction`."
  [col row direction]
  ;; XXX: broken - should only consider direction. I.e. 

  ;; e.g. for 1.5 <= new-col < 2.5:
  ;;   next-col = 1 where direction = :west
  ;;   next-col = 2 where direction = :east
  {:col (+ (Math/round col) (/ (- (direction->x direction) 1) 2))
   :row (+ (Math/round row) (/ (- (direction->y direction) 1) 2))})

;; XXX: This update/move stuff doesn't work and it all seems horrible :(

(defn- move
  "Returns Pac-Man's new location, or `nil` if move isn't possible. Moving
   beyond the centre of a tile isn't allowed if the tile beyond is not
   traversable."
  [x y direction speed]
  (let [dx (direction->x direction)
        dy (direction->y direction)
        moving-horizontally? (zero? dy)
        moving-vertically? (zero? dx)
        new-x (+ x (* speed dx))
        new-y (+ y (* speed dy))
        new-col (x->col new-x)
        new-row (y->row new-y)
        tile-centre (tile-centre new-col new-row)
        tile-centre-x (col->x (:col tile-centre))
        tile-centre-y (row->y (:row tile-centre))
        next-tile (tile-beyond new-col new-row direction)]
    (if-let [new-pos (if (maze/traversable? (:col next-tile) (:row next-tile))
                       {:x new-x :y new-y}
                       ;; We can't move beyond the tile centre, so try moving
                       ;; directly onto the centre point.
                       (let [centred-x (if moving-horizontally? tile-centre-x x)
                             centred-y (if moving-vertically? tile-centre-y y)]
                         (if (or (not= x centred-x) (not= y centred-y))
                           {:x centred-x :y centred-y})))]
      ;; We can move in the requested direction. To let Pac-Man cut corners, we
      ;; now centre on the perpendicular axis, e.g. if moving horizontally,
      ;; centre vertically.
      (let [{:keys [x y]} new-pos
            min-offset #(* (sign %) (min speed (Math/abs %)))]
        {:x (+ x (if moving-horizontally? 0 (min-offset (- tile-centre-x x))))
         :y (+ y (if moving-vertically? 0 (min-offset (- tile-centre-y y))))}))))

(defn update
  [{{:keys [x y direction] :as pacman} :pacman :as state} new-direction]
  (let [speed (speed (:level state) false) ; FIXME
        try-move (fn [direction]
                   (if-let [pos (move x y direction speed)]
                     (-> (merge pacman pos)
                         (assoc :direction direction)
                         (animate))))]
    (if-let [moved (or (if new-direction
                         (try-move new-direction))
                       (try-move direction))]
      (let [{old-x :x old-y :y w :w h :h} (boundary pacman)]
        (-> state
            (invalidate-region (dec old-x) (dec old-y) (+ w 2) (+ h 2))
            (assoc :pacman (assoc moved :invalidated? true))))
      state)))

;; Map direction keywords to angles (radians)
(def ^:private angle
  (zipmap [:east :south :west :north]
          (iterate #(+ (/ Math/PI 2) %) 0)))

(defn draw!
  "Draws a Pac-Man figure.

   * `ctx`: graphics context
   * `centre-[x|y]`: circle centre
   * `offset-[x|y]`: distance to offset hinge from `centre-[x|y]`
   * `radius`: circle radius
   * `direction`: one of `:north`, `:south`, `:east`, `:west`
   * `open`: a value from 0 to 1, where 0 is completely closed and 1 is
     completely open (i.e. dead)"
  [ctx centre-x centre-y offset-x offset-y radius direction open]
  (let [angle (angle direction)
        open-angle (* Math/PI open)]
    (gfxm/with-properties ctx {:fill-style "yellow"}
      (gfxm/with-path ctx
        (-> ctx
            (gfx/move! (+ centre-x offset-x) (+ centre-y offset-y))
            (gfx/arc! centre-x
                      centre-y
                      radius
                      (+ angle open-angle)
                      (+ angle (- (* 2 Math/PI) open-angle)))))
      (gfx/fill! ctx))))

;; Note: `defrecord` within a `let` isn't allowed on the JVM, but it works here.
(let [diameter (* 1.5 tile-size)
      radius (/ diameter 2)
      centre-offset (/ radius 4)]
  (defrecord Pacman [x y direction opening? open]
    Sprite
    (boundary [_]
      {:x (- x radius)
       :y (- y radius)
       :w diameter
       :h diameter})
    (draw-sprite! [_ ctx]
      (draw! ctx x y
             (* centre-offset (- (direction->x direction)))
             (* centre-offset (- (direction->y direction)))
             radius
             direction
             open))))

(defn init []
  (->Pacman (col->x 14)
            (row->y 26.5)
            :west
            true
            0.2))
