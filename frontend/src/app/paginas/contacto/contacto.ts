import { Component, signal } from '@angular/core';
import { FormField, required, pattern, schema, form, validate } from '@angular/forms/signals';

interface DatosFormularioContacto {
  nombre: string;
  empresa: string;
  correo: string;
  telefono: string;
  servicioDeInteres: string;
  mensaje: string;
  sitioWeb: string;
}

const FORMATO_CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const CELULAR_COLOMBIANO = /^3\d{9}$/;

function normalizarTelefono(valor: string): string {
  let soloDigitos = valor.replace(/[\s-]/g, '').replace(/^\+/, '');
  if (soloDigitos.startsWith('57') && soloDigitos.length === 12) {
    soloDigitos = soloDigitos.substring(2);
  }
  return soloDigitos;
}

export const MENSAJE_ERROR_NOMBRE = 'Cuéntanos tu nombre para poder saludarte bien.';
export const MENSAJE_ERROR_CORREO = 'Escribe un correo válido, ej. nombre@empresa.com.';
export const MENSAJE_ERROR_TELEFONO = 'Escribe un número de celular colombiano válido, ej. 300 123 4567.';
export const MENSAJE_ERROR_SERVICIO = 'Selecciona el servicio que te interesa.';
export const MENSAJE_ERROR_MENSAJE = 'Cuéntanos brevemente qué necesitas, así podemos ayudarte mejor.';

const ESQUEMA_CONTACTO = schema<DatosFormularioContacto>((campo) => {
  required(campo.nombre, { message: MENSAJE_ERROR_NOMBRE });

  required(campo.correo, { message: MENSAJE_ERROR_CORREO });
  pattern(campo.correo, FORMATO_CORREO, {
    message: MENSAJE_ERROR_CORREO,
    when: ({ value }) => value().length > 0,
  });

  required(campo.telefono, { message: MENSAJE_ERROR_TELEFONO });
  validate(campo.telefono, ({ value }) => {
    if (value().length === 0 || CELULAR_COLOMBIANO.test(normalizarTelefono(value()))) {
      return undefined;
    }
    return { kind: 'telefono-invalido', message: MENSAJE_ERROR_TELEFONO };
  });

  required(campo.servicioDeInteres, { message: MENSAJE_ERROR_SERVICIO });
  required(campo.mensaje, { message: MENSAJE_ERROR_MENSAJE });
});

export const OPCIONES_SERVICIO = [
  { valor: 'DESARROLLO_A_LA_MEDIDA', etiqueta: 'Desarrollo a la medida' },
  { valor: 'IA_Y_AUTOMATIZACION', etiqueta: 'IA y automatización' },
  { valor: 'SOLUCIONES_TECNOLOGICAS', etiqueta: 'Soluciones tecnológicas' },
  { valor: 'OTRO', etiqueta: 'Otro' },
] as const;

@Component({
  selector: 'app-pagina-contacto',
  templateUrl: './contacto.html',
  styleUrl: './contacto.scss',
  imports: [FormField],
})
export class ContactoPage {
  protected readonly opcionesServicio = OPCIONES_SERVICIO;

  private readonly datos = signal<DatosFormularioContacto>({
    nombre: '',
    empresa: '',
    correo: '',
    telefono: '',
    servicioDeInteres: '',
    mensaje: '',
    sitioWeb: '',
  });

  protected readonly formulario = form(this.datos, ESQUEMA_CONTACTO);

  protected enviar(evento: Event): void {
    evento.preventDefault();
    this.formulario().markAsTouched();
    if (!this.formulario().valid()) {
      return;
    }
    // ISS-050: integración con POST /api/solicitudes
  }
}
