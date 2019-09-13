CREATE TABLE delict
(id SERIAL PRIMARY KEY,
 name VARCHAR (200) UNIQUE,
 weight INTEGER,
 created_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'),
 updated_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'));