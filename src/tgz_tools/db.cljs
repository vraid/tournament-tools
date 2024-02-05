(ns tgz-tools.db
  (:require [tgz-tools.methods :as methods]))

(def default-db
  (let
   [default-method methods/groups-of-7]
    {:players ""
     :previous-winners ""
     :method default-method}))
