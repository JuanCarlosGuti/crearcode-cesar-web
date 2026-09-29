-- Autorizacion comercial separada de la obligatoria (politica de datos
-- v2, seccion 13: "Las finalidades comerciales opcionales tienen su
-- propia casilla"). Un solo booleano para las dos hacia imposible saber
-- a quien se le puede escribir sin revisar correos viejos.
--
-- DEFAULT FALSE y NOT NULL: las solicitudes anteriores a esta columna
-- nunca dieron esa autorizacion, y asumir que si seria inventarla.
ALTER TABLE solicitudes_contacto
    ADD COLUMN consentimiento_comunicaciones_comerciales BOOLEAN NOT NULL DEFAULT FALSE;
