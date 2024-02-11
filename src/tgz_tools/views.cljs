(ns tgz-tools.views
  (:require
   [re-frame.core :as re-frame]
   [tgz-tools.subs :as subs]
   [tgz-tools.events :as events]
   [tgz-tools.util :as util]
   [tgz-tools.methods :as methods]
   [tgz-tools.tables :as tables]
   [clojure.string :as string]))

(defn gettext [e] (-> e .-target .-value))

(defn format-game [game]
  (string/join ", " game))

(defn main-panel []
  (let [data @(re-frame/subscribe [::subs/db])
        title (:title data)
        update-value (fn [key]
                       (fn [e] (re-frame/dispatch
                                [::events/set-value key (gettext e)])))
        [error? validated] (methods/result
                            (:method data)
                            (:players data)
                            (:seeds data))
        [groups games] (if error? [[] []] validated)
        [game-csv game-table group-table]
        (if (or error? (empty? games)) [[] "" ""]
            (let
             [game-csv (map (comp (partial str title) format-game) games)
              seeds (string/split (:seeds data) "\n")
              tag-str (comp (partial tables/join-with-tag "") tables/tag)
              game-table (tables/bgg-table (fn [_ col str]
                                             (if (zero? col)
                                               ((tag-str "[b]" "[/b]") str)
                                               str))
                                           games)
              group-table (tables/bgg-table (fn [row _ str]
                                              ((cond (zero? row) (tag-str "[b]" "[/b]")
                                                     (some #{str} seeds) (tag-str "[u]" "[/u]")
                                                     :else identity)
                                               str))
                                            (cons (map :name groups) (tables/transpose (map :players groups))))]
              [game-csv game-table group-table]))
        textarea-width (inc (reduce max 20 (map count game-csv)))
        option (fn [name] [:option {:key name} name])
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
     [:div
      [:label "Method "]
      [:select.form-control
       {:style {:padding "8px 8px"
                :margin "4px 4px"}
        :field :list
        :id :projection-input
        :value (:method data)
        :on-change (update-value :method)}
       (option methods/groups-of-7)
       (option methods/groups-of-13)
       (option methods/groups-of-16)
       (option methods/groupless-loop)]]
     [:div [:label "Tournament title"]]
     [:input {:type "text"
              :value (:title data)
              :on-change (update-value :title)}]
     [:div [:label "Players"]]
     (linked-text-area :players)
     [:div [:label "Seeds"]]
     (linked-text-area :seeds)
     (if error?
       [:div [:b (str "Error: " validated)]]
       [:div
        [:div [:label "Games (CSV)"]]
        [:textarea
         {:cols textarea-width
          :rows (inc (count game-csv))
          :read-only true
          :value (string/join "\n" game-csv)}]
        [:div [:label "Groups (BGG table)"]]
        [:textarea
         {:cols textarea-width
          :rows 10
          :read-only true
          :value group-table}]
        [:div [:label "Games (BGG table)"]]
        [:textarea
         {:cols textarea-width
          :rows 10
          :read-only true
          :value game-table}]])]))
