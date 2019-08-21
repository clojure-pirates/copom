CREATE TABLE entity_superscription
(id INTEGER PRIMARY KEY,
 entity_id INTEGER NOT NULL REFERENCES entity (id) ON DELETE CASCADE,
 superscription_id INTEGER NOT NULL REFERENCES superscription (id) 
  				   ON DELETE CASCADE,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 UNIQUE (entity_id, superscription_id));
