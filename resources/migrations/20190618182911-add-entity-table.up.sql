CREATE TABLE entity (
 id SERIAL PRIMARY KEY,
 name VARCHAR (200),
 doc_type VARCHAR (30),
 doc_issuer VARCHAR (30),
 doc_number VARCHAR (30),
 father VARCHAR (200),
 mother VARCHAR (200),
 phone VARCHAR (30),
 created_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'),
 updated_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'));