(ns poop.controller.common
  (:require
   [clojure.tools.logging :as log]
   [poop.lib.web :as web]
   [poop.lib.util :as util]
   [poop.model.user :as user]
   [sandbar.stateful-session :as session]
   ))

(defn now-local-str
  ([tz min-ago]
     (util/dt-format-to-str (util/dt-local-min-ago tz min-ago) "h:mm a MM/dd YYYY" tz))
  ([tz]
     (util/dt-format-to-str (util/dt-now-local tz) "h:mm a MM/dd YYYY" tz)))

(defn local-dt-str-to-gmt-timestamp [local-dt-str tz]
  (let [gmt-dt (util/dt-to-utc (util/dt-parse local-dt-str "h:mm a MM/dd YYYY" tz))]
    (util/dt-to-sql-timestamp gmt-dt)))

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
     :timezone-list util/TIMEZONE-LIST
     :uid uid
     :user usr
     :kids kids
     :now-local (now-local-str (:tz usr))
     :ago15 (now-local-str (:tz usr) 15)
     :ago30 (now-local-str (:tz usr) 30)
     :ago45 (now-local-str (:tz usr) 45)
     :ago60 (now-local-str (:tz usr) 60)
     }))
