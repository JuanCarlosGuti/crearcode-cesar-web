// Stub de los proveedores de IA para los e2e (ISS-117/ISS-130): imita
// el chat/completions de Groq/OpenAI Y el endpoint de imágenes de
// Pollinations, de forma determinista y sin red externa. El backend se
// arranca con GROQ_API_URL=http://localhost:9099/openai/v1 y
// POLLINATIONS_URL=http://localhost:9099 para usarlo.
import { createServer } from 'node:http';

const PORT = Number(process.env.PORT ?? 9099);

// PNG mínimo válido de 1x1 px (para que el <img> del demo renderice)
const PNG_1PX = Buffer.from(
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==',
  'base64',
);

createServer((peticion, respuesta) => {
  // Rama de imágenes (Pollinations): GET /prompt/<descripcion>
  if (peticion.url?.startsWith('/prompt/')) {
    respuesta.setHeader('Content-Type', 'image/png');
    respuesta.end(PNG_1PX);
    return;
  }

  let cuerpo = '';
  peticion.on('data', (trozo) => (cuerpo += trozo));
  peticion.on('end', () => {
    let ultimaPregunta = '';
    try {
      const mensajes = JSON.parse(cuerpo).messages ?? [];
      ultimaPregunta = mensajes.filter((m) => m.role === 'user').at(-1)?.content ?? '';
    } catch {
      // Cuerpo no JSON: se responde igual, el backend valida antes.
    }

    let contexto = '';
    try {
      contexto = JSON.parse(cuerpo).messages?.[0]?.content ?? '';
    } catch {
      // Sin contexto: cae en la rama genérica.
    }

    const esDemo = contexto.includes('TITULO:');
    const esDiagnostico = contexto.includes('VEREDICTO:');
    const debeEscalar = /precio|cuesta|cotiza|humano|persona/i.test(ultimaPregunta);
    const content = esDemo
      ? 'TITULO: App de pedidos para tu restaurante\n' +
        'FUNCIONALIDAD: Menú digital con disponibilidad en tiempo real\n' +
        'FUNCIONALIDAD: Pedidos que entran ordenados a la cocina\n' +
        'FUNCIONALIDAD: Pago con link o contraentrega\n' +
        'FUNCIONALIDAD: Seguimiento del domicilio para el cliente\n' +
        'FUNCIONALIDAD: Reporte diario de ventas por producto'
      : esDiagnostico
        ? 'VEREDICTO: Tu negocio tiene un problema de tiempo, no de ventas.\n' +
          'OPORTUNIDAD: Respuestas automáticas | Las preguntas repetidas se contestan solas. | Dejas de contestar lo mismo todo el día.\n' +
          'OPORTUNIDAD: Pedidos en un solo lugar | Cada pedido queda registrado con su estado. | Nadie vuelve a preguntar en qué va ese pedido.\n' +
          'OPORTUNIDAD: Reportes que se arman solos | Ventas calculadas a partir de lo que registras. | Cierras el mes sin cuadrar nada a mano.'
        : debeEscalar
          ? 'Cada proyecto se cotiza a la medida según su alcance.\n[ESCALAR]'
          : 'Ofrecemos desarrollo a la medida, IA y automatización para pymes, y soluciones tecnológicas.';

    respuesta.setHeader('Content-Type', 'application/json');
    respuesta.end(JSON.stringify({ choices: [{ message: { role: 'assistant', content } }] }));
  });
}).listen(PORT, () => {
  console.log(`stub de IA escuchando en http://localhost:${PORT} (chat/completions + /prompt)`);
});
