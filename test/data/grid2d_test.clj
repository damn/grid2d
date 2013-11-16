(ns data.grid2d-test
  (:require [clojure.test :refer :all]
            [data.grid2d :refer :all]))

(deftest vectorgrid-test
  (let [grid (create-grid 2 3 (fn [p] {:foo p}))]
    (is (= (width grid) 2))
    (is (= (height grid) 3))
    (is (= (posis grid) [[0 0] [0 1] [0 2] [1 0] [1 1] [1 2]]))
    (is (= (cells grid) [{:foo [0 0]} {:foo [0 1]} {:foo [0 2]} {:foo [1 0]} {:foo [1 1]} {:foo [1 2]}]))
    (is (= (get grid [1 1]) {:foo [1 1]}))
    (is (nil? (get grid [100 200])))
    (is (= (grid [1 2]) {:foo [1 2]}))))

(deftest seq-test
  (let [grid (create-grid 3 2 identity)]
    (is (= (cells grid) (posis grid))))
  (let [grid (create-grid 2 1 (fn [p] [:foo p]))]
    (is (= (seq grid) [[[0 0] [:foo [0 0]]]
                       [[1 0] [:foo [1 0]]]]))))

(deftest transform-test
  (let [grid (create-grid 50 34 (constantly :foo))]
    (is (every? #(= :foo %) (cells grid)))
    (is (every? #(= :kaboo %) (cells (transform grid (fn [p old] :kaboo)))))
    (is (let [grid (create-grid 2 3 (constantly :foo))]
          (= grid (transform grid (fn [p old] old)))))))

(deftest assoc-test
  (let [grid (create-grid 5 5 identity)]
    (is (= :foo (get (assoc grid [3 3] :foo) [3 3])))))
