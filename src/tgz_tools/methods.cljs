(ns tgz-tools.methods
  (:require [tgz-tools.util :as util]))

(defn split-player-by-type [players previous-winners]
  [(filter (fn [name]
             (not (some #{name} previous-winners)))
           players)
   previous-winners])

(defn group-of-7-games [group-id players]
  (let
   [indices [[0 1 2 3]
             [0 1 4 5]
             [0 2 5 6]
             [0 3 4 6]
             [1 2 4 6]
             [1 3 5 6]
             [2 3 4 5]]]
    (map-indexed (fn [game-id group]
                   (cons
                    (str group-id (inc game-id))
                    (map (partial nth players)
                         group)))
                 indices)))

(defn groups-of-7-split [players previous-winners]
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
          player-count (if winner? 6 7)]
          (recur (inc group-id)
                 (drop player-count players)
                 (drop 1 winners)
                 (into result (group-of-7-games (char group-id) (into (vec (take 1 winners)) (take player-count players))))))))))

(defn groups-of-7-validate [split]
  (fn [players previous-winners]
    (let
     [player-count (count players)
      group-count (/ player-count 7)]
      (cond
        (not (zero? (mod player-count 7))) [true (str "player count of " player-count " not divisible by 7")]
        (> (count previous-winners) group-count) [true (str "more previous winners than groups")]
        :else [false (split players previous-winners)]))))

(def groups-of-7 "Groups of 7")

(def method-dict
  {groups-of-7 (groups-of-7-validate groups-of-7-split)})

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
