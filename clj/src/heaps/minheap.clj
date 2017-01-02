(ns heaps.minheap)

(defn index-parent [index]
  (quot (- index 1) 2))

(defn- index-depth [index]
  (dec (int (Math/ceil (/ (Math/log (+ index 2)) (Math/log 2))))))

(defn minheap?
  "Returns true if x is a vector and contains a valid minheap, else false"
  [x]
  (and (vector? x)
       (loop [[[index value] & tail] (drop 1 (map-indexed list x))]
         (cond
          (nil? index) true
          (< value (x (index-parent index))) false
          :else (recur tail)))))

(defn minheap
  "Creates a minheap if args constitute a valid heap, else nil"
  [& args]
  (if (zero? (count args))
    []
    (let [heap (vec args)]
      (and (minheap? heap) heap))))

(defn insert
  "inserts value into minheap (vector)"
  [heap value]
  {:pre [(vector? heap)]}
  (loop [v     (conj heap value)
         index (- (count v) 1)
         value value]
    (let [par-index (index-parent index)
          par-value (get v par-index ::oob)]
      (if (or (= par-value ::oob) (<= par-value value))
        v
        (recur (assoc v index par-value par-index value) par-index value)))))

(defn extract
  "extracts minimum value from minheap (vector)"
  [heap]
  {:pre [(vector? heap)]}
  (let [head  (first heap)
        value (peek heap)]
    (loop [heap  (-> heap (assoc 0 value) (pop))
           index 0]
      (let [left-index  (+ (* index 2) 1)
            right-index (+ (* index 2) 2)
            left-value  (get heap left-index ::oob)
            right-value (get heap right-index ::oob)]
        (cond
          (and (not= right-value ::oob)
               (< right-value value)
               (<= right-value left-value))
            (recur (assoc heap right-index value index right-value) right-index)
          (and (not= left-value ::oob)
               (< left-value value))
            (recur (assoc heap left-index value index left-value) left-index)
          :else
            [head heap])))))

(defn ->rows
  "converts heap to a sequence of sequences, blocked by depth"
  [heap]
  {:pre [(vector? heap)]}
  (->> heap
    (map-indexed list)
    (partition-by (comp index-depth first))
    (map #(map second %))))

(defn pprint
  "pretty prints (non-empty) heap"
  [heap]
  {:pre [(vector? heap)]}
  (doseq [slice (->rows heap)]
    (println "-" (clojure.string/join " " slice))))
