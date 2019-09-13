CREATE TABLE appuser
(id SERIAL PRIMARY KEY,
 first_name VARCHAR(30),
 last_name VARCHAR(30),
 email VARCHAR(30) UNIQUE,
 admin BOOLEAN,
 last_login TIMESTAMP,
 is_active BOOLEAN,
 pass VARCHAR(300),
 created_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'),
 updated_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'));
