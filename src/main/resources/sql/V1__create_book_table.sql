CREATE TABLE books_new (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- 알라딘 API 필드
    isbn13 VARCHAR(13) NOT NULL UNIQUE,
    title VARCHAR(300) NOT NULL,
    author VARCHAR(300) NOT NULL,
    publisher VARCHAR(200) NOT NULL,
    pub_date DATE NOT NULL,
    pub_year INT NOT NULL,
    cover_url VARCHAR(500) NOT NULL,
    aladin_link VARCHAR(500) NOT NULL,
    price_standard INT NOT NULL,
    description TEXT NOT NULL,
    item_page INT NOT NULL,
    category_id BIGINT NOT NULL,

    -- 서비스 고유 필드
    genre VARCHAR(50) NOT NULL,
    level INT NOT NULL DEFAULT 1,
    vocab_level VARCHAR(20) NOT NULL DEFAULT 'EASY',
    weight INT NOT NULL DEFAULT 0,

    -- 시스템 필드
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 인덱스
    INDEX idx_isbn13 (isbn13),
    INDEX idx_genre (genre),
    INDEX idx_published_date (pub_date),
    INDEX idx_pub_year (pub_year),
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;