package com.crearcode.leads.dominio;

/**
 * Borra la cuenta del cliente que lo pide. Derecho de supresión de la
 * Ley 1581 de 2012, que la política de datos v2 promete: no basta con
 * que el derecho exista si ejercerlo depende de que alguien atienda un
 * correo a mano.
 *
 * <p>
 * No borra las cotizaciones ni las solicitudes de contacto: son
 * registros comerciales y contables con su propio plazo legal de
 * conservación (política v2, §9). Lo que se borra es la cuenta y lo
 * que solo existe por ella.
 */
public interface EliminarMiCuentaUseCase {

	void eliminar(String correo);

}
