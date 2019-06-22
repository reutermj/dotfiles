(ns noise-code.perlin)

(defn ffloor [x]
  (if (>= x 0)
    (int x)
    (- (int x) 1)))



(defn perlin [g p interp v]
  (let [vf (map ffloor v)
        vs (map - v vf)
        vf1 (map inc vf)
        vs1 (map #(- % 1) vs)]))