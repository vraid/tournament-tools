(ns tgz-tools.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::db
 (fn [db]
   db))
