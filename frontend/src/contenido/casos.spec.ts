import { CASOS } from './casos';

/**
 * Guarda de honestidad del portafolio. No prueba componentes: prueba el
 * contenido, porque el riesgo aqui no es que la pagina se rompa sino
 * que diga algo que no podemos sustentar.
 *
 * Viene de la misma regla que saco los testimonios ficticios en F10e.
 * Lo que se muestra son emprendimientos propios y codigo abierto, no
 * encargos de clientes, y cada uno tiene que poder comprobarse.
 */
describe('Proyectos del portafolio', () => {
  it('no queda ningun placeholder sin reemplazar', () => {
    for (const proyecto of CASOS) {
      const texto = `${proyecto.titulo} ${proyecto.reto} ${proyecto.solucion} ${proyecto.resultado}`;
      expect(texto).not.toMatch(/\[|placeholder/i);
    }
  });

  it('cada proyecto lleva un enlace que el visitante puede abrir', () => {
    // Sin esto la seccion es una lista de afirmaciones: el enlace es lo
    // que la vuelve comprobable, y es la razon por la que se puede
    // publicar sin tener todavia clientes.
    expect(CASOS.length).toBeGreaterThan(0);
    for (const proyecto of CASOS) {
      expect(proyecto.enlace.url).toMatch(/^https:\/\//);
      expect(proyecto.enlace.etiqueta.length).toBeGreaterThan(0);
    }
  });

  it('ninguno se presenta como trabajo hecho para un cliente', () => {
    // Un lector asume "cliente" en cuanto lee "caso de exito": esa es
    // la mentira por omision que esta seccion tiene que evitar.
    for (const proyecto of CASOS) {
      const texto = `${proyecto.reto} ${proyecto.solucion} ${proyecto.resultado}`.toLowerCase();
      expect(texto).not.toContain('el cliente');
      expect(texto).not.toContain('caso de exito');
      expect(texto).not.toContain('caso de éxito');
    }
  });

  it('cada proyecto declara a que linea de negocio corresponde', () => {
    const lineas = ['Desarrollo a la medida', 'IA y automatización', 'Soluciones tecnológicas'];
    for (const proyecto of CASOS) {
      expect(lineas).toContain(proyecto.linea);
    }
  });

  it('los slugs son unicos y aptos para URL', () => {
    const slugs = CASOS.map((proyecto) => proyecto.slug);
    expect(new Set(slugs).size).toBe(slugs.length);
    for (const slug of slugs) {
      expect(slug).toMatch(/^[a-z0-9-]+$/);
    }
  });
});
