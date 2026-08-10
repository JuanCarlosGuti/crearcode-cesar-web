package com.crearcode.leads.infraestructura.rest;

import java.util.List;

import com.crearcode.leads.dominio.BocetoDeDemo;

record BocetoDemoResponse(String titulo, List<String> funcionalidades, String imagenBase64, String tipoMime) {

	static BocetoDemoResponse desde(BocetoDeDemo boceto) {
		return new BocetoDemoResponse(boceto.titulo(), boceto.funcionalidades(),
				boceto.imagen().base64(), boceto.imagen().tipoMime());
	}

}
