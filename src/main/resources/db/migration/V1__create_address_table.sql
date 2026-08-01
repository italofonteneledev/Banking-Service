CREATE TABLE IF NOT EXISTS tb_address (
    id SERIAL PRIMARY KEY,
    rua VARCHAR(255),
    logradouro VARCHAR(255),
    complemento VARCHAR(255),
    numero INTEGER
);
