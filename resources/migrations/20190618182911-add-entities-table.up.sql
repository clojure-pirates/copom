CREATE TABLE entities (
 id INTEGER AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR (200),
 doc_type VARCHAR (30),
 doc_issuer VARCHAR (30),
 doc_number VARCHAR (30),
 father VARCHAR (200),
 mother VARCHAR (200),
 address_id INTEGER REFERENCES superscriptions (id),
 phone VARCHAR (30),
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);