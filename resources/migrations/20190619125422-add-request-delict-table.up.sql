CREATE TABLE request_delict
(
 request_id INTEGER NOT NULL REFERENCES request (id) ON DELETE CASCADE,
 delict_id INTEGER NOT NULL REFERENCES delict (id) ON DELETE CASCADE,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (request_id, delict_id),
 UNIQUE (request_id, delict_id));
