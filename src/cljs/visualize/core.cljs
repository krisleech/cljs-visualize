(ns visualize.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [cljsjs.react :as react])
    (:import goog.History))

(defonce world (atom { :todos [] }))

;; -------------------------
;; Views

(def done-to-color { true "green"
                     false nil })

(defn todo []
  (let [state (reagent/atom { :done false :name "Buy milk" })]
    (fn [] 
      [:div.todo
      [:input.done { :type "checkbox" :done (:done @state) :on-click #(swap! state assoc :done (not (:done @state)))}]
      [:input.name { :type "text" :style { :border "none" :color (done-to-color (:done @state)) } :value (:name @state) :on-change #(swap! state assoc :name (-> % .-target .-value)) }]
      [:br]
      [:input.state-editor { :type "text" :size 55 :value (str @state) :on-change #(reset! state (cljs.reader/read-string (-> % .-target .-value))) }]])))

(defn home-page []
  [:div [:h2 "Welcome to visualize"]
   [todo]
   [:p "Try changing the data or using the compontent"]
   [:div [:a {:href "#/about"} "go to about page"]]])


(defn about-page []
  [:div [:h2 "About visualize"]
   [:div [:a {:href "#/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
