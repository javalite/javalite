drop table if exists PEOPLE;
create table PEOPLE (
    ID          bigint          not null auto_increment primary key,
    NAME        varchar(64)     not null,
    FIRST_NAME  varchar(32)     not null
);


drop table if exists ADDRESSES;
create table ADDRESSES (
    ID          bigint          not null auto_increment primary key,
    CITY        varchar(128)    not null,
    STREET      varchar(128)    not null,
    PERSON_ID   bigint          not null,

    constraint FK_ADDRESSES_PERSON_ID foreign key (PERSON_ID) references PEOPLE(ID)
);
