(ns heaps.minheap)

(defn insert [heap value]
  (loop [heap  (conj heap value)
         pos   (- (count heap) 1)
         value value]
    (let [parPos   (quot (- pos 1) 2)
          parValue (get heap parPos ::oob)]
      (if (or (= parValue ::oob) (<= parValue value))
        heap
        (recur (assoc heap pos parValue parPos value) parPos parValue)))))

(defn extract [heap]
  (let [head (first heap)
        value (peek heap)]
    (loop [heap  (-> heap
                     (assoc 0 value)
                     (pop))
           pos   0]
      (let [leftPos    (+ (* pos 2) 1)
            leftValue  (get heap leftPos ::oob)
            rightPos   (+ (* pos 2) 2)
            rightValue (get heap rightPos ::oob)]
        (cond
          (and (not= rightValue ::oob)
               (< rightValue value)
               (<= rightValue leftValue))
            (recur (assoc heap rightPos value pos rightValue) rightPos)
          (and (not= leftValue ::oob)
               (< leftValue value))
            (recur (assoc heap leftPos value pos leftValue) leftPos)
          :else
            [head heap])))))

(extract [0 1 2])
(loop [heap [0 1 1 3 4 5 2]]
  (let [[head tail] (extract heap)]
    (when (seq tail) (recur tail))))
