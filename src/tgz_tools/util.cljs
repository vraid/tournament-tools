(ns tgz-tools.util
  (:require [clojure.string :as string]))

(def lines string/split-lines)

(defn trimmed-lines [str]
  (filter seq
          (map string/trim (lines str))))

(def line-count (comp count lines))

(defn duplicates [lines]
  (let
   [lowcase (map string/lower-case lines)]
    (for [[name freq] (frequencies lowcase)
          :when (> freq 1)]
      name)))
