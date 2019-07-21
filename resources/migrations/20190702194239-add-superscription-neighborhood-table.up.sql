CREATE TABLE superscription_neighborhood
(
 superscription_id INTEGER NOT NULL REFERENCES superscription (id) ON DELETE CASCADE,
 neighborhood_id INTEGER NOT NULL REFERENCES neighborhood (id) ON DELETE CASCADE,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (superscription_id, neighborhood_id),
 UNIQUE (superscription_id, neighborhood_id));
