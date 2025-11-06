;; # Helper functions for Advent of Code


^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns aoc-utils.core
  {:nextjournal.clerk/visibility {:result :hide}
   :nextjournal.clerk/auto-expand-results? true
   :nextjournal.clerk/toc true}
  (:require [clojure.string :as str]
            [clojure.math :as math]
            [clojure.data.int-map :as i]
            [clojure.data.priority-map :refer [priority-map]]
            [nextjournal.clerk :as clerk]
            [aoc-utils.core :as aoc]))




;; ## Reading input files
;;
;; My inputs are always in the `../inputs/` directory,
;; named `dd`, i.e. have two digits, and have a `.txt` extension.
;; Let's simplify reading them:
;;
(defn read-input
  "Read contents of an input file."
  [file]
  (let [name (if (int? file)
               (format "%02d" file)
               file)]
    (str/trim (slurp (str "../inputs/" name ".txt")))))

;; This allows `(read-input 1)` to read the contents of a `01.txt` file.





;; ## Input parsing
;;
(defn integers
  "Extracts all integers from a string.
  It can ignore `-` if `negative=false`, e.g. for ranges `23-45`."
  [s & {:keys [negative?]
        :or {negative? true}}]
  (mapv parse-long
        (re-seq (if negative? #"-?\d+" #"\d+") s)))

(defn string->digits
  "Extracts digits from a string.
  Ignores non-digit characters."
  [s]
  (->> (str/split s #"")
       (map parse-long)
       (filterv some?)))

(defn parse-input
  "Parse input string, based on `parse-fn`."
  [s & [parse-fn word-sep]]
  (let [f (case parse-fn
            :int    parse-long
            :ints   integers
            :digits string->digits
            :chars  vec
            :words  #(str/split % (or word-sep #" "))
            nil     identity
            parse-fn)]
    (f s)))

;; The `parse-input` is doing some heavy lifting here.\
;; The `parse-fn` parameter there is the key to the versatility:
;; it makes possible to parse all AoC inputs, either by the typical
;; functions (parsing integers, extracting integers from a line, creating a
;; vector of integers, vector of characters, splitting lines into words, or
;; keeping everything as it is) or by passing it a custom function, specially
;; crafted for the task at hand.


^{:nextjournal.clerk/visibility {:code :fold :result :show}}
(let [inputs ["123" "abc12def34" "abc12def34" "ab1c" "abc def"]
      fns [:int :ints :digits :chars :words]
      results (map (fn [line parse-fn] (parse-input line parse-fn))
                   inputs
                   fns)]
  (clerk/html
   [:div.flex.justify-center
    (clerk/table {"Input" inputs
                  "Parse function" fns
                  "Result" results})]))




;; Now, parsing a whole input file is just a matter of applying that function
;; to each line of the input.\
;; On some rare occasions, the input is split into paragraphs by having an
;; empty line between different parts of input.
;; We have a function for that too:
;;
(defn parse-lines
  "Parse each line of `input`."
  [input & [parse-fn {:keys [word-sep nl-sep]}]]
  (mapv #(parse-input % parse-fn word-sep)
        (str/split input (or nl-sep #"\n"))))

(defn parse-paragraphs
  "Split `input` into paragraphs (separated by a blank line).
  Parse each paragraph based on `parse-fn`."
  [input & [parse-fn word-sep]]
  (mapv #(parse-input % parse-fn word-sep)
        (parse-lines input nil {:nl-sep #"\n\n"})))




^{:nextjournal.clerk/visibility {:code :fold :result :show}}
(let [lines ["1 2 3\n4 -5 6\n7 8 9"
             "abc def\nghi jkl"
             "abc\ndef\nghi"]
      fns [:ints :words :chars]
      results (map (fn [l f] (parse-lines l f)) lines fns)]
  (clerk/html
   [:div.flex.justify-center
    (clerk/table {"Lines" lines
                  "Parse function" fns
                  "Result" results})]))





;; ## Grids
;;
;; AoC wouldn't be AoC if there aren't many tasks where you're given a 2D
;; grid (sometimes even 3D).
;;
;; It is important to have a usable representation of a grid.
;; Sometimes we need to know a character at each coordinate (`point-map`),
;; the other times only the coordinates are important (`point-set`).
;; We can only keep the coordinates that satisfy the `pred` function.

;; The hashed variants are used when trying to optimize for speed.
;;
(defn grid->point-map
  "Convert a 2D list of points to a {[x y]: char} hashmap."
  ([v] (grid->point-map v identity nil))
  ([v pred] (grid->point-map v pred nil))
  ([v pred mult]
   (into (if mult (i/int-map) {})
         (for [[^long y line] (map-indexed vector v)
               [^long x char] (map-indexed vector line)
               :when (pred char)]
           (if mult
             [(+ (* y ^long mult) x) char]
             [[x y] char])))))

(defn grid->hashed-point-map
  "Convert a 2D list of points to a {hash: char} hashmap."
  ([v] (grid->point-map v identity 1000))
  ([v pred] (grid->point-map v pred 1000))
  ([v pred mult] (grid->point-map v pred mult)))


(defn grid->point-set
  "Convert a 2D list of points to a #{[x y]} set."
  ([v] (grid->point-set v identity nil))
  ([v pred] (grid->point-set v pred nil))
  ([v pred mult]
   (into (if mult (i/dense-int-set) #{})
         (for [[^long y line] (map-indexed vector v)
               [^long x char] (map-indexed vector line)
               :when (pred char)]
           (if mult
             (+ (* y ^long mult) x)
             [x y])))))

(defn grid->hashed-point-set
  "Convert a 2D list of points to a #{hash} set."
  ([v] (grid->point-set v identity 1000))
  ([v pred] (grid->point-set v pred 1000))
  ([v pred mult] (grid->point-set v pred mult)))









;; Sometimes we need to inspect a grid.
;; With `points->lines`, we create a printable string of all
;; points in the grid.
;;
(defn points->lines [points]
  (if (map? points) (points->lines (set (keys points)))
      (let [x-lim (inc ^long (reduce max (map first points)))
            y-lim (inc ^long (reduce max (map second points)))]
        (str/join \newline
                  (for [y (range y-lim)]
                    (str/join (for [x (range x-lim)]
                                (if (points [x y])
                                  \█ \space))))))))


;; #### Usage
;;
^{:nextjournal.clerk/visibility {:code :show :result :show}}
(let [grid ["#.." "..#" "##."]]
  (grid->point-set grid #{\#}))

^{:nextjournal.clerk/visibility {:code :show :result :show}}
(let [grid ["#.." "..#" "##."]]
  (points->lines (grid->point-map grid #{\#})))





;; ### 2D grids
;;
;; We also need some functions to navigate through the grids or do
;; stuff with the points in them:
;;
(defn manhattan ^long
  ([pt] (manhattan pt [0 0]))
  ([[^long x1 ^long y1] [^long x2 ^long y2]]
   (+ (abs (- x1 x2))
      (abs (- y1 y2)))))

(defn pt+ ^longs [[^long x1 ^long y1] [^long x2 ^long y2]]
  [(+ x1 x2) (+ y1 y2)])

(defn pt- ^longs [[^long x1 ^long y1] [^long x2 ^long y2]]
  [(- x1 x2) (- y1 y2)])

(defn pt* ^longs [^long magnitude [^long x ^long y]]
  [(* magnitude x) (* magnitude y)])

(defn left-turn
  "Assumes left-hand coord system (positive y goes down)."
  [[^long x ^long y]]
  [y (- x)])

(defn right-turn
  "Assumes left-hand coord system (positive y goes down)."
  [[^long x ^long y]]
  [(- y) x])

(defn inside?
  "Check if a point (x, y) is inside of a square/rectangle of a given size."
  ([size [x y]] (inside? size size x y))
  ([size x y]   (inside? size size x y))
  ([size-x size-y x y]
   (and (< -1 x size-x)
        (< -1 y size-y))))



;; We often need to find neigbhours of a point:

(def ^:const nb-4 [[0 -1] [-1 0] [1 0] [0 1]])
(def ^:const diags [[-1 -1] [1 -1] [-1 1] [1 1]])
(def ^:const nb-5 (conj nb-4 [0 0]))
(def ^:const nb-8 (into nb-4 diags))
(def ^:const nb-9 (conj nb-8 [0 0]))


(defn neighbours
  "4/5/8/9 neighbours of a point."
  (^longs [^long amount pt] (neighbours amount pt identity))
  (^longs [^long amount [^long x ^long y] cnd]
   (let [nbs (case amount
               4 nb-4
               5 nb-5
               8 nb-8
               9 nb-9)]
     (for [[^long dx ^long dy] nbs
           :let [nb [(+ x dx) (+ y dy)]]
           :when (cnd nb)]
       nb))))




;; #### Usage
;;
;; Different amount of neighbours of a point:
;;
^{:nextjournal.clerk/visibility {:code :fold :result :show}}
(let [point [0 0]
      nbs [4 5 8 9]
      results (map #(neighbours % point) nbs)
      axis-template {:ticks ""
                     :showticklabels false
                     :showgrid false
                     :zeroline false
                     :range [-2 2]}]
  (clerk/row
   (for [res results]
     (clerk/plotly {:config {:displayModeBar false
                             :displayLogo false}
                    :data [{:x (map first res)
                            :y (map second res)
                            :type :scatter
                            :mode :markers
                            :marker {:size 12}
                            :showscale false}]
                    :layout {:xaxis axis-template
                             :yaxis axis-template
                             :width 100
                             :height 100
                             :margin {:l 0 :r 0 :t 0 :b 0}}}))))

;; Some functions on a point:
;;
^{:nextjournal.clerk/visibility {:code :fold :result :show}}
(let [pts [[1 0] [1 0] [3 -4]]
      fns [left-turn right-turn manhattan]
      fnames ["left-turn" "right-turn" "manhattan"]
      results (map (fn [p f] (f p)) pts fns)]
  (clerk/html
     [:div.flex.justify-center
      (clerk/table {"Point" pts
                    "Parse function" fnames
                    "Result" results})]))






;; ### 3D grids
;;
;; Sometimes we also get 3D-grids, so here are some simplified
;; 3D-variants of the functions above.
;;
(defn manhattan-3d ^long
  ([p] (manhattan-3d p [0 0 0]))
  ([[^long x1 ^long y1 ^long z1] [^long x2 ^long y2 ^long z2]]
   (+ (abs (- x1 x2))
      (abs (- y1 y2))
      (abs (- z1 z2)))))

(defn pt-3d+ ^longs [[^long x1 ^long y1 ^long z1]
                     [^long x2 ^long y2 ^long z2]]
  [(+ x1 x2) (+ y1 y2) (+ z1 z2)])

(defn pt-3d- ^longs [[^long x1 ^long y1 ^long z1]
                     [^long x2 ^long y2 ^long z2]]
  [(- x1 x2) (- y1 y2) (- z1 z2)])

(defn pt-3d* ^longs [^long magnitude [^long x ^long y ^long z]]
  [(* magnitude x) (* magnitude y) (* magnitude z)])

(defn neighbours-3d [[^long x ^long y ^long z]]
  [[(dec x) y z] [(inc x) y z]
   [x (dec y) z] [x (inc y) z]
   [x y (dec z)] [x y (inc z)]])

(defn inside-3d?
  ([size [x y z]] (inside-3d? size x y z))
  ([size x y z]
   (and (< -1 x size)
        (< -1 y size)
        (< -1 z size))))







;; ## Graph traversal
;;
;; Graph traversal problems are relatively frequent in AoC,
;; but this is the first time I'm writing a pre-defined helper function
;; for them.
;; It remains to be seen how useful it'll be for specific tasks with
;; their specific needs: I feel like in the recent years the graph traversal
;; tasks always had some gotcha which made it harder to use a general algorithm,
;; rather than a specific one written for the task at hand.
;;
;; If this gets used, the implementation details will probably change,
;; depending on the specific tasks.
;; (As if the `traverse` function is not already way too long and complicated.)
;;
;; All four algorithms (`DFS`, `BFS`, `Dijkstra`, `A*`) share the same logic,
;; the difference is in a `queue` type.
;;
(def empty-queue clojure.lang.PersistentQueue/EMPTY)

(defn- build-path [current seen]
  (loop [curr current
         path nil]
    (if (or (nil? curr) (= :start curr))
      path
      (recur (first (seen curr)) (conj path curr)))))


(defn- traverse
  "General graph traversal function. Not very performant.
  The simplest version needs just `:start`, `:end` and `:walls` keys,
  and either `:nb-num` (number of neighbours to consider) or a custom
  `:nb-func`."
  [algo {:keys [start end walls size size-x size-y
                nb-func nb-num nb-cond end-cond
                cost-fn heuristic steps-limit
                allow-revisits? side-effect]
         :or {nb-num 4
              walls  #{}
              nb-cond (constantly true)
              cost-fn (constantly 1)
              side-effect (constantly nil)
              steps-limit ##Inf
              end-cond  #(= end %)
              heuristic (if end
                          (fn [pt] (manhattan pt end))
                          (constantly 0))}}]
  (let [inbounds? (cond
                    size #(inside? size %)
                    (and size-x size-y) (fn [[x y]] (inside? size-x size-y x y))
                    :else (constantly true))
        nb-filter (every-pred inbounds? (complement walls) nb-cond)]
    (loop [queue (case algo
                   :dfs    (list [start 0])
                   :bfs    (conj empty-queue [start 0])
                   :dijk   (priority-map start 0)
                   :a-star (priority-map [start 0] (heuristic start))
                   (throw (Exception. "aoc: unknown graph algorithm")))
           seen {start [:start 0]}]
      (let [[current ^long steps] (case algo
                                    :a-star (first (peek queue))
                                    (peek queue))
            nb-cost     (fn [pt] (+ steps ^long (cost-fn current pt)))
            seen-filter (fn [pt] (or allow-revisits?
                                     (< ^long (nb-cost pt)
                                        (or (second (seen pt))
                                            steps-limit))))]
        (side-effect {:pt current
                      :steps steps
                      :seen seen
                      :queue queue})
        (cond
          (or (nil? current)
              (>= steps steps-limit)
              (end-cond current)) {:steps   steps
                                   :seen    (set (keys seen))
                                   :costs   (into {} (map (fn [[k [_ s]]] [k s]) seen))
                                   :count   (count seen)
                                   :queue   queue
                                   :path    #(build-path current seen) ; don't build it if not needed
                                   :current current}
          :else
          (let [nbs (if nb-func
                      (filter (every-pred nb-filter seen-filter) (nb-func current))
                      (neighbours nb-num current (every-pred nb-filter seen-filter)))
                nbs+costs (map (fn [pt] [pt (nb-cost pt)]) nbs)]
            (recur
             (reduce (fn [q [pt ^long cost]]
                       (case algo
                         :dfs    (conj q [pt cost])
                         :bfs    (conj q [pt cost])
                         :dijk   (assoc q pt cost)
                         :a-star (assoc q [pt cost] (+ cost ^long (heuristic pt)))))
                     (pop queue)
                     nbs+costs)
             (reduce (fn [s [pt cost]] (assoc s pt [current cost]))
                     seen
                     nbs+costs))))))))


(defn dfs [options]
  (traverse :dfs options))

(defn bfs [options]
  (traverse :bfs options))

(defn a-star [options]
  (traverse :a-star options))

(defn dijkstra [options]
  (traverse :dijk options))



;; #### Usage
;;
^{:nextjournal.clerk/visibility {:code :show :result :show}}
(let [walls #{[0 1] [1 1] [1 3] [2 3] [4 0] [3 1] [4 2] [3 3] [4 4]}
      start [0 0]
      end [3 4]
      size 5]
  [(points->lines walls)
   (aoc/bfs {:start start
             :end end
             :walls walls
             :size size})])





;; ## Utilities
;;
;; Functions for some common AoC stuff.
;;
;; - `transpose`: We often need to iterate through columns, instead of rows.
;;   This transposes the matrix.
;; - `invert-tree`: Connections between nodes in a reverse order.
;; - `count-if`: In many tasks we need to apply some condition and then count
;;   the number of elements which satisfy it. This should be slightly faster
;;   (to type, at least) than `(count (filter ...))`.
;; - `sum-map`: Similarly, in some tasks we need to apply a function to
;;   each row, and then take a sum of the results.
;; - `find-first`: Returns the first element which satisfies a predicate.
;;
(defn transpose [matrix]
  (apply mapv vector matrix))

(defn invert-tree [tree]
  (reduce-kv
   (fn [acc k vs]
     (reduce (fn [acc v]
               (update acc v conj k))
             acc
             vs))
   {}
   tree))

(defn count-if
  "An alternative to `(count (filter ...))`."
  ^long [pred xs]
  (reduce
   (fn [^long acc x]
     (if (pred x) (inc acc) acc))
   0
   xs))

(defmacro do-count
  "Similar to the `count-if` function above, but allows for a more
  elaborate predicate, i.e. everything that the `doseq` built-in does."
  {:clj-kondo/lint-as 'clojure.core/doseq}
  [seq-exprs]
  `(let [counter# (atom 0)]
     (doseq ~seq-exprs
       (swap! counter# inc))
     @counter#))

(defn sum-map [f xs]
  (transduce (map f) + xs))

(defn sum-map-indexed [f xs]
  (transduce (map-indexed f) + xs))

(defn sum-pmap [f xs]
  (reduce + (pmap f xs)))

(defn prod-map [f xs]
  (transduce (map f) * xs))

(defn find-first [pred xs]
  (reduce
   (fn [_ x]
     (when (pred x) (reduced x)))
   nil
   xs))


(defn gcd
  "Greatest common divisor."
  (^long [] 1)
  (^long [x] x)
  (^long [^long a ^long b]
   (if (zero? b) a
       (recur b (rem a b)))))

(defn lcm
  "Least common multiple."
  (^long [] 1)
  (^long [x] x)
  (^long [^long a ^long b]
   (/ (* a b)
      (gcd a b))))


(defn count-digits
  "Slightly faster than `((comp count str) n)`."
  ^long [^long n]
  (cond
    (zero? n) 1
    (neg? n) (count-digits (- n))
    :else (-> n
              math/log10
              math/floor
              long
              inc)))

(defn sign ^long [^long x]
  (cond
    (pos? x) 1
    (neg? x) -1
    :else 0))









;; ## Need for Speed
;;
;; These functions should be faster than their counterparts in the Clojure's
;; standard library.
;;
(defn none?
  "A faster version of `not-any?`."
  [pred xs]
  (reduce
   (fn [acc x]
     (if (pred x)
       (reduced false)
       acc))
   true
   xs))

(defn array-none?
  "Much much faster version of `not-any?` for long-arrays."
  [pred ^longs arr]
  (loop [idx (dec (alength arr))
         acc true]
    (if (neg? idx)
      acc
      (if (pred (aget arr idx))
        false
        (recur (dec idx) acc)))))


(defn update-2
  "Usually faster than the `update-in` built-in."
  [m k1 k2 f]
  (let [m2 (m k1)
        v (m2 k2)]
    (assoc m k1 (assoc m2 k2 (f v)))))

(defn assoc-2
  "Usually faster than the `assoc-in` built-in."
  [m k1 k2 v]
  (let [m2 (m k1)]
    (assoc m k1 (assoc m2 k2 v))))

(defn assoc-3
  "Usually faster than the `assoc-in` built-in."
  [m k1 k2 k3 v]
  (let [m2 (m k1)]
    (assoc m k1 (assoc-2 m2 k2 k3 v))))
