CREATE TABLE requests
(id INTEGER AUTO_INCREMENT PRIMARY KEY,
 address_id INTEGER REFERENCES superscriptions (id),
 complaint VARCHAR (200),
 summary TEXT,
 event_timestamp TIMESTAMP,
 status VARCHAR (20),
 measures TEXT,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
