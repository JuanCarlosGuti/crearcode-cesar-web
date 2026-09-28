# AGENTS.md — Crear Code Cesar S.A.S. · Sitio web corporativo

**Las instrucciones para agentes viven en [`CLAUDE.md`](CLAUDE.md).** Este
archivo existía como copia y se desfasó: llegó a describir Render, Neon,
el correo por Gmail y una paleta que ya no existen (auditoría del 28 sep
2026). Una copia que se desactualiza en silencio es peor que un enlace.

Reglas duras del proyecto, por si el agente solo lee este archivo:

- **Responder siempre en español.**
- **`docs/` es la fuente de verdad viva.** Si una decisión no está
  documentada allí, no se asume: se pregunta y se actualiza el documento
  antes de seguir.
- **TDD real**: test que falla → implementación mínima → refactor.
- **Un issue ≈ un commit**, con tests en verde y ArchUnit en verde.
- **No se avanza de fase sin el OK explícito del usuario.**
- **Nada de secretos en el repositorio**: todo lo configurable va por
  variables de entorno; los nombres en `.kamal/secrets`, los valores en
  los secretos del repositorio de GitHub.
- **Diffs siempre visibles**: el usuario revisa cada cambio.

El resto —estado del proyecto, stack, arranque local, API, despliegue con
Kamal, decisiones ya resueltas y pendientes— está en `CLAUDE.md`, que se
mantiene al día.
