(ns tgz-tools.views
  (:require
   [re-frame.core :as re-frame]
   [tgz-tools.subs :as subs]
   [tgz-tools.events :as events]
   [tgz-tools.util :as util]
   [tgz-tools.methods :as methods]
   [clojure.string :as string]))

(defn gettext [e] (-> e .-target .-value))

(defn format-game [game]
  (string/join ", " game))

(defn main-panel []
  (let [data @(re-frame/subscribe [::subs/db])
        update-value (fn [key]
                       (fn [e] (re-frame/dispatch
                                [::events/set-value key (gettext e)])))
        [error? validated] (methods/result
                            (:method data)
                            (:players data)
                            (:previous-winners data))
        games (if error? [] (map format-game validated))
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
     (linked-text-area :previous-winners)
     (if error?
       [:div [:b (str "Error: " validated)]]
       [:div
        [:div [:label "Result"]]
        [:textarea
         {:cols 60
          :rows (inc (count games))
          :read-only true
          :value (string/join "\n" games)}]])]))
