CREATE TABLE usuarios (
    id                UUID PRIMARY KEY,
    correo            VARCHAR(254) NOT NULL,
    contrasena_hash   VARCHAR(255) NOT NULL,
    rol               VARCHAR(20) NOT NULL
);

CREATE UNIQUE INDEX idx_usuarios_correo ON usuarios (LOWER(correo));
