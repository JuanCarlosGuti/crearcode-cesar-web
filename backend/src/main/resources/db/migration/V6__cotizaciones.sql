-- Fase F11 (HU-44 a HU-48): cotizaciones del pipeline comercial.
-- La cuenta de cobro quedo FUERA del alcance (decision 18 de
-- docs/10-vision-v2.md): como persona juridica la empresa esta obligada
-- a factura electronica DIAN, asi que esta tabla es solo de propuestas
-- comerciales, sin efecto fiscal.
--
-- Los montos van en pesos enteros (NUMERIC sin decimales): el COP no
-- maneja centavos en la practica comercial y el VO Dinero normaliza la
-- escala. Los totales NO se persisten: se calculan desde los items
-- (invariante 2 del contexto) para que no puedan divergir.
CREATE TABLE cotizaciones (
    id                      UUID PRIMARY KEY,
    numero                  VARCHAR(20),
    origen_solicitud_id     UUID REFERENCES solicitudes_contacto (id),
    cliente_nombre          VARCHAR(120) NOT NULL,
    cliente_correo          VARCHAR(254) NOT NULL,
    cliente_telefono        VARCHAR(40),
    cliente_identificacion  VARCHAR(40),
    impuesto_porcentaje     INTEGER NOT NULL,
    estado                  VARCHAR(20) NOT NULL,
    notas                   TEXT,
    creada_en               TIMESTAMPTZ NOT NULL,
    valida_hasta            TIMESTAMPTZ NOT NULL,
    enviada_en              TIMESTAMPTZ,
    respondida_en           TIMESTAMPTZ
);

-- El numero es unico entre las que ya salieron; los borradores lo tienen
-- en NULL y en Postgres varios NULL no chocan en un indice unico.
CREATE UNIQUE INDEX idx_cotizaciones_numero ON cotizaciones (numero);
CREATE INDEX idx_cotizaciones_estado ON cotizaciones (estado);
-- La vista del cliente filtra por su correo, sin distinguir mayusculas.
CREATE INDEX idx_cotizaciones_cliente_correo ON cotizaciones (LOWER(cliente_correo));

CREATE TABLE items_de_cotizacion (
    id              UUID PRIMARY KEY,
    cotizacion_id   UUID NOT NULL REFERENCES cotizaciones (id) ON DELETE CASCADE,
    posicion        INTEGER NOT NULL,
    descripcion     VARCHAR(200) NOT NULL,
    cantidad        INTEGER NOT NULL,
    valor_unitario  NUMERIC(15, 0) NOT NULL
);

CREATE INDEX idx_items_de_cotizacion_cotizacion ON items_de_cotizacion (cotizacion_id);

-- Consecutivo por anio. Se actualiza con UPDATE ... RETURNING, que es
-- atomico: dos envios simultaneos no pueden llevarse el mismo numero
-- (invariante 5). Una secuencia de Postgres no serviria porque hay que
-- reiniciar la cuenta cada anio.
CREATE TABLE consecutivos_de_cotizacion (
    anio            INTEGER PRIMARY KEY,
    ultimo_numero   INTEGER NOT NULL
);
