package com.crearcode.leads.infraestructura.rest;

import java.util.List;

import com.crearcode.leads.dominio.InformeDeDiagnostico;

record InformeDiagnosticoResponse(String veredicto, List<OportunidadResponse> oportunidades) {

	static InformeDiagnosticoResponse desde(InformeDeDiagnostico informe) {
		return new InformeDiagnosticoResponse(informe.veredicto(), informe.oportunidades().stream()
				.map(o -> new OportunidadResponse(o.titulo(), o.detalle(), o.beneficio()))
				.toList());
	}

	record OportunidadResponse(String titulo, String detalle, String beneficio) {
	}

}
