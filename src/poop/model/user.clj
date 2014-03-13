(ns poop.model.user
  (:use
   [korma.db]
   [korma.core]
   [clojure.tools.logging :only [info error]]
   [poop.lib.mail :as mail]
   [clj-json.core :as json]
   )
  (:require
   [clojure.tools.logging :as log]
   [poop.lib.db :as db]
   [poop.lib.util :as util]
   [clojure.string :as str]
   [digest]
   ))


;;
;; user_mail entity
;;
(defentity user_mail)

(defn find-mail-by-user
  "everything sent to this user, ordered by date.  newest first"
  [user_id]
  (select user_mail
          (where {:users_id user_id})
          (order :create_dt :DESC)))


(defn send-mail [user from subject msg]
    (insert user_mail
            (values {:id (util/uuid)
                     :users_id (:id user)
                     :message (json/generate-string {:to (:email user)
                                                     :from from
                                                     :subject subject
                                                     :text msg})})))

(defn- send-one-queued-mail [um]
  (let [umid (:id um)
        args (json/parse-string (:message um))
        response (mail/sendmail args)]
    (if (= true response)
                                        ; Success. Update time sent.
      (update user_mail
              (set-fields (:send_dt (util/ts-now)))
              (where {:id umid}))
                                        ; failed.  Update with the reason why.
      (update user_mail
              (set-fields (:response (json/generate-string response)))
              (where {:id umid})))))


(defn send-queued-mail []
  (doseq [um (select user_mail
                     (where {:send_dt nil
                             :response nil}))]
    (send-one-queued-mail um)))


(defn delete-all-mails [users_id]
    (delete user_mail
            (where {:users_id users_id})))


;;
;; user_kid_log entity
;;
(defentity user_kid_log)

(defn log-activity
  ([users_id kid_id params]
     (let [id (util/uuid)]
       (insert user_kid_log
               (values (merge params
                              {:id id
                               :users_id users_id
                               :kid_id kid_id
                               }))))))

(defn get-log-activity-by-user
  ([users_id cnt]
     (let [query (str "select ukl.*, uk.name
                      from user_kid_log ukl, user_kid uk
                      where ukl.kid_id = uk.id and
                      ukl.users_id = '" users_id "'
                      order by ukl.create_dt desc
                      limit " cnt)]
      (println query)
      (exec-raw [query] :results))))


;;
;; user_kid entity
;;
(defentity user_kid)

(defn load-one-kid [id]
  (first (select user_kid
                 (where {:id id
                         :delete_dt nil}))))

(defn find-kids-by-user [usr]
  (select user_kid
          (where {:users_id (:id usr)
                  :delete_dt nil})))

(defn add-kid [usr name]
  (let [id (util/uuid)]
    (insert user_kid
            (values {:id id
                     :users_id (:id usr)
                     :name name}))))

(defn remove-kid [users_id kid_id]
  (update user_kid
          (set-fields {:delete_dt (util/ts-now)})
          (where {:id kid_id
                  :users_id users_id})))


;;
;; User entity
;;
(defn encrypt-password [pw]
  (digest/sha-256 pw))

(defentity user
  (table :users)
  (prepare (fn [props]
             (let [email (:email props)
                   password (:password props)
                   props (if email
                           (assoc props :email (str/lower-case email)) props)
                   props (if password
                           (assoc props :password (encrypt-password password)) props)
                   ]
               props
               ))))

(defn find-by-email [email]
  (first
   (select user
           (where {:email (str/lower-case email)
                   :delete_dt nil}))))

(defn load-one [id]
  (first (select user
                 (where {:id id
                         :delete_dt nil}))))

(defn full-delete [id]
  (delete-all-mails id)
  (delete user (where {:id id})))

(defn save
  ([id params]
     (update user
             (set-fields (merge {:id id} params))
             (where {:id id})))
  ([params]
     (let [id (util/uuid)]
       (insert user
               (values (merge params {:id id}))))))

(defn authenticate [usr password]
  (and (not (nil? usr))
       (= (encrypt-password password) (usr :password))))

(defn save-password [id password]
  (save id {:password password}))
