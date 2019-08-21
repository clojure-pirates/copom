CREATE TABLE request_entity_superscription
(id INTEGER PRIMARY KEY,
 request_entity_id INTEGER NOT NULL REFERENCES request_entity (id) 
	ON DELETE CASCADE,
 superscription_id INTEGER NOT NULL REFERENCES superscription (id) 
	ON DELETE CASCADE,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 UNIQUE (request_entity_id, superscription_id));
