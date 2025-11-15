(ns aoc-utils.core
  (:require [clojure.string :as str]
            [clojure.math :as math]
            [clojure.data.int-map :as i]
            [clojure.data.priority-map :refer [priority-map]]))



;; ## Reading input files

(defn read-input
  "Read contents of an input file.

  Assumes:

  - inputs are in the sibling `../inputs` directory
  - inputs have `.txt` extension"
  [file]
  (let [name (if (int? file)
               (format "%02d" file)
               file)]
    (str/trim (slurp (str "../inputs/" name ".txt")))))




;; ## Input parsing

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
  "Parse input string, based on `parse-fn`, which can be a custom
  function or one of the following:

  - `:int` - parse a single integer
  - `:ints`- get all integers
  - `:digits` - extract all single digits
  - `:chars` - make a list of chars
  - `:words` - make a list of words"
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


(defn parse-lines
  "Parse each line of `input` by applying `parse-fn` to it.

  We can pass any function as `parse-fn` or use one of the following:

  - `:int` - parse a single integer
  - `:ints`- get all integers
  - `:digits` - extract all single digits
  - `:chars` - make a list of chars
  - `:words` - make a list of words"
  [input & [parse-fn {:keys [word-sep nl-sep]}]]
  (mapv #(parse-input % parse-fn word-sep)
        (str/split input (or nl-sep #"\n"))))


(defn parse-paragraphs
  "Split `input` into paragraphs (separated by a blank line).
  Parse each paragraph based on `parse-fn`."
  [input & [parse-fn word-sep]]
  (mapv #(parse-lines % parse-fn {:word-sep word-sep})
        (parse-lines input nil {:nl-sep #"\n\n"})))




;; ## Grids

(defn grid->point-map
  "Convert a 2D list of points to a {[x y]: char} hashmap.

  Keep only the points that satisfy a `pred`."
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
  ([v pred] (grid->point-map v pred 1000))
  ([v pred mult] (grid->point-map v pred mult)))


(defn grid->point-set
  "Convert a 2D list of points to a #{[x y]} set.

  Keep only the points that satisfy a `pred`."
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
  ([v pred] (grid->point-set v pred 1000))
  ([v pred mult] (grid->point-set v pred mult)))



(defn points->lines
  "Convert a map/set representation of a grid to a printable string of points."
  [points]
  (if (map? points) (points->lines (set (keys points)))
      (let [x-lim (inc ^long (reduce max (map first points)))
            y-lim (inc ^long (reduce max (map second points)))]
        (str/join \newline
                  (for [y (range y-lim)]
                    (str/join (for [x (range x-lim)]
                                (if (points [x y])
                                  \█ \space))))))))






;; ### 2D grids

(defn manhattan
  "Manhattan distance of a 2D point or between two 2D points."
  (^long [pt] (manhattan pt [0 0]))
  (^long [[^long x1 ^long y1] [^long x2 ^long y2]]
   (+ (abs (- x1 x2))
      (abs (- y1 y2)))))

(defn pt+
  "Sum of two 2D points."
  ^longs [[^long x1 ^long y1] [^long x2 ^long y2]]
  [(+ x1 x2) (+ y1 y2)])

(defn pt-
  "Difference between two 2D points."
  ^longs [[^long x1 ^long y1] [^long x2 ^long y2]]
  [(- x1 x2) (- y1 y2)])

(defn pt*
  "Multiply each coordinate of a 2D point by `magnitude`."
  ^longs [^long magnitude [^long x ^long y]]
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




(def ^:const nb-4
  "Four neighbours of a 2D point."
  [[0 -1] [-1 0] [1 0] [0 1]])

(def ^:const diags
  "Diagonal neighbours of a 2D point."
  [[-1 -1] [1 -1] [-1 1] [1 1]])

(def ^:const nb-5
  "Four neighbours plus a 2D point."
  (conj nb-4 [0 0]))

(def ^:const nb-8
  "Eight neighbours of a 2D point."
  (into nb-4 diags))

(def ^:const nb-9
  "Eight neighbours plus a 2D point."
  (conj nb-8 [0 0]))


(defn neighbours
  "4/5/8/9 neighbours of a 2D point.

  Return only those neighbours which satisfy `pred`."
  (^longs [^long amount pt] (neighbours amount pt identity))
  (^longs [^long amount [^long x ^long y] pred]
   (let [nbs (case amount
               4 nb-4
               5 nb-5
               8 nb-8
               9 nb-9)]
     (for [[^long dx ^long dy] nbs
           :let [nb [(+ x dx) (+ y dy)]]
           :when (pred nb)]
       nb))))





;; ### 3D grids

(defn manhattan-3d
  "Manhattan distance of a 3D point or between two 3D points."
  (^long [p] (manhattan-3d p [0 0 0]))
  (^long [[^long x1 ^long y1 ^long z1] [^long x2 ^long y2 ^long z2]]
   (+ (abs (- x1 x2))
      (abs (- y1 y2))
      (abs (- z1 z2)))))

(defn pt-3d+
  "Sum of two 3D points."
  ^longs [[^long x1 ^long y1 ^long z1]
          [^long x2 ^long y2 ^long z2]]
  [(+ x1 x2) (+ y1 y2) (+ z1 z2)])

(defn pt-3d-
  "Difference between two 3D points."
  ^longs [[^long x1 ^long y1 ^long z1]
          [^long x2 ^long y2 ^long z2]]
  [(- x1 x2) (- y1 y2) (- z1 z2)])

(defn pt-3d*
  "Multiply each coordinate of a 3D point by `magnitude`."
  ^longs [^long magnitude [^long x ^long y ^long z]]
  [(* magnitude x) (* magnitude y) (* magnitude z)])


(defn neighbours-3d
  "Six neighbours of a 3D point, two in each direction."
  [[^long x ^long y ^long z]]
  [[(dec x) y z] [(inc x) y z]
   [x (dec y) z] [x (inc y) z]
   [x y (dec z)] [x y (inc z)]])

(defn inside-3d?
  "Check if a 3D point (x, y, z) is inside of a cube of a given size."
  ([size [x y z]] (inside-3d? size x y z))
  ([size x y z]
   (and (< -1 x size)
        (< -1 y size)
        (< -1 z size))))





;; ## Graph traversal

(def empty-queue
  "An easier-to-type way to create an empty queue."
  clojure.lang.PersistentQueue/EMPTY)


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


(defn dfs
  "Traverse a graph using the DFS alorithm."
  [options]
  (traverse :dfs options))

(defn bfs
  "Traverse a graph using the BFS alorithm."
  [options]
  (traverse :bfs options))

(defn a-star
  "Traverse a graph using the A* alorithm."
  [options]
  (traverse :a-star options))

(defn dijkstra
  "Traverse a graph using the Dijkstra's alorithm."
  [options]
  (traverse :dijk options))





;; ## Utilities

(defn transpose
  "Transform a matrix of rows into a matrix of columns."
  [matrix]
  (apply mapv vector matrix))

(defn indexed
  "Create a seq of `[idx el]` pairs from a `coll`."
  [coll]
  (map-indexed vector coll))

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

(defn sum-map
  "Map a function to a collection and take a sum of the results."
  [f xs]
  (transduce (map f) + xs))

(defn sum-map-indexed
  "Map a function (which takes two arguments `idx` and `el`)
  to a collection and take a sum of the results."
  [f xs]
  (transduce (map-indexed f) + xs))

(defn sum-pmap
  "Parallel map a function to a collection and take a sum of the results."
  [f xs]
  (reduce + (pmap f xs)))

(defn prod-map
  "Map a function to a collection and take a product of the results."
  [f xs]
  (transduce (map f) * xs))

(defn max-map
  "Map a function to a collection and find a maximum value of the results."
  [f xs]
  (reduce max (map f xs)))

(defn max-pmap
  "Parallel map a function to a collection and find a maximum value of the results."
  [f xs]
  (reduce max (pmap f xs)))

(defn find-first
  "Find first element of a collection which satisfies the predicate."
  [pred xs]
  (reduce
   (fn [_ x]
     (when (pred x) (reduced x)))
   nil
   xs))

(defn find-first-index
  "Returns the index of the first element which satisfies the predicate."
  [pred xs]
  (reduce-kv
   (fn [_ idx x]
     (when (pred x) (reduced idx)))
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

(defn sign
  "Sign of a number."
  ^long [^long x]
  (cond
    (pos? x) 1
    (neg? x) -1
    :else 0))

(defn divisible? [^long n ^long d]
  (zero? ^long (mod n d)))




;; ## Need for Speed

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
