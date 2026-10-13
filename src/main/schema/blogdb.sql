CREATE DATABASE blogdb;
USE blogdb;
SELECT id, name, email, password
FROM users;
SELECT id, name, email
FROM users
WHERE LOWER(TRIM(email)) = LOWER('testuser2@example.com');