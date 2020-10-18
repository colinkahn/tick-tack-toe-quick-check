(ns tick-tack-toe.pbt
  (:require [clojure.test.check :refer [quick-check]]
            [clojure.test.check.properties :refer [for-all]]
            [clojure.test.check.generators :as gen]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as st]))

(def magic-square
  [[4 9 2]
   [3 5 7]
   [8 1 6]])

(def positions (set (for [x (range 3)
                          y (range 3)]
                      [x y])))

(def winning-positions
  (set (for [a positions
             b positions
             c positions
             :when (distinct? a b c)
             :when (= 15 (->> [a b c]
                              (map #(get-in magic-square %))
                              (apply +)))]
         #{a b c})))

(defn winning-player [board]
  (some (fn [positions]
          (let [bvs (map #(get-in board %) positions)]
            (when (and (every? some? bvs)
                       (apply = bvs))
              (first bvs))))
        winning-positions))

(defn ->board []
  (vec (repeat 3 (vec (repeat 3 nil)))))

(def gen-op (gen/hash-map :player (gen/elements [:x :y])
                          :position (gen/elements positions)))

(defn apply-op [ctx op]
  (let [prev-board (or (last (:boards ctx))
                       (->board))
        next-board (assoc-in prev-board (:position op) (:player op))]
    (cond-> ctx
      (and (nil? (:winning-player ctx))
           (= (:active-player ctx) (:player op))
           (nil? (get-in prev-board (:position op))))
      (->
        (assoc :active-player ({:x :y :y :x} (:active-player ctx))
               :winning-player (winning-player next-board))
        (update :boards (fnil conj []) next-board)))))

(defn single-plays?
  "Checks that all boards only have a single players move per position and that
  each successive board has one more play than the last."
  [boards]
  (or (<= (count boards) 1)
      (and (every? (fn [pos]
                     (let [moves (->> boards
                                      (map #(get-in % pos))
                                      (remove nil?))]
                       (or (empty? moves)
                           (apply = moves))))
                   positions)
           (->> boards
                (map (fn [rows]
                       (->> rows
                            (mapcat identity)
                            (remove nil?)
                            (count))))
                (reduce (fn [t n]
                          (if (= (inc t) n)
                            n
                            (reduced false))))
                number?))))

(defn win-terminates?
  "Checks that there is a single winning board state."
  [boards]
  (->> boards
       (drop-while #(not (winning-player %)))
       (drop 1)
       empty?))

(s/def ::player #{:x :y})
(s/def ::move (s/or :empty nil? :played ::player))
(s/def ::row (s/tuple ::move ::move ::move))
(s/def ::board (s/tuple ::row ::row ::row))
(s/def ::boards (s/coll-of ::board))
(s/def ::active-player #{:x :y})
(s/def ::ctx (s/and (s/keys :req-un [::boards ::active-player])
                    (fn [{:keys [boards active-player]}]
                      (and (single-plays? (s/unform ::boards boards))
                           (win-terminates? (s/unform ::boards boards))))))
(s/def ::position positions)
(s/def ::op (s/keys :req-un [::position ::player]))

(s/fdef apply-op
        :args (s/cat :ctx ::ctx :op ::op)
        ::ret ::ctx)

(defn minimal-win []
  (quick-check 50
               (for-all [ops (gen/vector gen-op 1 100)]
                        (let [ctx (reduce apply-op {:active-player :x :boards []} ops)]
                          ; to shrink, we want it to fail when we have a win
                          (nil? (:winning-player ctx))))))

(st/instrument)

(comment
  (def res (minimal-win))
  (get-in res [:fail])
  (get-in res [:shrunk :smallest]))
