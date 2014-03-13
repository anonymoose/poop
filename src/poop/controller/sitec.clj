(ns poop.controller.sitec
  (:require
   [poop.lib.template :as tpl]
   [clojure.tools.logging :as log]
   [clojure.string :as str]
   [ring.util.response :as ring]
   [poop.lib.util :as util]
   [poop.lib.web :as web]
   [korma.db :as kdb]
   [poop.model.user :as user]
   [poop.controller.common :as common]
   [poop.controller.userc :as userc]
   [sandbar.stateful-session :as session]))


(defn index []
  (if (nil? (common/logged-in-user-id))
    (tpl/page-out "index"
                  (merge
                   (common/user-vars)
                   {:mobile (web/mobile?)}))
    (ring/redirect "/dashboard")))

(defn show-page [pg]
  (try
    (tpl/page-out pg (common/user-vars))
    (catch Exception e
      "")))

(defn access-denied []
  (tpl/page-out "404" {}))
