DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS requests;

CREATE TABLE IF NOT EXISTS categories
(
    id integer generated by default as identity primary key,
    name varchar(50) not null
);
CREATE TABLE IF NOT EXISTS users
(
    id integer generated by default as identity primary key,
    name varchar(250) not null,
    email varchar(254) not null
);
CREATE TABLE IF NOT EXISTS events
(
    id integer generated by default as identity primary key,
    title varchar(120),
    description varchar(7000),
    annotation varchar(2000),
    initiator_id integer references users (id) on delete cascade,
    category_id integer references categories (id) on delete cascade,
    longitude double precision,
    latitude double precision,
    event_date timestamp,
    participant_limit integer,
    created_on timestamp,
    published_time timestamp,
    paid bool,
    request_moderation bool,
    confirmed_requests integer,
    state_condition varchar(20)
);
CREATE TABLE IF NOT EXISTS requests
(
    id integer generated by default as identity primary key,
    requester_id integer references users (id) on delete cascade,
    event_id integer references events (id) on delete cascade,
    created timestamp,
    status integer
);
CREATE TABLE IF NOT EXISTS compilations
(
    id integer generated by default as identity primary key,
    pinned bool,
    title varchar(50)
);
CREATE TABLE IF NOT EXISTS compilations_events
(
    compilation_id integer references compilations (id) on delete cascade,
    event_id integer references events (id) on delete cascade
);