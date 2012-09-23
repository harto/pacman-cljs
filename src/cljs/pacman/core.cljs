(ns pacman.core)

(def hz 60)
(def cols 28)
(def rows 36)
(def tile-size 8)
(def world-width (* cols tile-size))
(def world-height (* rows tile-size))

;; Timing helpers

(defn seconds->ticks [seconds] (Math/round (* seconds hz)))
(defn ticks->seconds [ticks] (/ ticks hz))

;; Map between tiles and pixels

(defn col->x [col] (* col tile-size))
(defn row->y [row] (* row tile-size))
(defn x->col [x] (/ x tile-size))
(defn y->row [y] (/ y tile-size))

(defn direction->x [direction] (get {:east 1 :west -1} direction 0))
(defn direction->y [direction] (get {:south 1 :north -1} direction 0))

(defn intersects? [ax ay aw ah bx by bw bh]
  (not (or (> ax (+ bx bw))
           (> bx (+ ax aw))
           (> ay (+ by bh))
           (> by (+ ay ah)))))

;; Define a generic interface for certain classes of entities (ghosts, Pac-Man)

(defprotocol Sprite
  (boundary [this])
  (draw-sprite! [this ctx]))

;; ## Canvas

(defn invalidate-region [state x y w h]
  (update-in state [:invalidated] conj {:x x :y y :w w :h h}))

