CREATE TABLE superscription_neighborhood
(
 superscription_id INTEGER NOT NULL REFERENCES superscription (id),
 neighborhood_id INTEGER NOT NULL REFERENCES neighborhood (id),
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (superscription_id, neighborhood_id));
