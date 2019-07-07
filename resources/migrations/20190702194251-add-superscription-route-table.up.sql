CREATE TABLE superscription_route
(
 superscription_id INTEGER NOT NULL REFERENCES superscription (id),
 route_id INTEGER NOT NULL REFERENCES route (id),
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (superscription_id, route_id));
