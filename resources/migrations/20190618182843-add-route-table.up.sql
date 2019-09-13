CREATE TABLE route
(id SERIAL PRIMARY KEY,
 name VARCHAR(200),
 type VARCHAR(200),
 created_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'),
 updated_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'),
 UNIQUE (name, type));
