CREATE TABLE request_entity_role
(id SERIAL PRIMARY KEY,
 request_entity_id INTEGER NOT NULL REFERENCES request_entity (id) ON DELETE CASCADE,
 role_id INTEGER NOT NULL REFERENCES request_role (id) ON DELETE CASCADE,
 created_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'),
 updated_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'),
 UNIQUE (request_entity_id, role_id));
