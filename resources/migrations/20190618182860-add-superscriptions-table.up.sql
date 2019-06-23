CREATE TABLE superscriptions (
 id INTEGER AUTO_INCREMENT PRIMARY KEY,
 neighborhood_id INTEGER REFERENCES neighborhood (id),
 route_id INTEGER REFERENCES route (id),
 num VARCHAR (30),
 complement VARCHAR (200),
 reference VARCHAR (200),
 city VARCHAR (200),
 state VARCHAR (200),
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);