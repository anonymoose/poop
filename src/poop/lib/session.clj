(ns poop.lib.session
  (:require
   [ring.middleware.session.store :as session]
   [poop.lib.util :as util])
  (:use
   [korma.core]))


(defentity ring_session)

;; create table ring_session (
;;        id varchar(25) not null primary key,
;;        session_data varchar(1024),
;;        session_timestamp timestamp not null
;; );


;;
;; Extend ring.middleware.session.store to do our bidding.
;;
(deftype DBStore []

  session/SessionStore

  (read-session [_ session-key]
    (let [sess (first (select ring_session
                              (fields :session_data)
                              (where {:id session-key})))]
      (if (not (nil? sess))
        (do
          (read-string (sess :session_data))
          ))))

  (write-session [_ session-key data]
    (let [vals {:session_data (pr-str data)
                :session_timestamp (util/ts-now)}]
      (if (empty? session-key)
        (let [generated-key (util/uuid)]
          (insert ring_session (values (merge {:id generated-key} vals)))
          generated-key
          )
        (do
          (update ring_session
                  (set-fields vals)
                  (where {:id session-key}))
          session-key
          )))) 
  
  (delete-session [_ session-key]
    (delete ring_session (where {:id session-key}))
    nil))


(defn db-session-store
  []
  (DBStore.))