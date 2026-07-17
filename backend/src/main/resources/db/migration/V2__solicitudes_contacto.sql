CREATE TABLE solicitudes_contacto (
    id                                UUID PRIMARY KEY,
    nombre                            VARCHAR(120) NOT NULL,
    empresa                           VARCHAR(120),
    correo                            VARCHAR(254) NOT NULL,
    telefono                          VARCHAR(10) NOT NULL,
    servicio_de_interes               VARCHAR(40) NOT NULL,
    mensaje                           TEXT NOT NULL,
    estado                            VARCHAR(20) NOT NULL,
    consentimiento_aceptado           BOOLEAN NOT NULL,
    consentimiento_fecha_aceptacion   TIMESTAMPTZ NOT NULL,
    consentimiento_version_politica   VARCHAR(40) NOT NULL,
    fecha_creacion                    TIMESTAMPTZ NOT NULL,
    fecha_ultima_actualizacion        TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_solicitudes_contacto_estado ON solicitudes_contacto (estado);
