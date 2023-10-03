(ns jungon.tictactoe-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [jungon.tictactoe :refer [computer-move]]))

(deftest computer-move-test
  (testing "Test that computer can move to board"
    (is (= [["C"]]
           (computer-move [["B"]])))
    (is (= [["P"]]
           (computer-move [["P"]]))))) 