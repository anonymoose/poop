/*
createdb -U postgres poop
psql -U postgres -c "create user poop with password 'poop';"
psql -U postgres -c "alter database poop owner to poop;"
psql -U postgres -d poop -c 'create extension "hstore";'
psql -U postgres -d poop -c 'create extension "fuzzystrmatch";'

psql -U poop -d poop -f doc/schema.sql
*/


drop table if exists ring_session cascade;
create table ring_session (
       id varchar(25) not null primary key,
       session_data varchar(1024),
       session_timestamp timestamp not null
);

/*
   Any user of the system.
   parent_id - If null, this is the boss.  If not null, then points to the boss.
   customer_id - always points to the paying customer account.  The billing email is on crm_customer.
*/
drop table if exists users cascade;
create table users (
       id varchar(25) not null primary key,
       fname varchar(50) not null,
       lname varchar(50) not null,
       email varchar(50) not null,
       phone varchar(20),
       password varchar(64),
       owner varchar(25),
       user_type varchar(10) not null default 'USER', -- 'USER' or 'BROKER'
       consent_email_notifications boolean default false,
       create_dt timestamp without time zone default now(),
       delete_dt timestamp without time zone
);
alter table users add constraint user_email unique (email);


drop table if exists user_mail cascade;
create table user_mail (
       id varchar(25) not null primary key,
       users_id varchar(25) not null,
       message text not null,
       response varchar(200),
       create_dt timestamp without time zone default now(),
       send_dt timestamp without time zone
);
alter table user_mail add foreign key (users_id) references users;

drop table if exists user_kid;
create table user_kid (
       id varchar(25) not null primary key,
       users_id varchar(25) not null,
       name varchar(100) not null,
       create_dt timestamp without time zone default now(),
       delete_dt timestamp without time zone
);

drop table if exists user_kid_log;
create table user_kid_log (
       id varchar(25) not null primary key,
       users_id varchar(25) not null,
       kid_id varchar(25) not null,
       log_type varchar(15) not null,
       note text,
       datapoint varchar(20),
       datapoint2 varchar(20),
       create_dt timestamp without time zone default now(),
       delete_dt timestamp without time zone
);
