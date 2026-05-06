
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS post_reactions CASCADE;
DROP TABLE IF EXISTS messages CASCADE;
DROP TABLE IF EXISTS friendships CASCADE;
DROP TABLE IF EXISTS profiles CASCADE;
DROP TABLE IF EXISTS posts CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS chats CASCADE;


CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(50)  NOT NULL,
    status        VARCHAR(50)  NOT NULL,
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    code          VARCHAR(255),
    expiry_code   TIMESTAMP
);


CREATE TABLE profiles (
    user_id    BIGSERIAL      PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    first_name VARCHAR(100),
    last_name  VARCHAR(100),
    city       VARCHAR(100),
    birth_date DATE,
    bio        VARCHAR(255),
    avatar_url VARCHAR(255),
    is_private BOOLEAN        NOT NULL DEFAULT false
);

CREATE TABLE posts (
    id          BIGSERIAL     PRIMARY KEY,
    author_id   BIGSERIAL     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content     VARCHAR(255)  NOT NULL,
    image_url   VARCHAR(255),
    created_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE comments (
    id            BIGSERIAL       PRIMARY KEY,
--     comment_order INT             NOT NULL,
    post_id       BIGINT          NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    author_id     BIGSERIAL       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    text          VARCHAR(255)    NOT NULL,
    created_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE post_reactions (
    id          BIGSERIAL   PRIMARY KEY,
    post_id     BIGINT      NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id     BIGSERIAL   NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type        VARCHAR(50) NOT NULL,
    created_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (post_id, user_id)
);

CREATE TABLE friendships (
    id              BIGSERIAL   PRIMARY KEY,
    requester_id    BIGSERIAL   NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    addressee_id    BIGSERIAL   NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status          VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (requester_id, addressee_id)
);

CREATE TABLE messages (
    id              BIGSERIAL       PRIMARY KEY,
    chat_id         BIGINT          NOT NULL,
    sender_id       BIGSERIAL            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recipient_id    BIGSERIAL            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content         VARCHAR(255)    NOT NULL,
    is_read         BOOLEAN         NOT NULL DEFAULT false,
    sent_at         TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE chats (
    id                  BIGSERIAL       PRIMARY KEY,
    user1               BIGSERIAL            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user2               BIGSERIAL            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    last_message_text   VARCHAR(255),
    last_sent           TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user1, user2)
);