CREATE TABLE request_entity
(id SERIAL PRIMARY KEY,
 request_id INTEGER NOT NULL REFERENCES request (id) ON DELETE CASCADE,
 entity_id INTEGER NOT NULL REFERENCES entity (id) ON DELETE CASCADE,
 created_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'),
 updated_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'),
 UNIQUE (request_id, entity_id));
