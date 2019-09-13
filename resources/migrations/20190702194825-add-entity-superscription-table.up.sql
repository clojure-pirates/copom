CREATE TABLE entity_superscription
(id SERIAL PRIMARY KEY,
 entity_id INTEGER NOT NULL REFERENCES entity (id) ON DELETE CASCADE,
 superscription_id INTEGER NOT NULL REFERENCES superscription (id) 
  				   ON DELETE CASCADE,
 created_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'),
 updated_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'),
 UNIQUE (entity_id, superscription_id));
