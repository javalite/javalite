create table users (
    username varchar not null,
    password varchar not null
);

alter table users
    add index (username),
    add unique (username);