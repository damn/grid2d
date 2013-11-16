# grid2d

A simple two dimensional grid data structure that is implemented as vectors of vectors.

## Usage

Grids implement clojure.lang.ILookup, IFn, Seqable, Associative and a new Grid2D protocol.

So you can use get, assoc, contains?, seq (and map,for,doseq...) on it.

``` clojure
(defprotocol Grid2D
  (transform [this f] "f should be a function of [posi old-value] that returns new-value.")
  (posis [this])
  (cells [this])
  (width [this])
  (height [this]))
```

``` clojure
user=> (use 'data.grid2d)
nil
user=> (def mygrid (create-grid 3 4 (fn [[x y]] {:cell [x y]})))
#'user/mygrid
user=> mygrid
#<VectorGrid width 3, height 4>
user=> (cells mygrid)
({:cell [0 0]} {:cell [0 1]} {:cell [0 2]} {:cell [0 3]} {:cell [1 0]} {:cell [1 1]} {:cell [1 2]} {:cell [1 3]} {:cell [2 0]} {:cell [2 1]} {:cell [2 2]} {:cell [2 3]})
user=> (posis mygrid)
([0 0] [0 1] [0 2] [0 3] [1 0] [1 1] [1 2] [1 3] [2 0] [2 1] [2 2] [2 3])
user=> (width mygrid)
3
user=> (height mygrid)
4
user=> (get mygrid [2 1])
{:cell [2 1]}
user=> (mygrid [2 1])
{:cell [2 1]}
user=> (clojure.pprint/pprint (.data mygrid))
[[{:cell [0 0]} {:cell [0 1]} {:cell [0 2]} {:cell [0 3]}]
 [{:cell [1 0]} {:cell [1 1]} {:cell [1 2]} {:cell [1 3]}]
 [{:cell [2 0]} {:cell [2 1]} {:cell [2 2]} {:cell [2 3]}]]
nil
user=> (clojure.pprint/pprint (.data (transform mygrid (fn [p val] {:foo p}))))
[[{:foo [0 0]} {:foo [0 1]} {:foo [0 2]} {:foo [0 3]}]
 [{:foo [1 0]} {:foo [1 1]} {:foo [1 2]} {:foo [1 3]}]
 [{:foo [2 0]} {:foo [2 1]} {:foo [2 2]} {:foo [2 3]}]]
nil
user=> (contains? mygrid [100 200])
false
user=> (contains? mygrid [2 3])
true
user=> (def mygrid (apply assoc mygrid (interleave (posis mygrid) (repeat :mouse))))
#'user/mygrid
user=> (clojure.pprint/pprint (.data mygrid))
[[:mouse :mouse :mouse :mouse]
 [:mouse :mouse :mouse :mouse]
 [:mouse :mouse :mouse :mouse]]
nil
user=> (def mygrid (assoc mygrid [1 1] :cat))
#'user/mygrid
user=> (clojure.pprint/pprint (.data mygrid))
[[:mouse :mouse :mouse :mouse]
 [:mouse :cat :mouse :mouse]
 [:mouse :mouse :mouse :mouse]]
nil
``` 

## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
