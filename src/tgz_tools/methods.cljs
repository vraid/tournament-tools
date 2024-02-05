(ns tgz-tools.methods
  (:require [tgz-tools.util :as util]))

(defn split-player-by-type [players previous-winners]
  [(filter (fn [name]
             (not (some #{name} previous-winners)))
           players)
   previous-winners])

(defn group-of-n-games [indices group-id players]
  (map-indexed (fn [game-id group]
                 (cons
                  (str group-id (inc game-id))
                  (map (partial nth players)
                       group)))
               indices))

(defn groups-of-n-split [group-size to-games]
  (fn [players previous-winners]
    (let
     [[non-winners winners] (split-player-by-type players previous-winners)]
      (loop [group-id 65
             players non-winners
             winners winners
             result []]
        (if (empty? players)
          result
          (let
           [winner? (seq winners)
            player-count (if winner? (dec group-size) group-size)]
            (recur (inc group-id)
                   (drop player-count players)
                   (drop 1 winners)
                   (into result (to-games (char group-id) (into (vec (take 1 winners)) (take player-count players)))))))))))

(defn groups-of-n-validate [group-size split]
  (fn [players previous-winners]
    (let
     [player-count (count players)
      group-count (/ player-count group-size)]
      (cond
        (not (zero? (mod player-count group-size))) [true (str "player count of " player-count " not divisible by " group-size)]
        (> (count previous-winners) group-count) [true (str "more previous winners than groups")]
        :else [false (split players previous-winners)]))))

(defn groups-of-n [group-size indices]
  (groups-of-n-validate
   group-size
   (groups-of-n-split
    group-size
    (partial group-of-n-games indices))))

(defn intersperse-winners [players previous-winners]
  (let
   [[non-winners winners] (split-player-by-type players previous-winners)]
    (loop [players non-winners
           winners winners
           result []]
      (if (empty? winners)
        (into result players)
        (recur (drop 4 players) (rest winners) (into result (into [(first winners)] (take 4 players))))))))

(defn groupless-loop-split [players previous-winners]
  (let
   [interspersed (intersperse-winners players previous-winners)
    player-count (count interspersed)
    nth-player (fn [n] (nth interspersed (mod n player-count)))]
    (map (fn [index]
           (cons (inc index)
                 (map (comp nth-player (partial + index))
                      [0 1 3 7])))
         (range player-count))))

(defn groupless-loop-validate [players previous-winners]
  (let
   [player-count (count players)]
    (cond
      (< player-count 15) [true "player count less than 15"]
      (< player-count (* 5 (count previous-winners))) [true "more than 1 previous winner per 5 players"]
      :else [false (groupless-loop-split players previous-winners)])))

(def groups-of-7 "Groups of 7 (4 games)")
(def groups-of-13 "Groups of 13 (4 games)")
(def groups-of-16 "Groups of 16 (5 games)")
(def groupless-loop "Groupless - loop (4 games)")

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

(defn validate-input [players previous-winners]
  (let
   [duplicate-players (util/duplicates players)
    duplicate-winners (util/duplicates previous-winners)
    winners-not-present (filter (fn [name]
                                  (not (some #{name} players)))
                                previous-winners)]
    (cond
      (seq duplicate-players) [true (str "duplicate players " duplicate-players)]
      (seq winners-not-present) [true (str "winners not present in player list " winners-not-present)]
      (seq duplicate-winners) [true (str "duplicate winners " duplicate-winners)]
      :else [false ""])))

(defn result [method-name players previous-winners]
  (let
   [players (util/trimmed-lines players)
    previous-winners (util/trimmed-lines previous-winners)
    [error? validated] (validate-input players previous-winners)]
    (if error?
      [error? validated]
      ((get method-dict method-name) players previous-winners))))
