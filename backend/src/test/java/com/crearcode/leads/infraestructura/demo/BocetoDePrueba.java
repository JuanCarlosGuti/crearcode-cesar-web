package com.crearcode.leads.infraestructura.demo;

/** Un boceto válido en el JSON del esquema, para las pruebas de los adaptadores. */
public final class BocetoDePrueba {

	public static final String JSON = """
			{
			  "identidad": {
			    "nombre": "Sabor Valduparense",
			    "eslogan": "Comida casera hasta tu puerta",
			    "paleta": "CALIDA",
			    "logo": {"forma": "CIRCULO", "simbolo": "🍲", "letra": "SENCILLA"}
			  },
			  "titulo": "App de pedidos para tu restaurante",
			  "funcionalidades": ["Menú del día", "Pedidos por WhatsApp", "Estado del domicilio",
			                      "Historial de clientes", "Aviso de pedido listo"],
			  "pantallas": [
			    {"nombre": "Inicio", "piezas": [
			      {"tipo": "ENCABEZADO", "titulo": "Sabor Valduparense", "elementos": []},
			      {"tipo": "categorias", "titulo": "", "elementos": [
			        {"texto": "Almuerzos", "detalle": "", "icono": "🍛"}, {"texto": "Desayunos", "detalle": "", "icono": "🍳"}]},
			      {"tipo": "BOTON", "titulo": "Pedir a domicilio", "elementos": []}
			    ]},
			    {"nombre": "Pedidos", "piezas": [
			      {"tipo": "LISTA", "titulo": "Pedidos de hoy", "elementos": [
			        {"texto": "Ana · almuerzo ejecutivo", "detalle": "En camino", "icono": ""},
			        {"texto": "Luis · bandeja paisa", "detalle": "En cocina", "icono": ""}]},
			      {"tipo": "NAVEGACION", "titulo": "", "elementos": [
			        {"texto": "Inicio", "detalle": "", "icono": "🏠"}, {"texto": "Pedidos", "detalle": "", "icono": "🧾"},
			        {"texto": "Perfil", "detalle": "", "icono": "👤"}]}
			    ]}
			  ]
			}
			""";

	private BocetoDePrueba() {
	}

}
