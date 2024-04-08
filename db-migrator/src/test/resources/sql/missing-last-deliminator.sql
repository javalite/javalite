create table users (
    username varchar not null,
    password varchar not null
);

create table roles (
    name varchar not null unique,
    description text not null
)
