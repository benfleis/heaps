(ns heaps.minheap-test
  (:use clojure.test)
  (:use heaps.minheap))

(defn- h [& args]
  (cond 
   (zero? (count args)) []
   ; validate
   :else (vec args)))

(deftest test-insert
  (is (insert (h) 0) (h 0))
  (is (insert (h 0) 1) (h 0 1))
  (is (insert (h 1) 0) (h 0 1))
  (is (insert (h 0 1 2 3 4 5) 1) (h 0 1 1 3 4 5 2)))

(def ^:private test-extract-data
  (list (h 0)
        (h 0 1)
        (h 0 1 2)
        (h 0 2 1)
        (h 0 1 1 3 4 5 2)))

(deftest test-extract
  (doseq [datum test-extract-data]
    (loop [heap datum]
      (when (seq heap)
        (let [[top popped] (extract heap)]
          (is (= (count popped) (dec (count heap))))
          (is (= top (apply min heap)))
          (recur popped))))))

(run-tests)
