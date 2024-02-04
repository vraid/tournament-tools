(ns tgz-tools.events
  (:require
   [re-frame.core :as re-frame]
   [tgz-tools.db :as db]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::set-value
 (fn [db [_ key value]]
   (assoc db key value)))
