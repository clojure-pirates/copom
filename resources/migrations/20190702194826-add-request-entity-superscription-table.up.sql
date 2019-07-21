CREATE TABLE request_entity_superscription
(
 superscription_id INTEGER NOT NULL REFERENCES superscription (id) ON DELETE CASCADE,
 request_entity_id INTEGER NOT NULL REFERENCES request_entity (id) ON DELETE CASCADE,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (superscription_id, request_entity_id),
 UNIQUE (superscription_id, request_entity_id));
