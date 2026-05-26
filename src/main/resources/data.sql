-- Очищаем таблицы перед вставкой, чтобы при повторном запуске не было дублей
TRUNCATE TABLE users, profiles, posts, comments, post_reactions, friendships, chats, messages RESTART IDENTITY CASCADE;

-- 1. Пользователи
INSERT INTO users (id, username, email, password_hash, role, status) VALUES
(1, 'admin_neo', 'neo@matrix.com', '$2a$12$J.EMR9WFk/jiOE3dDV4t7uklAeh41ecqVha0XAbxzkJo4RbpOcUj6', 'SUPER_ADMIN', 'ACTIVE'),
(2, 'alice_wonder', 'alice@example.com', '$2a$12$J.EMR9WFk/jiOE3dDV4t7uklAeh41ecqVha0XAbxzkJo4RbpOcUj6', 'USER', 'ACTIVE'),
(3, 'bob_builder', 'bob@example.com', '$2a$12$J.EMR9WFk/jiOE3dDV4t7uklAeh41ecqVha0XAbxzkJo4RbpOcUj6', 'USER', 'ACTIVE'),
(4, 'charlie_chap', 'charlie@example.com', '$2a$12$J.EMR9WFk/jiOE3dDV4t7uklAeh41ecqVha0XAbxzkJo4RbpOcUj6', 'USER', 'BANNED');

-- 2. Профили
INSERT INTO profiles (user_id, first_name, last_name, city, birth_date, bio, is_private) VALUES
(1, 'Томас', 'Андерсон', 'Нью-Йорк', '1999-03-31', 'Следуй за белым кроликом.', false),
(2, 'Алиса', 'Селезнева', 'Москва', '2010-10-18', 'Люблю путешествовать во времени и космосе.', false),
(3, 'Боб', 'Строитель', 'Лондон', '1998-11-28', 'Можем ли мы это починить? Да, мы можем!', true),
(4, 'Чарли', 'Браун', 'Чикаго', '2005-02-14', 'Заблокированный пользователь за спам.', false);

-- 3. Посты
INSERT INTO posts (id, author_id, content) VALUES
(1, 2, 'Сегодня отличная погода для прогулки по парку! ☀️'),
(2, 3, 'Кто-нибудь знает хороший рецепт пиццы? Срочно нужно для вечеринки.'),
(3, 1, 'Система обновлена до версии 2.0. Все баги (надеюсь) пофикшены.'),
(4, 2, 'Мой кот опять скинул цветок с подоконника... Классика.');

-- 4. Комментарии
INSERT INTO comments (id, post_id, author_id, text) VALUES
(1, 1, 3, 'Завидую, у нас тут дождь весь день.'),
(2, 2, 2, 'Возьми готовое тесто, не мучайся!'),
(3, 3, 2, 'Ура! Наконец-то работает загрузка аватарок.'),
(4, 4, 1, 'Коты — это хаос во плоти.');

-- 5. Реакции (Лайки)
INSERT INTO post_reactions (id, post_id, user_id, type) VALUES
(1, 1, 3, 'LIKE'),
(2, 1, 1, 'LIKE'),
(3, 2, 2, 'LIKE'),
(4, 4, 3, 'LIKE');

-- 6. Друзья
INSERT INTO friendships (id, requester_id, addressee_id, status) VALUES
(1, 2, 3, 'ACCEPTED'),
(2, 1, 2, 'ACCEPTED'),
(3, 3, 1, 'PENDING');

-- 7. Чаты
INSERT INTO chats (id, user1, user2, last_message_text) VALUES
(1, 2, 3, 'Да, давай в 19:00.'),
(2, 1, 2, 'Привет, как дела с новым функционалом?');

-- 8. Сообщения
INSERT INTO messages (id, chat_id, sender_id, recipient_id, content, is_read) VALUES
(1, 1, 3, 2, 'Идем сегодня в кино?', true),
(2, 1, 2, 3, 'Да, давай в 19:00.', false),
(3, 2, 1, 2, 'Привет, как дела с новым функционалом?', true);

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('profiles_user_id_seq', (SELECT MAX(user_id) FROM profiles));
SELECT setval('posts_id_seq', (SELECT MAX(id) FROM posts));
SELECT setval('comments_id_seq', (SELECT MAX(id) FROM comments));
SELECT setval('post_reactions_id_seq', (SELECT MAX(id) FROM post_reactions));
SELECT setval('friendships_id_seq', (SELECT MAX(id) FROM friendships));
SELECT setval('chats_id_seq', (SELECT MAX(id) FROM chats));
SELECT setval('messages_id_seq', (SELECT MAX(id) FROM messages));