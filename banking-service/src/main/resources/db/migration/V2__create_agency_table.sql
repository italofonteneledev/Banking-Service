CREATE TABLE IF NOT EXISTS tb_agency (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255),
    razao_social VARCHAR(255),
    cnpj VARCHAR(255),
    endereco_id INTEGER,
    CONSTRAINT fk_agency_address FOREIGN KEY (endereco_id) REFERENCES tb_address(id)
);
