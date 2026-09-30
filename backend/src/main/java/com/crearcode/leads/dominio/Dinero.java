package com.crearcode.leads.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value object de un monto en pesos colombianos. La escala se normaliza
 * a pesos enteros (el COP no maneja centavos en la práctica comercial),
 * así dos montos equivalentes son iguales y los totales no arrastran
 * decimales fantasma.
 *
 * <p>La aritmética vive aquí y no en quien lo usa: es la única forma de
 * garantizar la invariante 2 del contexto — los totales los calcula el
 * dominio, nunca llegan de fuera.
 */
public record Dinero(BigDecimal monto) {

	public static final Dinero CERO = Dinero.de(0);

	public Dinero {
		if (monto == null) {
			throw new MontoInvalidoException("El monto no puede ser nulo");
		}
		if (monto.signum() < 0) {
			throw new MontoInvalidoException("El monto no puede ser negativo: " + monto);
		}
		monto = monto.setScale(0, RoundingMode.HALF_UP);
	}

	public static Dinero de(long pesos) {
		return new Dinero(BigDecimal.valueOf(pesos));
	}

	public Dinero mas(Dinero otro) {
		return new Dinero(this.monto.add(otro.monto));
	}

	public Dinero por(int cantidad) {
		return new Dinero(this.monto.multiply(BigDecimal.valueOf(cantidad)));
	}

	/**
	 * Resta sin permitir negativos: un saldo o un pendiente nunca pueden
	 * quedar por debajo de cero (F12). Quien necesite comparar antes, que
	 * use {@link #esMayorQue}.
	 */
	public Dinero menos(Dinero otro) {
		BigDecimal resultado = this.monto.subtract(otro.monto);
		if (resultado.signum() < 0) {
			throw new MontoInvalidoException(
					"No se puede restar " + otro.monto.toPlainString() + " de " + this.monto.toPlainString());
		}
		return new Dinero(resultado);
	}

	public boolean esMayorQue(Dinero otro) {
		return this.monto.compareTo(otro.monto) > 0;
	}

	public boolean esCero() {
		return this.monto.signum() == 0;
	}

	public Dinero porcentaje(Porcentaje porcentaje) {
		return new Dinero(this.monto
				.multiply(BigDecimal.valueOf(porcentaje.valor()))
				.divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP));
	}

}
