(ns aoc-utils.examples
  {:nextjournal.clerk/visibility {:code :show :result :show}
   :nextjournal.clerk/auto-expand-results? true
   :nextjournal.clerk/toc true}
  (:require [aoc-utils.core :as aoc]
            [nextjournal.clerk :as clerk]))


;; # Helper functions for Advent of Code
;;
;; `aoc-utils` is a collection of various helper functions I usually
;; use for solving Advent of Code tasks.
;;
;; Some highlights:
;; - Read the contents of an input file with `aoc/read-input`.
;;   This is usually just `(aoc/read-input 1)`, and if I need to
;;   read the file containing test input, e.g. `01_test.txt`
;;   then I use `(aoc/read-input "01_test")`
;; - The input file usually contains multiple lines of data,
;;   which are parsed with the `parse-lines` function.
;;   For the details on how to parse different datatypes, see below.
;; - A very frequent task type is the one where we have a 2D-grid.
;;   For converting the input to a datatype that can be easily
;;   used as a grid, see `grid->point-map` and `grid->point-set` functions.
;;   There are also some helpers to manipulate 2D points,
;;   `pt+`, `pt-`, `pt*`, and their 3D equivalents (`pt-3d+`, etc.).
;; - Some tasks are graph traversal problems, and for those
;;   there are four options: `dfs`, `bfs`, `a-star`, `dijkstra`.
;;   These try to be as general as possible to fit different tasks,
;;   but they are not as performant as a specialized function for
;;   a given task.
;; - Out of all utility functions, the one most commonly used is
;;   probably `transpose`, which is a shortcut for switching
;;   from row- to column-representation of the data.




;; ## Reading input files
;;
;; My inputs are always in the `../inputs/` directory,
;; named `dd`, i.e. have two digits, and have a `.txt` extension.
;; With `aoc/read-input` allows `(read-input 1)` to read the contents of a `01.txt` file.




;; ## Input parsing
;;
(aoc/integers "12ab34 56 78.45")

(aoc/string->digits "1239")


;; The `aoc/parse-input` is doing some heavy lifting.\
;; The `parse-fn` parameter there is the key to the versatility:
;; it makes possible to parse all AoC inputs, either by the typical
;; functions (parsing integers, extracting integers from a line, creating a
;; vector of integers, vector of characters, splitting lines into words, or
;; keeping everything as it is) or by passing it a custom function, specially
;; crafted for the task at hand.
;;
;; It is usually used on single-line inputs.

^{:nextjournal.clerk/visibility {:code :fold :result :show}}
(let [inputs ["123" "abc12def34" "abc12def34" "ab1c" "abc def"]
      fns [:int :ints :digits :chars :words]
      results (map (fn [line parse-fn] (aoc/parse-input line parse-fn))
                   inputs
                   fns)]
  (clerk/html
   [:div.flex.justify-center
    (clerk/table {"Input" inputs
                  "Parse function" fns
                  "Result" results})]))



;; Parsing a whole multi-line input file is just a matter of applying
;; that function to each line of the input, and there is
;; `aoc/parse-lines` that does exactly that:

(def comma-sep-lines "ab cd,ef gh\nij kl,mn op")

