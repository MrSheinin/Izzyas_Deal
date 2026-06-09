DROP TABLE IF EXISTS games;
DROP TABLE IF EXISTS game_market_data;

create table games
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

create table game_market_data
(
    app_id bigint primary key,
    initial_price integer,
    final_price integer,
    discount_percent integer,
    updated_at timestamp
);

create index idx_games_recommendations
    on games(recommendations);

create index idx_market_updated_at
    on game_market_data(updated_at);