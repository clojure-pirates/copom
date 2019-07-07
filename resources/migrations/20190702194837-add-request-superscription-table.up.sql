CREATE TABLE request_superscription
(
 superscription_id INTEGER NOT NULL REFERENCES superscription (id),
 request_id INTEGER NOT NULL REFERENCES request (id),
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (superscription_id, request_id));
