(ns tgz-tools.util
  (:require [clojure.string :as string]))

(def lines string/split-lines)

(def line-count (comp count lines))
