package com.crearcode.leads.infraestructura.rest;

/**
 * Error del asistente con un código estable para que la interfaz
 * decida qué mostrar: {@code limite-anonimo} (CTA a registro),
 * {@code limite-registrado} o {@code no-disponible} (alternativa
 * humana). Los textos visibles viven en el frontend (ADR-05).
 */
record ErrorAsistenteResponse(String mensaje, String codigo) {

}
