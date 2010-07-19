-- *********************** Table ITEMS ***********************
CREATE TABLE items(
	id NUMBER NOT NULL,
	item_name VARCHAR2(50) NOT NULL,
	created_at TIMESTAMP,
	updated_at TIMESTAMP
)

BREAK

ALTER TABLE items ADD CONSTRAINT item_pk PRIMARY KEY ( id )

BREAK
CREATE SEQUENCE item_seq START WITH 1 INCREMENT BY 1

BREAK
-- *********************** TABLE PAGES ***********************
create table pages (
  id NUMBER(10) not null,
  description VARCHAR(56)
)

BREAK
ALTER TABLE pages ADD CONSTRAINT pages_PK PRIMARY KEY ( id )

BREAK
CREATE SEQUENCE pages_seq START WITH 1 INCREMENT BY 1


BREAK
CREATE OR REPLACE TRIGGER pages_pk_trigger
    BEFORE INSERT ON pages REFERENCING
    NEW AS new
    OLD AS old
    FOR EACH ROW
    begin 
select coalesce(:new.id, pages_seq.nextval) into :new.id from dual;
end;
BREAK