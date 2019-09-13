CREATE TABLE request_delict
(
 request_id INTEGER NOT NULL REFERENCES request (id) ON DELETE CASCADE,
 delict_id INTEGER NOT NULL REFERENCES delict (id) ON DELETE CASCADE,
 created_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'),
 updated_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'),
 PRIMARY KEY (request_id, delict_id),
 UNIQUE (request_id, delict_id));
