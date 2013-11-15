(ns data.grid2d)

; 2dimvector is 7x faster than a map of [x y] to values

; TODO add tests
; -> add documentation/docs? to the protocol not transform-values private function !
;     documentation for example: supports get/assoc/seq (doseq,map,..)/contains?/IFn/=
; -> just one test of 30x30 50x50 100x100 maps of hashmap, vector, vector-of-vectors and get fields should suffice
; -> check for filetype linux and no extra whitespaces before linebreaks? and ends with newline?
; -> maybe look for open source guidelines, license?

(defprotocol Grid2D
  (transform [this f] "f should be a function of [posi old-value] that returns new-value.")
  (posis [this])
  (cells [this])
  (width [this])
  (height [this]))

(defn- transform-values [data width height f]
  (mapv
    (fn [x] (loop [v (transient (get data x))
                   y 0]
              (if (not= y height)
                (recur (assoc! v y (f [x y] (get v y)))
                       (inc y))
                (persistent! v))))
    (range width)))

; TODO could memoize the following functions: posis,cells,width,height,seq. Or put in data vector metadata?
; yeah man how to memoize it ??
; deftype is a low level construct -> just supply additional arguments?
; or use delays internally
; additional fields posis,cells,width,height,seq all as delays, or in an additional map of stuff?
(deftype VectorGrid [data]

  Grid2D
  (transform [this f]
    (VectorGrid. (transform-values data (width this) (height this) f)))
  (posis [this]
    (for [x (range (width this))
          y (range (height this))]
      [x y]))
  (cells [this] (apply concat data))
  (width [this] (count data))
  (height [this] (count (data 0)))

  clojure.lang.ILookup
  (valAt [this k] (get-in data k))

  clojure.lang.IFn
  (invoke [this k] (.valAt this k)) ; get-in quicker! .valAt is redirection!

  clojure.lang.Seqable
  (seq [this] 
    (map #(vector %1 %2) (posis this) (cells this)))

  clojure.lang.IPersistentCollection
  (equiv [this obj]
    (and (= VectorGrid (class obj)) ; TODO equiv should implement value and not type comparison?
         (= (.data ^VectorGrid obj) data)))

  clojure.lang.Associative
  (assoc [this k v]
    (VectorGrid. (assoc-in data k v)))
  (containsKey [this [x y]]
    (and (contains? data x)
         (contains? (data 0) y)))
  (entryAt [this k]));returns IMapEntry, used in find

(defn- vector2d [w h f]
  (mapv (fn [x] (mapv (fn [y] (f [x y]))
                      (range h)))
        (range w)))

(defn create-grid
  "Creates a 2 dimensional grid.
  xyfn is a function is applied for every [x y] to get value."
  [w h xyfn]
  (VectorGrid. (vector2d w h xyfn)))

;;

(defn print-grid [grid & {print-cell :print-cell
                          :or {print-cell
                               #(print (case % :wall "#" :ground "_" "?"))}}]
  (doseq [y (range (height grid))]
    (doseq [x (range (width grid))]
      (print-cell (grid [x y])))
    (println)))

;;

(defn mapgrid->vectorgrid
  "Transforms a grid of {position value} to a grid2d.
  Returns [grid convert-fn]: convert-fn converts a position of the old grid to a position of the new one."
  [grid calc-newgrid-value] 
  (let [posis (keys grid)
        xs (map #(% 0) posis)
        min-x (apply min xs)
        max-x (apply max xs)
        ys (map #(% 1) posis)
        min-y (apply min ys)
        max-y (apply max ys)
        width (inc (- max-x min-x))
        height (inc (- max-y min-y))
        convert (fn [[x y]] [(- x min-x -1) 
                             (- y min-y -1)])]
    ; +2 so there are walls on all borders around the farthest ground cells
    [(create-grid (+ width 2) (+ height 2) 
                  (fn [[x y]]
                    ; new grid starts 1 left/top of leftest cell
                    (calc-newgrid-value (get grid [(+ x min-x -1) 
                                                   (+ y min-y -1)]))))
     convert]))

;;

(defn get-4-neighbour-positions [[x y]]
  [[(inc x) y] 
   [(dec x) y] 
   [x (inc y)] 
   [x (dec y)]])

(defn get-8-neighbour-positions [[x y]]
  (for [tx (range (dec x) (+ x 2))
        ty (range (dec y) (+ y 2))
        :when (not= [x y] [tx ty])]
    [tx ty]))

;;

; Could put width and height as deftype arguments instead of protocol functions 
(comment (let [g (create-grid 5 5 identity)]
           (compare-times 100000
                          (.width ^VectorGrid g) ; 1.575   ms
                          (.width g)             ; 305.477 ms
                          (width g))))           ; 2.761   ms
 
; Could use something like (make-grid (map f grid)) instead transform-values, because seq is implemented
; but keeping it as it is for reasons of speed -> what about mapv? 
; Assert (cells) entsprechen  (getposis) of identity grid for seq and doseq [position cell]
; Assert >=1 w and h and integer 
; TODO !! throw illegalargumentexception when argument is not 2 dimensional vector? or does this slow down get too much ? not for example get :keyword -> nth not supported on type :keyword ..
;Implementing equiv because else there is an error: (create-grid 10 10 identity)
;#<VectorGrid game.utils.grid2d.VectorGrid@6efdebd>
;AbstractMethodError   clojure.lang.Util.pcequiv  (Util.java:75)

;;{:pre [(contains? grid [x y])]} ; always check and give indexoutofbounds for get, assoc, whatever, transform , ... 
; use contains-keys? rename contains-ks?
  
; replace by update-in but then have to [p] ... but can have more keys in the p thing ...
; also supporting update-in when somewhere it would be easier to use

; (map grid posis) // get multiple posis ... 
; TODO could do this faster  , at-posis ... chunked get? overkill? 
; if multiple posis in same row => only 1 row access 

;Could also use a dogrid or docells function instead of doseq [p cell] -> could be a lot faster
;or just memoize seq
;Time of:  (docells grid myfunc)
;"Elapsed time: 2.455 msecs"
;Time of:  (doseq [[p cell] grid] (myfunc p cell))
;"Elapsed time: 6.649 msecs"
;Time of:  (doseq [cell (cells grid) p (posis grid)] (myfunc p cell))
;"Elapsed time: 442.595 msecs"

; say alternatives: 1 dimensional grid, mapgrid, java array grid? 
; whats quicker for lookup?
; No way to implement keys/vals instead of posis/cells? kecause keys -> APersistentMap.Keyseq ...? no interface
