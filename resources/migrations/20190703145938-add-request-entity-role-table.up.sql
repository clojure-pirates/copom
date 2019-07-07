CREATE TABLE request_entity_role
(id INTEGER PRIMARY KEY,
 request_entity_id INTEGER NOT NULL REFERENCES request_entity (id),
 role_id INTEGER NOT NULL REFERENCES request_role (id),
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 UNIQUE (request_entity_id, role_id));
