CREATE TABLE superscription (
 id SERIAL PRIMARY KEY,
 num VARCHAR (30),
 complement VARCHAR (200),
 reference VARCHAR (200),
 city VARCHAR (200),
 state VARCHAR (200),
 created_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc'),
 updated_at TIMESTAMP DEFAULT (now() AT TIME ZONE 'utc')
);