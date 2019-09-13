CREATE TABLE request
(id SERIAL PRIMARY KEY,
 complaint VARCHAR (200),
 summary TEXT,
 event_timestamp TIMESTAMP,
 status VARCHAR (20),
 measures TEXT,
 created_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'),
 updated_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'));
