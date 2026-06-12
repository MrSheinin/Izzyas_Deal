DROP TABLE IF EXISTS games_genres;
DROP TABLE IF EXISTS genres;
DROP TABLE IF EXISTS game_market_data;
DROP TABLE IF EXISTS games;

CREATE TABLE games
(
    app_id bigint primary key,
    name varchar(255),
    type varchar(100),
    description text,
    recommendations integer,
    windows boolean,
    mac boolean,
    linux boolean,
    is_coming_soon boolean,
    release_date_parsed date,
    header_image_url text,
    created_at timestamp
);

CREATE TABLE game_market_data
(
    app_id bigint primary key references games(app_id) on delete cascade,
    initial_price integer,
    final_price integer,
    discount_percent integer,
    updated_at timestamp
);


CREATE TABLE genres
(
    genre_id bigint primary key,
    name varchar(255) not null
);

CREATE TABLE games_genres
(
    app_id bigint references games(app_id) on delete cascade,
    genre_id bigint references genres(genre_id) on delete cascade,
    primary key (app_id, genre_id) -- Составной первичный ключ
);


-- Индексы
CREATE INDEX idx_games_recommendations ON games(recommendations);
CREATE INDEX idx_market_updated_at ON game_market_data(updated_at);

CREATE INDEX idx_games_genres_genre_id ON games_genres(genre_id);