-- Fase F8 (HU-31/HU-32): tokens de un solo uso enviados por correo
-- (verificacion de cuenta y recuperacion de contrasena). Solo se
-- persiste el hash SHA-256 del valor -- el valor real viaja unicamente
-- en el enlace del correo. Sin poda de vencidos en F8 (follow-up
-- registrado en docs/05-backlog-issues.md).
CREATE TABLE tokens_de_usuario (
    id           UUID PRIMARY KEY,
    usuario_id   UUID NOT NULL REFERENCES usuarios (id),
    valor_hash   VARCHAR(64) NOT NULL,
    proposito    VARCHAR(20) NOT NULL,
    creado_en    TIMESTAMPTZ NOT NULL,
    expira_en    TIMESTAMPTZ NOT NULL,
    usado_en     TIMESTAMPTZ
);

CREATE UNIQUE INDEX idx_tokens_de_usuario_valor_hash ON tokens_de_usuario (valor_hash);
CREATE INDEX idx_tokens_de_usuario_usuario ON tokens_de_usuario (usuario_id);
