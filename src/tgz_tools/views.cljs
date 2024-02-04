(ns tgz-tools.views
  (:require
   [re-frame.core :as re-frame]
   [tgz-tools.subs :as subs]
   [tgz-tools.events :as events]
   [tgz-tools.util :as util]))

(defn gettext [e] (-> e .-target .-value))

(defn main-panel []
  (let [data @(re-frame/subscribe [::subs/db])
        update-value (fn [key]
                       (fn [e] (re-frame/dispatch
                                [::events/set-value key (gettext e)])))
        linked-text-area (fn [key]
                           (let
                            [value (get data key)]
                             [:textarea
                              {:cols 40
                               :rows (inc (util/line-count value))
                               :on-change (update-value key)
                               :value value}]))]
    [:div
     [:h1
      "TGZ tools"]
     [:div [:label "Players"]]
     (linked-text-area :players)
     [:div [:label "Previous winners"]]
     (linked-text-area :previous-winners)]))
