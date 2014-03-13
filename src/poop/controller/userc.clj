(ns poop.controller.userc
  (:require
   [clojure.tools.logging :as log]
   [sandbar.stateful-session :as session]
   [poop.lib.template :as tpl]
   [clojure.string :as str]
   [poop.model.user :as user]
   [ring.util.response :as ring]
   [korma.db :as kdb]
   [poop.controller.common :as common]
   [poop.lib.util :as util]))


(defn sign-in
  ([deny?] (tpl/page-out "sign-in" {:deny? deny?}))
  ([email password]
     (let [usr (user/find-by-email email)]
       (if (and (not (nil? usr))
                (user/authenticate usr password))
         (do
           (kdb/transaction
            (session/session-put! :user-id (usr :id)))
           (ring/redirect "/dashboard"))
         (ring/redirect (str "/sign-in/deny"))))))


(defn send-welcome-email [usr]
  (user/send-mail usr
                  "support@poop.com"
                  "Welcome to P00P!"
                  (tpl/render-tpl "email/welcome" {:user usr})))

(defn send-forgot-password-email [usr]
  (user/send-mail usr
                  "support@poop.com"
                  "Pickr.io Password Reset"
                  (tpl/render-tpl "email/forgot-password" {:user usr})))


(defn forgot-password
  ([]
     (tpl/page-out "forgot-password" {}))
  ([email]
     (let [usr (user/find-by-email email)]
       (if (not (nil? usr))
         (kdb/transaction
          (user/log-activity usr "FORGOT-PW")
          (send-forgot-password-email usr)))
       (tpl/page-out "forgot-password" {:user usr}))))


(defn account-param-scrub [params]
  (let [params (if (empty? (params "password")) ; if password is empty, don't pass it in.
                 (dissoc params "password")
                 params)]
    ; check boxes come in as "on".  turn those to booleans
    (assoc params
      "consent_email_notifications" (= "on" (params "consent_email_notifications")))))

(defn sign-up
  ([] (tpl/page-out "sign-up" {}))
  ([params]
     (kdb/transaction
      (let [usr (user/save (account-param-scrub params))]
        (session/session-put! :user-id (:id usr))
        (send-welcome-email usr)
        (ring/redirect (str "/dashboard"))))))

(defn sign-out
  ([]
     (session/destroy-session!)
     (ring/redirect "/")))

(defn dashboard
  [qparams]
  (let [usr (user/load-one (common/logged-in-user-id))
        qparams (util/keyword-keyify qparams)]
      (tpl/page-in "dashboard"
               (merge (common/summary-vars)
                      (common/user-vars)
                      {:events (user/get-log-activity-by-user (:id usr) 20)}))))

(defn account
  ([deny?]
     (let [logged-in-usr (user/load-one (common/logged-in-user-id))]
       (tpl/page-in "account"
                    (merge (common/user-vars)
                           {:user logged-in-usr
                            :deny? deny?}))))
  ([id params]
     (kdb/transaction
      (let [usr (user/save id (account-param-scrub params))]
        (ring/redirect "/account/saved")))))


(defn account-saved []
  (let [logged-in-usr (user/load-one (common/logged-in-user-id))]
    (tpl/page-in "account"
                 (merge (common/user-vars)
                        {:user logged-in-usr
                         :saved? true}))))

(defn change-password
  [params]
  (let [password (params "password")
        confirm (params "confirm")
        usr (user/load-one (common/logged-in-user-id))]
    (if (= password confirm)
      (kdb/transaction
       (user/save-password (:id usr) password)
       (ring/redirect "/account/saved"))
      (ring/redirect "/account/deny"))))

(defn add-kid [name]
  (let [usr (user/load-one (common/logged-in-user-id))]
    (kdb/transaction
     (user/add-kid usr name)
     (ring/redirect "/account/saved"))))

(defn remove-kid [kid_id]
  (let [usr (user/load-one (common/logged-in-user-id))]
    (kdb/transaction
     (user/remove-kid (:id usr) kid_id)
     (ring/redirect "/account/saved"))))

(defn event-save [users_id kid_id params]
  (let [usr (user/load-one (common/logged-in-user-id))]
    (kdb/transaction
     (user/log-activity (:id usr) kid_id
                        (dissoc params "kid_id"))
     (ring/redirect "/dashboard"))))


(defn event-report []
  (let [logged-in-usr (user/load-one (common/logged-in-user-id))]
    (tpl/page-in "event-report"
                 (merge (common/user-vars)
                        {:events (user/get-log-activity-by-user (:id logged-in-usr) 300)}))))
