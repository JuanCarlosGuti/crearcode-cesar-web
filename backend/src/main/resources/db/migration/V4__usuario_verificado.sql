-- Fase F8 (cuentas de cliente, HU-30/HU-31): los clientes nacen sin
-- verificar y no pueden iniciar sesion hasta abrir el enlace del correo.
-- Las filas existentes son admins sembrados (no pasan por verificacion
-- por correo), por eso el backfill a true.
ALTER TABLE usuarios ADD COLUMN verificado BOOLEAN NOT NULL DEFAULT false;

UPDATE usuarios SET verificado = true;
