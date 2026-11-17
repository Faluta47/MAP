-- Test data equivalent to data/utilizatori.txt and data/prietenii.txt:
-- 10 users, 3 communities (chain 1-2-3-4-5, dense group 6-7-8-9, isolated 10).
-- Run after schema.sql, on an empty users table. The ids are generated (1..10, in insertion order).

INSERT INTO users (first_name, last_name) VALUES ('Ion', 'Popescu');
INSERT INTO users (first_name, last_name) VALUES ('Maria', 'Ionescu');
INSERT INTO users (first_name, last_name) VALUES ('Mihai', 'Georgescu');
INSERT INTO users (first_name, last_name) VALUES ('Elena', 'Dumitru');
INSERT INTO users (first_name, last_name) VALUES ('Andrei', 'Stan');
INSERT INTO users (first_name, last_name) VALUES ('Ana', 'Marin');
INSERT INTO users (first_name, last_name) VALUES ('Vlad', 'Constantin');
INSERT INTO users (first_name, last_name) VALUES ('Irina', 'Radu');
INSERT INTO users (first_name, last_name) VALUES ('Paul', 'Dinu');
INSERT INTO users (first_name, last_name) VALUES ('Diana', 'Barbu');

INSERT INTO friendships (id1, id2, friends_from) VALUES (1, 2, '2026-09-01 10:00:00');
INSERT INTO friendships (id1, id2, friends_from) VALUES (2, 3, '2026-09-02 11:30:00');
INSERT INTO friendships (id1, id2, friends_from) VALUES (3, 4, '2026-09-03 12:15:00');
INSERT INTO friendships (id1, id2, friends_from) VALUES (4, 5, '2026-09-04 09:45:00');
INSERT INTO friendships (id1, id2, friends_from) VALUES (6, 7, '2026-09-05 14:00:00');
INSERT INTO friendships (id1, id2, friends_from) VALUES (6, 8, '2026-09-06 15:20:00');
INSERT INTO friendships (id1, id2, friends_from) VALUES (6, 9, '2026-09-07 16:40:00');
INSERT INTO friendships (id1, id2, friends_from) VALUES (7, 8, '2026-09-08 08:10:00');
INSERT INTO friendships (id1, id2, friends_from) VALUES (7, 9, '2026-09-09 18:25:00');
INSERT INTO friendships (id1, id2, friends_from) VALUES (8, 9, '2026-09-10 20:05:00');
