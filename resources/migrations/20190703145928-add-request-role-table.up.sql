CREATE TABLE request_role
(id SERIAL PRIMARY KEY,
 role VARCHAR (200) NOT NULL UNIQUE,
 created_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'),
 updated_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc')
 );