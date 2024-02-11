(ns tgz-tools.tables
  (:require [clojure.string :as string]))

(defn tag [a b]
  (fn [ls]
    (concat [a] ls [b])))

(defn join-with-tag [s tag]
  (comp (partial string/join s) tag))

(def floatleft (tag "[floatleft]" "[/floatleft]"))

(def join-rows (join-with-tag "\n" floatleft))
(def join-columns (join-with-tag "" floatleft))

(defn bgg-table [format rows]
  (join-columns
   (map (comp join-rows
              (fn [column]
                (map (fn [row]
                       (format row column (nth (nth rows row) column)))
                     (range (count rows)))))
        (range (count (first rows))))))

(defn transpose [ls]
  (map (fn [n]
         (map (fn [a] (nth a n))
              ls))
       (range (count (first ls)))))
