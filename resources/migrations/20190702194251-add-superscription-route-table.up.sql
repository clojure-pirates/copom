CREATE TABLE superscription_route
(
 superscription_id INTEGER NOT NULL REFERENCES superscription (id) ON DELETE CASCADE,
 route_id INTEGER NOT NULL REFERENCES route (id) ON DELETE CASCADE,
 created_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'),
 updated_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'),
 PRIMARY KEY (superscription_id, route_id),
 UNIQUE (superscription_id, route_id));
