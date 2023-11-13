create table posts (
    id serial primary key,
    name varchar,
    text text,
    link varchar unique,
    created timestamp
);