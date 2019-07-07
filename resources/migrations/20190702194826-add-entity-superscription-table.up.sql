CREATE TABLE entity_superscription
(
 superscription_id INTEGER NOT NULL REFERENCES superscription (id),
 entity_id INTEGER NOT NULL REFERENCES entity (id),
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (superscription_id, entity_id));
