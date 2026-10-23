package com.crearcode.leads.infraestructura.demo;

/** Un boceto válido en el JSON del esquema, para las pruebas de los adaptadores. */
final class BocetoDePrueba {

	static final String JSON = """
			{
			  "titulo": "App de pedidos para tu restaurante",
			  "funcionalidades": ["Menú del día", "Pedidos por WhatsApp", "Estado del domicilio",
			                      "Historial de clientes", "Aviso de pedido listo"],
			  "pantallas": [
			    {"nombre": "Inicio", "piezas": [
			      {"tipo": "ENCABEZADO", "titulo": "Sabor Valduparense", "elementos": []},
			      {"tipo": "categorias", "titulo": "", "elementos": [
			        {"texto": "Almuerzos", "detalle": ""}, {"texto": "Desayunos", "detalle": ""}]},
			      {"tipo": "BOTON", "titulo": "Pedir a domicilio", "elementos": []}
			    ]},
			    {"nombre": "Pedidos", "piezas": [
			      {"tipo": "LISTA", "titulo": "Pedidos de hoy", "elementos": [
			        {"texto": "Ana · almuerzo ejecutivo", "detalle": "En camino"},
			        {"texto": "Luis · bandeja paisa", "detalle": "En cocina"}]},
			      {"tipo": "NAVEGACION", "titulo": "", "elementos": [
			        {"texto": "Inicio", "detalle": ""}, {"texto": "Pedidos", "detalle": ""}, {"texto": "Perfil", "detalle": ""}]}
			    ]}
			  ]
			}
			""";

	private BocetoDePrueba() {
	}

}
