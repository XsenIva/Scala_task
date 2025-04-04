
CREATE TABLE players (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    logine VARCHAR(255) NOT NULL,
    passwd VARCHAR(255) NOT NULL
);

CREATE TABLE teams (
    id SERIAL PRIMARY KEY,
    team_name VARCHAR(50) NOT NULL UNIQUE,
    player_id INTEGER,
    FOREIGN KEY (player_id) REFERENCES players(id)
);


CREATE TABLE games (
    id SERIAL PRIMARY KEY,
    status_game VARCHAR(20) NOT NULL,
    creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE team_game (
    game_id INTEGER,
    team_id INTEGER,
    PRIMARY KEY (game_id, team_id),
    FOREIGN KEY (game_id) REFERENCES games(id),
    FOREIGN KEY (team_id) REFERENCES teams(id)
);


CREATE TABLE sessions (
    id SERIAL PRIMARY KEY,
    player_id INTEGER NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    creation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiration_time TIMESTAMP NOT NULL,
    FOREIGN KEY (player_id) REFERENCES players(id)
);

CREATE TABLE moves (
    id SERIAL PRIMARY KEY,
    game_id INTEGER NOT NULL,
    player_id INTEGER NOT NULL,
    x_coordinate INTEGER NOT NULL,
    y_coordinate INTEGER NOT NULL,
    color VARCHAR(20) NOT NULL,
    move_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    points INTEGER,
    FOREIGN KEY (game_id) REFERENCES games(id),
    FOREIGN KEY (player_id) REFERENCES players(id)
);