// Stub de los proveedores de IA para los e2e (ISS-117/ISS-130/ISS-235):
// imita el chat/completions de Groq/OpenAI, la API de mensajes de Claude
// (/v1/messages) y el endpoint de imágenes de Pollinations, de forma
// determinista y sin red externa. El backend se arranca con
// GROQ_API_URL=http://localhost:9099/openai/v1,
// POLLINATIONS_URL=http://localhost:9099 y, para que el demo recorra el
// camino de Claude sin gastar créditos, DEMO_PROVEEDOR_BOCETOS=claude,
// CLAUDE_API_URL=http://localhost:9099 y CLAUDE_API_KEY=stub.
import { createServer } from 'node:http';

const PORT = Number(process.env.PORT ?? 9099);

// PNG mínimo válido de 1x1 px (para que el <img> del demo renderice)
const PNG_1PX = Buffer.from(
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==',
  'base64',
);

const pieza = (tipo, titulo, elementos = []) => ({
  tipo,
  titulo,
  elementos: elementos.map(([texto, detalle = '']) => ({ texto, detalle })),
});

// Un boceto que usa casi todo el catálogo de piezas (ADR-16), para que
// el e2e y axe recorran cómo se dibuja cada una.
const BOCETO = JSON.stringify({
  titulo: 'App de pedidos para tu restaurante',
  funcionalidades: [
    'Menú del día con lo que hay disponible',
    'Pedidos a domicilio sin saturar el WhatsApp',
    'Hora de entrega que elige el cliente',
    'Estado del pedido en tiempo real',
    'Tablero de cocina con los pedidos del día',
  ],
  pantallas: [
    {
      nombre: 'Inicio',
      piezas: [
        pieza('ENCABEZADO', 'Sabor Valduparense'),
        pieza('BUSCADOR', 'Busca un plato'),
        pieza('BANNER', 'Menú del día listo desde las 11:30 a. m.', [['Ver menú']]),
        pieza('CATEGORIAS', '', [['Almuerzos'], ['Desayunos'], ['Bebidas'], ['Postres']]),
        pieza('TARJETAS', '', [
          ['Almuerzo ejecutivo', 'Sopa, seco y jugo natural'],
          ['Bandeja valduparense', 'Arroz, carne, plátano y ensalada'],
        ]),
        pieza('NAVEGACION', '', [['Inicio'], ['Pedidos'], ['Mi cuenta']]),
      ],
    },
    {
      nombre: 'Pedido',
      piezas: [
        pieza('ENCABEZADO', 'Tu pedido'),
        pieza('FORMULARIO', 'Datos de entrega', [
          ['Dirección', 'Ej. Calle 16 # 9-20'],
          ['Barrio', 'Ej. Novalito'],
        ]),
        pieza('HORARIOS', 'Hora de entrega', [['11:30 a. m.'], ['12:00 m.'], ['12:30 p. m.'], ['1:00 p. m.']]),
        pieza('BOTON', 'Confirmar pedido'),
      ],
    },
    {
      nombre: 'Cocina',
      piezas: [
        pieza('ENCABEZADO', 'Pedidos de hoy'),
        pieza('INDICADORES', '', [['Pedidos de hoy', '18'], ['En camino', '4']]),
        pieza('LISTA', '', [
          ['Ana · almuerzo ejecutivo', 'En camino'],
          ['Luis · bandeja', 'En cocina'],
          ['Marta · desayuno', 'Entregado'],
        ]),
        pieza('CHAT', '', [
          ['Hola, ¿ya salió mi pedido?', 'cliente'],
          ['Sí, va en camino. Llega en 10 minutos.', 'negocio'],
        ]),
      ],
    },
  ],
});

function leerCuerpo(peticion, alTerminar) {
  let cuerpo = '';
  peticion.on('data', (trozo) => (cuerpo += trozo));
  peticion.on('end', () => {
    let json = {};
    try {
      json = JSON.parse(cuerpo);
    } catch {
      // Cuerpo no JSON: se responde igual, el backend valida antes.
    }
    alTerminar(json);
  });
}

createServer((peticion, respuesta) => {
  // Rama de imágenes (Pollinations): GET /prompt/<descripcion>
  if (peticion.url?.startsWith('/prompt/')) {
    respuesta.setHeader('Content-Type', 'image/png');
    respuesta.end(PNG_1PX);
    return;
  }

  // Rama de Claude (API de mensajes): hoy solo la usa el demo de diseño.
  if (peticion.url?.startsWith('/v1/messages')) {
    leerCuerpo(peticion, () => {
      respuesta.setHeader('Content-Type', 'application/json');
      respuesta.end(
        JSON.stringify({
          id: 'msg_stub',
          type: 'message',
          role: 'assistant',
          model: 'claude-sonnet-5-5',
          content: [{ type: 'text', text: BOCETO }],
          stop_reason: 'end_turn',
          usage: { input_tokens: 2100, output_tokens: 1400, cache_creation_input_tokens: 0, cache_read_input_tokens: 0 },
        }),
      );
    });
    return;
  }

  leerCuerpo(peticion, (json) => {
    const mensajes = json.messages ?? [];
    const ultimaPregunta = mensajes.filter((m) => m.role === 'user').at(-1)?.content ?? '';
    const contexto = mensajes[0]?.content ?? '';

    const esBoceto = json.response_format?.json_schema?.name === 'boceto';
    const esDiagnostico = contexto.includes('VEREDICTO:');
    const debeEscalar = /precio|cuesta|cotiza|humano|persona/i.test(ultimaPregunta);
    const content = esBoceto
      ? BOCETO
      : esDiagnostico
        ? 'VEREDICTO: Tu negocio tiene un problema de tiempo, no de ventas.\n' +
          'OPORTUNIDAD: Respuestas automáticas | Las preguntas repetidas se contestan solas. | Dejas de contestar lo mismo todo el día.\n' +
          'OPORTUNIDAD: Pedidos en un solo lugar | Cada pedido queda registrado con su estado. | Nadie vuelve a preguntar en qué va ese pedido.\n' +
          'OPORTUNIDAD: Reportes que se arman solos | Ventas calculadas a partir de lo que registras. | Cierras el mes sin cuadrar nada a mano.'
        : debeEscalar
          ? 'Cada proyecto se cotiza a la medida según su alcance.\n[ESCALAR]'
          : 'Ofrecemos desarrollo a la medida, IA y automatización para pymes, y soluciones tecnológicas.';

    respuesta.setHeader('Content-Type', 'application/json');
    respuesta.end(JSON.stringify({ choices: [{ message: { role: 'assistant', content }, finish_reason: 'stop' }] }));
  });
}).listen(PORT, () => {
  console.log(`stub de IA escuchando en http://localhost:${PORT} (chat/completions + /v1/messages + /prompt)`);
});
