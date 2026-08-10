// Stub del proveedor de IA para el e2e del asistente (ISS-117): imita
// el endpoint de chat/completions de Groq/OpenAI de forma determinista
// y sin red externa. El backend se arranca con
// GROQ_API_URL=http://localhost:9099/openai/v1 para usarlo.
import { createServer } from 'node:http';

const PORT = Number(process.env.PORT ?? 9099);

createServer((peticion, respuesta) => {
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

    const esDiagnostico = contexto.includes('VEREDICTO:');
    const debeEscalar = /precio|cuesta|cotiza|humano|persona/i.test(ultimaPregunta);
    const content = esDiagnostico
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
  console.log(`stub de Groq escuchando en http://localhost:${PORT}/openai/v1/chat/completions`);
});
