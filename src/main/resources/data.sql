-- ==================== ТЕСТОВЫЕ ПОЛЬЗОВАТЕЛИ ====================

-- 1. Супер-админ
INSERT INTO users (username, email, password_hash, role, status)
VALUES (
           'superadmin',
           'superadmin@example.com',
           '$2a$12$Fl1NgaHZSjVCqqXVgx5sdusvo.2mXd9TOQgZQcAfo.EPWAdYH/vCi', -- пароль: admin123
           'SUPER_ADMIN',
           'ACTIVE'
       );

INSERT INTO profiles (user_id, first_name, last_name, city, birth_date, bio, is_private)
VALUES (
           (SELECT id FROM users WHERE username = 'superadmin'),
           'Admin',
           'Super',
           'Москва',
           '1990-01-01',
           'Главный администратор системы',
           false
       );

-- ==================== 2. Модератор ====================
INSERT INTO users (username, email, password_hash, role, status)
VALUES (
           'moderator',
           'moderator@example.com',
           '$2a$12$Fl1NgaHZSjVCqqXVgx5sdusvo.2mXd9TOQgZQcAfo.EPWAdYH/vCi', -- пароль: admin123
           'MODERATOR',
           'ACTIVE'
       );

INSERT INTO profiles (user_id, first_name, last_name, city, birth_date, bio, is_private)
VALUES (
           (SELECT id FROM users WHERE username = 'moderator'),
           'Modest',
           'Moderatorov',
           'Санкт-Петербург',
           '1992-05-15',
           'Модератор контента',
           false
       );

-- ==================== 3. Обычный пользователь ====================
INSERT INTO users (username, email, password_hash, role, status)
VALUES (
           'user',
           'user@example.com',
           '$2a$12$Fl1NgaHZSjVCqqXVgx5sdusvo.2mXd9TOQgZQcAfo.EPWAdYH/vCi', -- пароль: admin123
           'USER',
           'ACTIVE'
       );

INSERT INTO profiles (user_id, first_name, last_name, city, birth_date, bio, is_private)
VALUES (
           (SELECT id FROM users WHERE username = 'user'),
           'Ivan',
           'Ivanov',
           'Екатеринбург',
           '1995-08-20',
           'Обычный пользователь соцсети',
           true
       );

-- ==================== 4. Дополнительный пользователь для теста дружбы ====================
INSERT INTO users (username, email, password_hash, role, status)
VALUES (
           'friend',
           'friend@example.com',
           '$2a$12$Fl1NgaHZSjVCqqXVgx5sdusvo.2mXd9TOQgZQcAfo.EPWAdYH/vCi', -- пароль: admin123
           'USER',
           'ACTIVE'
       );

INSERT INTO profiles (user_id, first_name, last_name, city, birth_date, bio, is_private)
VALUES (
           (SELECT id FROM users WHERE username = 'friend'),
           'Petr',
           'Petrov',
           'Новосибирск',
           '1993-03-10',
           'Друг для теста дружбы',
           false
       );

-- ==================== ТЕСТОВЫЕ ПОСТЫ ====================

-- Посты от пользователя 'user'
INSERT INTO posts (author_id, content)
VALUES
    ((SELECT id FROM users WHERE username = 'user'), 'Мой первый пост в этой соцсети!'),
    ((SELECT id FROM users WHERE username = 'user'), 'Сегодня отличная погода для прогулки ☀️'),
    ((SELECT id FROM users WHERE username = 'user'), 'Изучаю SQL и базы данных. Очень интересно!');

-- Посты от пользователя 'friend'
INSERT INTO posts (author_id, content)
VALUES
    ((SELECT id FROM users WHERE username = 'superadmin'), 'Привет всем! Я только что зарегистрировался.'),
    ((SELECT id FROM users WHERE username = 'friend'), 'Люблю программировать по вечерам 💻');

-- ==================== ТЕСТОВАЯ ДРУЖБА ====================

-- user отправляет запрос в друзья friend
INSERT INTO friendships (requester_id, addressee_id, status)
VALUES (
           (SELECT id FROM users WHERE username = 'user'),
           (SELECT id FROM users WHERE username = 'friend'),
           'ACCEPTED'
       );

INSERT INTO friendships (requester_id, addressee_id, status)
VALUES (
    (SELECT id FROM users WHERE username = 'superadmin'),
    (SELECT id FROM users WHERE username = 'moderator'),
    'PENDING'
);