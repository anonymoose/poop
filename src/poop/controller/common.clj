(ns poop.controller.common
  (:require
   [clojure.tools.logging :as log]
   [poop.lib.web :as web]
   [poop.lib.util :as util]
   [poop.model.user :as user]
   [sandbar.stateful-session :as session]
   )
  )

(defn summary-vars []
  {:production (web/production?)
   :mobile (web/mobile?)
   })

(defn logged-in-user-id []
  (session/session-get :user-id))

(defn logged-in? []
  (util/not-nil? (logged-in-user-id)))

(defn user-vars []
  (let [uid (logged-in-user-id)
        usr (if (util/not-nil? uid) (user/load-one uid))
        kids (if (util/not-nil? usr) (user/find-kids-by-user usr))]
    {
     :logged-in? (complement (nil? uid))
     :user usr
     :kids kids
     }))