(aoc/parse-lines comma-sep-lines :words)
(aoc/parse-lines comma-sep-lines :words {:word-sep #","})
(aoc/parse-lines comma-sep-lines :words {:word-sep #",| "})


^{:nextjournal.clerk/visibility {:code :fold :result :show}}
(let [lines ["1 2 3\n4 -5 6\n7 8 9"
             "abc def\nghi jkl"
             "abc\ndef\nghi"]
      fns [:ints :words :chars]
      results (map (fn [l f] (aoc/parse-lines l f)) lines fns)]
  (clerk/html
   [:div.flex.justify-center
    (clerk/table {"Lines" lines
                  "Parse function" fns
                  "Result" results})]))





;; On some rare occasions, the input is split into paragraphs by having an
;; empty line between different parts of input.
;; We have a function for that too:
;;
(def int-paragraphs "1,2\n3,4\n\n5,6\n7,8")

(aoc/parse-paragraphs int-paragraphs)
(aoc/parse-paragraphs int-paragraphs :ints)
(aoc/parse-paragraphs int-paragraphs :words #",")





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

(def grid ["#.." "..#" "##."])

(aoc/grid->point-map grid #{\#})
(aoc/grid->hashed-point-map grid #{\#})

(aoc/grid->point-set grid #{\#})




;; Sometimes we need to inspect a grid.
;; With `points->lines`, we create a printable string of all
;; points in the grid.

(let [grid ["#.." "..#" "##."]]
  (aoc/points->lines (aoc/grid->point-map grid #{\#})))





;; ### 2D grids
;;
;; We also need some functions to navigate through the grids or do
;; stuff with the points in them:

(def pt1 [2 3])
(def pt2 [7 -5])

(aoc/manhattan pt2)
(aoc/manhattan pt1 pt2)

(aoc/pt+ pt1 pt2)
(aoc/pt- pt1 pt2)
(aoc/pt* 2 pt1)

(aoc/left-turn [1 0])
(aoc/right-turn [1 0])

(aoc/inside? 5 [3 4])
(aoc/inside? 5 [-1 4])
(aoc/inside? 5 [5 4])

(aoc/neighbours 4 [0 0])
(aoc/neighbours 8 [0 0])




;; Different amount of neighbours of a point:

^{:nextjournal.clerk/visibility {:code :fold :result :show}}
(let [point [0 0]
      nbs [4 5 8 9]
      results (map #(aoc/neighbours % point) nbs)
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




;; ### 3D grids

(def pt3 [7 -2 4])
(def pt4 [-2 11 13])

(aoc/manhattan pt3)
(aoc/manhattan pt3 pt4)

(aoc/pt-3d+ pt3 pt4)
(aoc/pt-3d- pt3 pt4)
(aoc/pt-3d* 2 pt3)

(aoc/neighbours-3d [0 0 0])

(aoc/inside-3d? 8 [2 3 4])
(aoc/inside-3d? 8 [-2 3 4])





;; ## Graph traversal
;;
;; Graph traversal problems are relatively frequent in AoC.
;;
;; I've written a huge general function for traversing grids, but
;; it remains to be seen how useful it'll be for specific tasks with
;; their specific needs: I feel like in the recent years the graph traversal
;; tasks always had some gotcha which made it harder to use a general algorithm,
;; rather than a specific one written for the task at hand.
;;
;; If this gets used, the implementation details will probably change,
;; depending on the specific tasks.
;; (As if the (private) `traverse` function is not already way too long
;; and complicated.
;;
;; All four algorithms (`DFS`, `BFS`, `Dijkstra`, `A*`) share the same logic,
;; the difference is in a `queue` type, they are available as functions:
;; - `aoc/dfs`
;; - `aoc/bfs`
;; - `aoc/dijkstra`
;; - `aoc/a-star`

^{:nextjournal.clerk/visibility {:code :show :result :show}}
(let [walls #{[0 1] [1 1] [1 3] [2 3] [4 0] [3 1] [4 2] [3 3] [4 4]}
      start [0 0]
      end [3 4]
      size 5]
  [(aoc/points->lines walls)
   (aoc/bfs {:start start
             :end end
             :walls walls
             :size size})])

;; The list of possible keys passed in the options map is quite large:
;; ```
;; [start end walls size size-x size-y
;;  nb-func nb-num nb-cond end-cond
;;  cost-fn heuristic steps-limit
;;  allow-revisits? side-effect]
;; ```
;;
;; It's best to read the function's source code to understand what each does.







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

(aoc/transpose [[1 2] [3 4]])

(aoc/indexed [10 20 30])

(aoc/count-if even? [2 3 4 5 6])

(aoc/do-count [x (range 10)
               :while (< x 3)
               y (range 10)
               :when (or (< y 2) (= y 9))])

(aoc/find-first even? [1 3 5 6 7 8])

(aoc/gcd 25 15)

(aoc/lcm 25 15)

(aoc/count-digits 99)
(aoc/count-digits 100)

(aoc/sign 10)
(aoc/sign 0)

(aoc/sum-map #(* 2 %) (range 5))
(aoc/prod-map #(* 2 %) (range 1 5))
