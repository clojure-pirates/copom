CREATE TABLE neighborhood
(id SERIAL PRIMARY KEY,
 name VARCHAR(200) UNIQUE,
 created_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'),
 updated_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'));
