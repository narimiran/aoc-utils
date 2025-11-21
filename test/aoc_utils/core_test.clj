(ns aoc-utils.core-test
  (:require
   [aoc-utils.core :as aoc]
   [clojure.test :refer [deftest testing is]]))


(def int-lines "123\n-456\n789")
(def ints-lines "1 2 3\n4 -5 6\n7 8 9")
(def char-lines "abc\ndef\nghi")
(def word-lines "a b c\nd e f\ng h i")
(def comma-sep "ab cd,ef gh\nij kl,mn op")
(def int-paragraphs "1,2\n3,4\n\n5,6\n7,8")

(defn test-parsing
  ([input result] (test-parsing input nil result))
  ([input f result]
   (is (= result (aoc/parse-lines input f)))))

(deftest parsing
  (testing "digits"
    (is (= [1 2 3] (aoc/string->digits "123")))
    (is (= [1 2] (aoc/string->digits "ab1cd2e"))))
  (testing "ints"
    (test-parsing int-lines         ["123" "-456" "789"])
    (test-parsing int-lines :int    [123 -456 789])
    (test-parsing int-lines :digits [[1 2 3] [4 5 6] [7 8 9]])
    (test-parsing ints-lines :ints  [[1 2 3] [4 -5 6] [7 8 9]]))
  (testing "chars"
    (test-parsing char-lines        ["abc" "def" "ghi"])
    (test-parsing char-lines :chars [[\a \b \c] [\d \e \f] [\g \h \i]]))
  (testing "words"
    (test-parsing word-lines        ["a b c" "d e f" "g h i"])
    (test-parsing word-lines :words [["a" "b" "c"] ["d" "e" "f"] ["g" "h" "i"]]))
  (testing "custom func"
    (test-parsing int-lines #(mod (abs (parse-long %)) 10) [3 6 9]))
  (testing "separators"
    (is (= [["ab" "cd,ef" "gh"] ["ij" "kl,mn" "op"]]
           (aoc/parse-lines comma-sep :words)))
    (is (= [["ab cd" "ef gh"] ["ij kl" "mn op"]]
           (aoc/parse-lines comma-sep :words {:word-sep #","})))
    (is (= [["ab" "cd" "ef" "gh"] ["ij" "kl" "mn" "op"]]
           (aoc/parse-lines comma-sep :words {:word-sep #",| "}))))
  (testing "pragraphs"
    (is (= ["1,2\n3,4" "5,6\n7,8"]
           (aoc/parse-lines int-paragraphs nil {:nl-sep #"\n\n"})))
    (is (= [[1 2 3 4] [5 6 7 8]]
           (aoc/parse-lines int-paragraphs :ints {:nl-sep #"\n\n"})))
    (is (= [["1,2" "3,4"] ["5,6" "7,8"]]
           (aoc/parse-paragraphs int-paragraphs)))
    (is (= [[[1 2] [3 4]] [[5 6] [7 8]]]
           (aoc/parse-paragraphs int-paragraphs :ints)))
    (is (= [[["1" "2"] ["3" "4"]] [["5" "6"] ["7" "8"]]]
           (aoc/parse-paragraphs int-paragraphs :words #",")))))




(def input-grid "#..\n..#\n##.")
(def grid (aoc/parse-lines input-grid :chars))
(def grid-print "█  \n  █\n██ ")
(def walls {[0 0] \# , [2 1] \# , [0 2] \# , [1 2] \#})
(def hashed-walls {0 \# , 1002 \# , 2000 \# , 2001 \#})
(def custom-hashed-walls {0 \# , 102 \# , 200 \# , 201 \#})

(def input-grid-2 "#123\n4.#5\n##bc")
(def grid-2 (aoc/parse-lines input-grid-2 :chars))
(def numbers {[1 0] \1 [2 0] \2 [3 0] \3
              [0 1] \4 [3 1] \5})
(def letters {[2 2] \b [3 2] \c})

(deftest grids
  (testing "grid-get"
    (is (= \# (aoc/grid-get grid [0 0])))
    (is (= \# (aoc/grid-get grid 0 0)))
    (is (nil? (aoc/grid-get grid [-1 0])))
    (is (nil? (aoc/grid-get grid -1 0)))
    (is (= :foo (aoc/grid-get grid [-1 0] :foo)))
    (is (= :foo (aoc/grid-get grid -1 0 :foo))))

  (testing "create grid"
    (let [g (aoc/create-grid grid {\# :walls})]
      (is (= walls (:walls g)))
      (is (= 3 (:width g)))
      (is (= 3 (:height g)))
      (is (= 3 (:size g))))
    (is (= hashed-walls (:walls (aoc/create-hashed-grid grid {\# :walls}))))
    (is (= custom-hashed-walls (:walls (aoc/create-hashed-grid grid {\# :walls} 100)))))

  (testing "create grid 2"
    (let [g (aoc/create-grid grid-2 {\# :walls
                                     Character/isDigit :numbers
                                     #{\a \b \c} :letters})]
      (is (= walls (:walls g)))
      (is (= numbers (:numbers g)))
      (is (= letters (:letters g)))
      (is (= 4 (:width g)))
      (is (= 3 (:height g)))
      (is (nil? (:size g)))))

  (testing "show-grid"
    (is (= grid-print (aoc/show-grid walls)))
    (is (= grid-print (aoc/show-grid (set (keys walls)))))))




(def pt1 [2 3])
(def pt2 [7 -5])


(deftest points
  (testing "points"
    (is (= 12 (aoc/manhattan pt2)))
    (is (= 13 (aoc/manhattan pt1 pt2)))
    (is (= [9 -2] (aoc/pt+ pt1 pt2)))
    (is (= [5 -8] (aoc/pt- pt2 pt1)))
    (is (= [-5 8] (aoc/pt- pt1 pt2)))
    (is (= [4 6] (aoc/pt* 2 pt1)))
    (is (= [-21 15] (aoc/pt* -3 pt2))))

  (testing "turns"
    (is (= [0 1] (aoc/left-turn [-1 0])))
    (is (= [0 1] (aoc/right-turn [1 0])))
    (is (= [1 0] (aoc/left-turn [0 1])))
    (is (= [1 0] (aoc/right-turn [0 -1])))
    (is (= [0 -1] (aoc/left-turn [1 0])))
    (is (= [0 -1] (aoc/right-turn [-1 0])))
    (is (= [-1 0] (aoc/left-turn [0 -1])))
    (is (= [-1 0] (aoc/right-turn [0 1]))))

  (testing "inside"
    (is (aoc/inside? 10 5 7))
    (is (not (aoc/inside? 10 5 17)))
    (is (aoc/inside? 10 0 0))
    (is (not (aoc/inside? 10 0 -1)))
    (is (aoc/inside? 10 20 9 17))
    (is (not (aoc/inside? 10 20 17 9))))

  (testing "neighbours"
    (is (= #{      [2 2]
             [1 3]       [3 3]
                   [2 4]}       (set (aoc/neighbours-4 pt1))))

    (is (= #{[1 3]       [3 3]} (set (aoc/neighbours-4 pt1 (fn [[x _]] (not= x 2))))))

    (is (= #{[1 2]       [3 2]

             [1 4]       [3 4]} (set (aoc/diagonals pt1))))

    (is (= #{[1 2]       [3 2]} (set (aoc/diagonals pt1 (fn [[_ y]] (not= y 4))))))

    (is (= #{[1 2] [2 2] [3 2]
             [1 3]       [3 3]
             [1 4] [2 4] [3 4]} (set (aoc/neighbours-8 pt1))))

    (is (= #{[1 2] [2 2] [3 2]
             [1 3]
             [1 4]       [3 4]} (set (aoc/neighbours-8 pt1 (fn [[x y]] (not= 6 (+ x y)))))))))


(def pt3 [7 -2 4])
(def pt4 [-2 11 13])

(deftest grid-3d
  (testing "3d points"
    (is (= 13 (aoc/manhattan-3d pt3)))
    (is (= 31 (aoc/manhattan-3d pt3 pt4)))
    (is (= [5 9 17] (aoc/pt-3d+ pt3 pt4)))
    (is (= [9 -13 -9] (aoc/pt-3d- pt3 pt4)))
    (is (= [-9 13 9] (aoc/pt-3d- pt4 pt3)))
    (is (= [14 -4 8] (aoc/pt-3d* 2 pt3)))
    (is (= [20 -110 -130] (aoc/pt-3d* -10 pt4))))

  (testing "neighbours-3d"
    (is (= [[6 -2 4] [8 -2 4] [7 -3 4] [7 -1 4] [7 -2 3] [7 -2 5]]
           (aoc/neighbours-3d pt3))))

  (testing "inside 3d"
    (is (aoc/inside-3d? 8 [2 5 3]))
    (is (aoc/inside-3d? 8 2 5 3))
    (is (not (aoc/inside-3d? 8 [-2 5 3])))
    (is (not (aoc/inside-3d? 8 [2 8 3])))))




(deftest graph-traversal
  (let [walls #{[0 1] [1 1] [1 3] [2 3] [3 0] [3 1] [3 2] [3 3]}
        with-hole (disj walls [3 1])
        start [0 0]
        end [4 3]
        size 5]
    (testing "dfs"
      (is (= 25 (:count (aoc/dfs {:start start
                                  :end-cond (constantly false)
                                  :size size}))))
      (is (= 13 (:steps (aoc/dfs {:start start
                                  :end end
                                  :walls walls
                                  :size size})))))
    (testing "bfs"
      (is (= 25 (:count (aoc/bfs {:start start
                                  :end-cond (constantly false)
                                  :size size}))))
      (is (= 13 (:steps (aoc/bfs {:start start
                                  :end end
                                  :walls walls
                                  :size size}))))
      (is (= 7 (:steps (aoc/bfs {:start start
                                 :end end
                                 :walls with-hole
                                 :size size})))))
    (testing "dijkstra"
      (is (= 25 (:count (aoc/dijkstra {:start start
                                       :end-cond (constantly false)
                                       :size size}))))
      (is (= 13 (:steps (aoc/dijkstra {:start start
                                       :end end
                                       :walls walls
                                       :size size}))))
      (is (= 7 (:steps (aoc/dijkstra {:start start
                                      :end end
                                      :walls with-hole
                                      :size size})))))
    (testing "a-star"
      (is (= 25 (:count (aoc/a-star {:start start
                                     :end-cond (constantly false)
                                     :size size}))))
      (is (= 13 (:steps (aoc/a-star {:start start
                                     :end end
                                     :walls walls
                                     :size size}))))
      (is (= 7 (:steps (aoc/a-star {:start start
                                    :end end
                                    :walls with-hole
                                    :size size})))))))



(def evens [2 4 6 8 -24 156])
(def stevens [2 4 6 21 32])

(deftest utilities
  (testing "transpose"
    (is (= [[1 3] [2 4]]
           (aoc/transpose [[1 2] [3 4]]))))
  (testing "indexed"
    (is (= [[0 10] [1 20] [2 30]]
           (aoc/indexed [10 20 30])))
    (is (= [[0 [10 20]] [1 [30 40]]]
           (aoc/indexed [[10 20] [30 40]])))
    (is (= [[5 10] [6 20] [7 30]]
           (aoc/indexed [10 20 30] 5)))
    (is (= [[-3 [10 20]] [-2 [30 40]]]
           (aoc/indexed [[10 20] [30 40]] -3))))
  (testing "count-if"
    (is (= 6 (aoc/count-if even? evens)))
    (is (zero? (aoc/count-if odd? evens)))
    (is (= 4 (aoc/count-if even? stevens)))
    (is (= 1 (aoc/count-if odd? stevens))))
  (testing "do-count"
    (is (= 3 (aoc/do-count [_ (range 3)])))
    (is (= 3 (aoc/do-count [x (range 10)
                            :when (< x 3)])))
    (is (= 9 (aoc/do-count [x (range 10)
                            :while (< x 3)
                            y (range 10)
                            :when (or (< y 2) (= y 9))]))))
  (testing "sum-map-indexed"
    (is (= 12 (aoc/sum-map-indexed
               (fn [idx _] (* 2 idx))
               [10 20 30 40])))
    (is (= 80 (aoc/sum-map-indexed
               (fn [idx el] (* idx el))
               [10 20 30]))))
  (testing "max-map"
    (is (= 10 (aoc/max-map #(* 2 %) (range 6))))
    (is (= 6 (aoc/max-map val {:a 3 :b 6 :c 4}))))
  (testing "find-first"
    (is (= 2 (aoc/find-first even? evens)))
    (is (nil? (aoc/find-first odd? evens)))
    (is (= 21 (aoc/find-first odd? stevens))))
  (testing "find-first-index"
    (is (zero? (aoc/find-first-index even? evens)))
    (is (nil? (aoc/find-first-index odd? evens)))
    (is (= 3 (aoc/find-first-index odd? stevens))))
  (testing "gcd"
    (is (= 1 (aoc/gcd 2 3)))
    (is (= 4 (aoc/gcd 4 12)))
    (is (= 5 (aoc/gcd 25 15)))
    (is (= 1 (aoc/gcd 7 17))))
  (testing "lcm"
    (is (= 6 (aoc/lcm 2 3)))
    (is (= 12 (aoc/lcm 4 12)))
    (is (= 75 (aoc/lcm 25 15)))
    (is (= (* 7 17) (aoc/lcm 7 17))))

  (testing "count digits"
    (is (= 1 (aoc/count-digits 0)))
    (is (= 1 (aoc/count-digits 1)))
    (is (= 1 (aoc/count-digits -1)))
    (is (= 2 (aoc/count-digits 99)))
    (is (= 3 (aoc/count-digits 100))))

  (testing "sign"
    (is (zero? (aoc/sign 0)))
    (is (= 1 (aoc/sign 2)))
    (is (= -1 (aoc/sign -2))))

  (testing "divisible"
    (is (aoc/divisible? 6 2))
    (is (aoc/divisible? 6 -2))
    (is (aoc/divisible? -6 2))
    (is (aoc/divisible? -6 -2))
    (is (not (aoc/divisible? 7 2)))
    (is (not (aoc/divisible? -7 2)))))


(def d {:a {:b 2}
        :c {:d {:e 3}}})

(deftest builtin-alternatives
  (testing "none?"
    (is (aoc/none? odd? evens))
    (is (not (aoc/none? odd? stevens))))
  (testing "update-2"
    (is (= (update-in d [:a :b] inc)
           (aoc/update-2 d :a :b inc))))
  (testing "assoc-2"
    (is (= (assoc-in d [:a :b] 5)
           (aoc/assoc-2 d :a :b 5))))
  (testing "assoc-3"
    (is (= (assoc-in d [:c :d :e] 7)
           (aoc/assoc-3 d :c :d :e 7)))))
