(ns heaps.minheap-test
  (:require [heaps.minheap :refer :all]
            [clojure.test :refer [deftest is]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))

(defn extracted?
  "Returns true if an extract succeeded, else false"
  [old-heap value new-heap]
  (and (= (inc (count new-heap)) (count old-heap))
       (minheap? new-heap)
       (= value (first old-heap))))

(defn inserted?
  "Returns true if an insert succeeded, else false"
  [old-heap value new-heap]
  (let [old-top (first old-heap)
        new-top (first new-heap)]
    (and (= (inc (count old-heap)) (count new-heap))
         (minheap? new-heap))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest insert-test
  (is (insert (minheap) 0) (minheap 0))
  (is (insert (minheap 0) 1) (minheap 0 1))
  (is (insert (minheap 1) 0) (minheap 0 1))
  (is (insert (minheap 0 1 2 3 4 5) 1) (minheap 0 1 1 3 4 5 2)))

(def extract-test-data
  (list (minheap 0)
        (minheap 0 1)
        (minheap 0 1 2)
        (minheap 0 2 1)
        (minheap 0 1 1 3 4 5 2)))

(deftest extract-test
  (doseq [datum extract-test-data]
    (loop [heap datum]
      (when (seq heap)
        (let [[top popped] (extract heap)]
          (is (extracted? heap top popped))
          (recur popped))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn sum-lineage
  "Returns sum of ancestor values from v[0]..v[i]"
  [v i]
  (->> (inc i)
       (iterate #(quot % 2))
       (take-while pos?)
       (map dec)   ;; indices of self+ancestors
       (map v)     ;; values
       (apply +))) ;; sum

;; make a heap from an arbitrary sequence of non-negative numbers; it is O(n ln
;; n), could be O(n) if we walked forward and only referred to the single
;; parent, already accumulated.
(defn non-negs->heap
  "Returns a heap constructed from an arbitrary sequences of non-negative numbers"
  [non-negs]
  (mapv (partial sum-lineage non-negs) (range (count non-negs))))

;; test heap generation itself, to make sure we always generate heaps
(def heap-property
  (prop/for-all [v (gen/vector gen/nat)]
    (let [heap (non-negs->heap v)]
      (minheap? heap))))

(def heap-insert-property
  (prop/for-all [non-negs (gen/vector gen/nat)
                 value    gen/nat]
    (let [pre  (non-negs->heap non-negs)
          post (insert pre value)]
      (inserted? pre value post))))

(defspec heap-test-insert 1000 heap-insert-property)

(def heap-extract-property
  (prop/for-all [non-negs (gen/not-empty (gen/vector gen/nat))]
    (let [pre          (non-negs->heap non-negs)
          [value post] (extract pre)]
      (extracted? pre value post))))

(defspec heap-test-extract 1000 heap-extract-property)
