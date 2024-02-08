(ns tgz-tools.tables
  (:require [clojure.string :as string]))

(defn tag [a b]
  (fn [ls]
    (concat [a] ls [b])))

(defn join-with-floatleft [s]
  (comp (partial string/join s) (tag "[floatleft]" "[/floatleft]")))

(def join-rows (join-with-floatleft "\n"))
(def join-columns (join-with-floatleft ""))

(defn bgg-table [rows]
  (join-columns
   (map (comp join-rows
              (fn [column]
                (map (fn [row] (nth row column))
                     rows)))
        (range (count (first rows))))))
