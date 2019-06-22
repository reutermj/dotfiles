(ns noise-code.gradients
  (:require [swiss.arrows :refer :all]))

(defn gradients [g]
  (fn [p i]
    (-<>> i
          (map p)
          (reduce bit-xor 0)
          (mod <> (count g))
          (get g))))

(defn create-permutation-table [len]
  (let [p (int-array (range len))]
    (doseq [_ (range 3)]                                    ; do this three times
      (doseq [i (range len)]                                ; iterate each element and swap with a random element
       (let [t (aget p i)
             j (rand-int len)]
         (aset p i (aget p j))
         (aset p j t))))
    (fn [i]                                                 ; create a function which gets an element from the table
      (aget p (mod i len)))))                               ; use modulo to ensure the index is always in range