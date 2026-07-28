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

    const debeEscalar = /precio|cuesta|cotiza|humano|persona/i.test(ultimaPregunta);
    const content = debeEscalar
      ? 'Cada proyecto se cotiza a la medida según su alcance.\n[ESCALAR]'
      : 'Ofrecemos desarrollo a la medida, IA y automatización para pymes, y soluciones tecnológicas.';

    respuesta.setHeader('Content-Type', 'application/json');
    respuesta.end(JSON.stringify({ choices: [{ message: { role: 'assistant', content } }] }));
  });
}).listen(PORT, () => {
  console.log(`stub de Groq escuchando en http://localhost:${PORT}/openai/v1/chat/completions`);
});
