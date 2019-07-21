CREATE TABLE request_entity_role
(id INTEGER PRIMARY KEY,
 request_entity_id INTEGER NOT NULL REFERENCES request_entity (id) ON DELETE CASCADE,
 role_id INTEGER NOT NULL REFERENCES request_role (id) ON DELETE CASCADE,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 UNIQUE (request_entity_id, role_id));
