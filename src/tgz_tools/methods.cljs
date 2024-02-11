(ns tgz-tools.methods
  (:require [tgz-tools.util :as util]))

(defn split-player-by-type [players seeds]
  [(filter (fn [name]
             (not (some #{name} seeds)))
           players)
   seeds])

(defn group-of-n-games [indices {:keys [name players]}]
  (map-indexed (fn [game-id player-indices]
                 (cons
                  (str name (inc game-id))
                  (map (partial nth players)
                       player-indices)))
               indices))

(defn to-group [name players]
  {:name name
   :players players})

(defn groups-of-n-split [group-size to-games]
  (fn [players seeds]
    (let
     [[players seeds] (split-player-by-type players seeds)]
      (loop [group-id 65
             players players
             seeds seeds
             groups []
             games []]
        (if (empty? players)
          [groups games]
          (let
           [winner? (seq seeds)
            player-count (if winner? (dec group-size) group-size)
            group (to-group (char group-id)
                            (into (vec (take 1 seeds)) (take player-count players)))]
            (recur (inc group-id)
                   (drop player-count players)
                   (drop 1 seeds)
                   (conj groups group)
                   (into games (to-games group)))))))))

(defn groups-of-n-validate [group-size split]
  (fn [players seeds]
    (let
     [player-count (count players)
      seed-count (count seeds)
      group-count (/ player-count group-size)]
      (cond
        (not (zero? (mod player-count group-size))) [true (str "player count of " player-count " not divisible by " group-size)]
        (> seed-count group-count) [true (str "more seeds (" seed-count ") than groups (" group-count ")")]
        :else [false (split players seeds)]))))

(defn groups-of-n [group-size indices]
  (groups-of-n-validate
   group-size
   (groups-of-n-split
    group-size
    (partial group-of-n-games indices))))

(defn intersperse-seeds [players seeds]
  (let
   [[players seeds] (split-player-by-type players seeds)]
    (loop [players players
           seeds seeds
           result []]
      (if (empty? seeds)
        (into result players)
        (recur (drop 4 players) (rest seeds) (into result (into [(first seeds)] (take 4 players))))))))

(defn groupless-loop-split [players seeds]
  (let
   [interspersed (intersperse-seeds players seeds)
    player-count (count interspersed)
    nth-player (fn [n] (nth interspersed (mod n player-count)))
    group (to-group "Players" interspersed)
    games (map (fn [index]
                 (cons (str (inc index))
                       (map (comp nth-player (partial - index))
                            [0 1 3 7])))
               (range player-count))]
    [[group] games]))

(defn groupless-loop-validate [players seeds]
  (let
   [player-count (count players)]
    (cond
      (< player-count 15) [true "player count less than 15"]
      (< player-count (* 5 (count seeds))) [true "more than 1 seed per 5 players"]
      :else [false (groupless-loop-split players seeds)])))

(def groups-of-7 "Groups of 7 (4 games)")
(def groups-of-13 "Groups of 13 (4 games)")
(def groups-of-16 "Groups of 16 (5 games)")
(def groupless-loop "Groupless - no repeat matchups (4 games)")

(def method-dict
  {groups-of-7
   (groups-of-n 7 [[0 1 2 3]
                   [0 1 4 5]
                   [0 2 5 6]
                   [0 3 4 6]
                   [1 2 4 6]
                   [1 3 5 6]
                   [2 3 4 5]])
   groups-of-13
   (groups-of-n 13 [[0 1 2 3]
                    [0 4 5 6]
                    [0 7 8 9]
                    [0 10 11 12]
                    [1 4 7 10]
                    [1 5 8 11]
                    [1 6 9 12]
                    [2 4 8 12]
                    [2 5 9 10]
                    [2 6 7 11]
                    [3 5 7 12]
                    [3 6 8 10]
                    [3 4 9 11]])
   groups-of-16
   (groups-of-n 16 [[0 1 2 3] [4 5 6 7] [8 9 10 11] [12 13 14 15]
                    [0 4 8 12] [1 5 9 13] [2 6 10 14] [3 7 11 15]
                    [0 5 10 15] [1 4 11 14] [2 7 8 13] [3 6 9 12]
                    [0 6 11 13] [1 7 10 12] [2 4 9 15] [3 5 8 14]
                    [0 7 9 14] [1 6 8 15] [2 5 11 12] [3 4 10 13]])
   groupless-loop groupless-loop-validate})

(defn validate-input [players seeds]
  (let
   [duplicate-players (util/duplicates players)
    duplicate-seeds (util/duplicates seeds)
    seeds-not-present (filter (fn [name]
                                (not (some #{name} players)))
                              seeds)]
    (cond
      (seq duplicate-players) [true (str "duplicate players " duplicate-players)]
      (seq seeds-not-present) [true (str "seeds not present in player list " seeds-not-present)]
      (seq duplicate-seeds) [true (str "duplicate seeds " duplicate-seeds)]
      :else [false ""])))

(defn result [method-name players seeds]
  (let
   [players (util/trimmed-lines players)
    seeds (util/trimmed-lines seeds)
    [error? validated] (validate-input players seeds)]
    (if error?
      [error? validated]
      ((get method-dict method-name) players seeds))))
