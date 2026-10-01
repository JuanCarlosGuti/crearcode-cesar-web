-- Fase F12 (HU-49 a HU-57): el portal de proyectos del cliente.
-- Modelo en docs/03 Parte 5; decisiones 21 a 30 de docs/10.
--
-- Como en cotizaciones, los montos van en pesos enteros y NINGUN total
-- se persiste: el avance, lo cobrado, lo pagado y el saldo se calculan
-- en el dominio (ADR-15) para que no puedan divergir.
--
-- El cliente se identifica por su correo y no por una clave foranea a
-- usuarios (ADR-14): el proyecto existe aunque el cliente todavia no
-- tenga cuenta, y sobrevive si la elimina, porque son datos del
-- contrato (la politica los conserva por la relacion mas 10 anios).
CREATE TABLE proyectos (
    id                      UUID PRIMARY KEY,
    cliente_correo          VARCHAR(254) NOT NULL,
    cliente_nombre          VARCHAR(120) NOT NULL,
    -- UNIQUE: la invariante 6 (un solo proyecto por cotizacion) tambien
    -- la cuida la base, por si dos peticiones llegan a la vez.
    origen_cotizacion_id    UUID UNIQUE REFERENCES cotizaciones (id),
    nombre                  VARCHAR(120) NOT NULL,
    descripcion             VARCHAR(1000),
    inicio                  DATE NOT NULL,
    entrega_estimada        DATE,
    impuesto_porcentaje     INTEGER NOT NULL,
    estado                  VARCHAR(20) NOT NULL,
    fin_de_garantia         TIMESTAMPTZ,
    creado_en               TIMESTAMPTZ NOT NULL,
    actualizado_en          TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_proyectos_estado ON proyectos (estado);
-- La vista del cliente filtra por su correo, sin distinguir mayusculas.
CREATE INDEX idx_proyectos_cliente_correo ON proyectos (LOWER(cliente_correo));

CREATE TABLE fases_de_proyecto (
    id                          UUID PRIMARY KEY,
    proyecto_id                 UUID NOT NULL REFERENCES proyectos (id) ON DELETE CASCADE,
    posicion                    INTEGER NOT NULL,
    nombre                      VARCHAR(120) NOT NULL,
    objetivo                    VARCHAR(500),
    inicio_planeado             DATE,
    fin_planeado                DATE,
    resumen_para_el_cliente     VARCHAR(2000)
);

CREATE INDEX idx_fases_de_proyecto_proyecto ON fases_de_proyecto (proyecto_id);

CREATE TABLE entregables_de_proyecto (
    id                      UUID PRIMARY KEY,
    fase_id                 UUID NOT NULL REFERENCES fases_de_proyecto (id) ON DELETE CASCADE,
    posicion                INTEGER NOT NULL,
    nombre                  VARCHAR(120) NOT NULL,
    descripcion             VARCHAR(1000),
    valor                   NUMERIC(15, 0) NOT NULL CHECK (valor >= 0),
    momento_de_cobro        VARCHAR(20) NOT NULL,
    es_cambio_de_alcance    BOOLEAN NOT NULL,
    estado                  VARCHAR(20) NOT NULL,
    url_demo                VARCHAR(500),
    nota_de_ajustes         VARCHAR(1000),
    aprobado_en             TIMESTAMPTZ,
    aprobado_por            VARCHAR(20)
);

CREATE INDEX idx_entregables_de_proyecto_fase ON entregables_de_proyecto (fase_id);

CREATE TABLE pagos_de_proyecto (
    id                  UUID PRIMARY KEY,
    proyecto_id         UUID NOT NULL REFERENCES proyectos (id) ON DELETE CASCADE,
    -- DEFERRABLE: al guardar, el adaptador reescribe fases, entregables
    -- y pagos del proyecto en una sola transaccion, y el orden de los
    -- INSERT no esta garantizado. Diferida, la referencia se comprueba
    -- al confirmar, cuando ya esta todo.
    entregable_id       UUID NOT NULL REFERENCES entregables_de_proyecto (id) DEFERRABLE INITIALLY DEFERRED,
    posicion            INTEGER NOT NULL,
    monto               NUMERIC(15, 0) NOT NULL CHECK (monto > 0),
    fecha               DATE NOT NULL,
    medio               VARCHAR(20) NOT NULL,
    origen              VARCHAR(20) NOT NULL,
    referencia          VARCHAR(200),
    registrado_por      VARCHAR(254) NOT NULL
);

CREATE INDEX idx_pagos_de_proyecto_proyecto ON pagos_de_proyecto (proyecto_id);
CREATE INDEX idx_pagos_de_proyecto_entregable ON pagos_de_proyecto (entregable_id);
