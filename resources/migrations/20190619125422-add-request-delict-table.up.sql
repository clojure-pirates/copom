CREATE TABLE request_delict
(
 request_id INTEGER NOT NULL REFERENCES request (id),
 delict_id INTEGER NOT NULL REFERENCES delict (id),
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (request_id, delict_id));
