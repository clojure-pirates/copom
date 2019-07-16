CREATE TABLE request_entity_superscription
(
 superscription_id INTEGER NOT NULL REFERENCES superscription (id),
 request_entity_id INTEGER NOT NULL REFERENCES request_entity (id),
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (superscription_id, request_entity_id));
