(ns tgz-tools.db
  (:require [tgz-tools.methods :as methods]))

(def default-db
  {:title ""
   :players ""
   :previous-winners ""
   :method methods/groups-of-7})
