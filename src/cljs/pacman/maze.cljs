(ns pacman.maze
  (:require [pacman.util.canvas :as gfx])
  (:require-macros [pacman.util.canvas :as gfxm])
  (:use [pacman.core :only [intersects? col->x x->col row->y y->row tile-size]]))

;; ## Maze, dots

(def ^:private tile-map
  ["############################"
   "############################"
   "############################"
   "############################"
   "#............##............#"
   "#.####.#####.##.#####.####.#"
   "#o####.#####.##.#####.####o#"
   "#.####.#####.##.#####.####.#"
   "#..........................#"
   "#.####.##.########.##.####.#"
   "#.####.##.########.##.####.#"
   "#......##....##....##......#"
   "######.##### ## #####.######"
   "######.##### ## #####.######"
   "######.##          ##.######"
   "######.## ######## ##.######"
   "######.## ######## ##.######"
   "      .   ########   .      "
   "######.## ######## ##.######"
   "######.## ######## ##.######"
   "######.##          ##.######"
   "######.## ######## ##.######"
   "######.## ######## ##.######"
   "#............##............#"
   "#.####.#####.##.#####.####.#"
   "#.####.#####.##.#####.####.#"
   "#o..##.......  .......##..o#"
   "###.##.##.########.##.##.###"
   "###.##.##.########.##.##.###"
   "#......##....##....##......#"
   "#.##########.##.##########.#"
   "#.##########.##.##########.#"
   "#..........................#"
   "############################"
   "############################"
   "############################"])

(defn traversable? [col row]
  (not= \# (get-in tile-map [row col])))

(def ^:private dot-diameter
  {:dot (* tile-size 0.25)
   :energiser (* tile-size 0.75)})

(def ^:private dot-colour
  {:dot "#FFCCCC"
   :energiser "#FFB6AD"})

(defn init-dots
  "Returns a data structure that maps rows->cols->type."
  []
  (let [types {\. :dot \o :energiser}]
    (->> tile-map
         (keep-indexed (fn [row chars]
                         (let [dots (->> chars
                                         (keep-indexed (fn [col c]
                                                         (if-let [type (types c)]
                                                           [col type])))
                                         (into {}))]
                           (if-not (empty? dots)
                             [row dots]))))
         (into {}))))

(defn invalidated-dots
  "Finds the set of dots that overlap `invalidated-regions`. This solution
   does an overlap check only on those dots contained by the tiles in each
   region. This reduces the maximum number of calls to `intersects?` from
   roughly 2440 to less than 100 (244 dots, 10 regularly invalidated entities,
   less than 10 tiles per entity). i.e. running time grows proportional to the
   total size of the regions, not the number of regions."
  [dots invalidated-regions]
  (->>
   (for [{rx :x ry :y rw :w rh :h} invalidated-regions
         row (range (Math/floor (y->row ry)) (inc (Math/floor (y->row (+ ry rh)))))
         col (range (Math/floor (x->col rx)) (inc (Math/floor (x->col (+ rx rw)))))
         :let [type (get-in dots [row col])]
         :when (and type (let [diameter (dot-diameter type)
                               radius (/ diameter 2)
                               x (- (col->x (+ col 0.5)) radius)
                               y (- (row->y (+ row 0.5)) radius)]
                           (intersects? rx ry rw rh x y diameter diameter)))]
     {:row row :col col :type type})
   (into #{})))

(defn draw-dot! [ctx type col row]
  (gfxm/with-properties ctx {:fill-style (dot-colour type)}
    (gfxm/with-path ctx
      (-> ctx
          (gfx/circle! (col->x (+ col 0.5))
                       (row->y (+ row 0.5))
                       (/ (dot-diameter type) 2))
          (gfx/fill!)))))
