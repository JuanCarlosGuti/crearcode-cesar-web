import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormField, required, pattern, schema, form, validate, maxLength } from '@angular/forms/signals';

import { SolicitudesApi } from '../../api/solicitudes-api';
import { WhatsappCta } from '../../componentes/whatsapp-cta/whatsapp-cta';
import { EMPRESA, urlWhatsapp } from '../../../contenido/empresa';
import { CONSENTIMIENTO_CONTACTO } from '../../../contenido/legales';
import { HOME } from '../../../contenido/home';
import { METADATOS_CONTACTO } from '../../../contenido/metadatos-paginas';
import { Analitica } from '../../nucleo/analitica';
import { establecerMetadatosDePagina } from '../../nucleo/metadatos-pagina';

interface DatosFormularioContacto {
  nombre: string;
  empresa: string;
  correo: string;
  telefono: string;
  servicioDeInteres: string;
  mensaje: string;
  aceptaConsentimiento: boolean;
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
// Espeja SolicitudDeContacto.MAXIMO_CARACTERES_MENSAJE del backend: el
// servidor rechaza con 400 lo que pase de aqui, asi que el formulario
// lo dice antes de enviar (QA13 de la auditoria).
export const MAXIMO_CARACTERES_MENSAJE = 2000;
// Los tres maximos del dominio (DatosDeContacto y Correo del backend).
// Sin ellos, un nombre de 121 caracteres viajaba y volvia como un 400
// que el visitante no sabia a que campo atribuir (auditoria P2-2b).
export const MAXIMO_CARACTERES_NOMBRE = 120;
export const MAXIMO_CARACTERES_EMPRESA = 120;
export const MAXIMO_CARACTERES_CORREO = 254;
export const MENSAJE_ERROR_NOMBRE_LARGO = `El nombre no puede superar los ${MAXIMO_CARACTERES_NOMBRE} caracteres.`;
export const MENSAJE_ERROR_EMPRESA_LARGA = `El nombre de la empresa no puede superar los ${MAXIMO_CARACTERES_EMPRESA} caracteres.`;
export const MENSAJE_ERROR_MENSAJE_LARGO = `El mensaje no puede superar los ${MAXIMO_CARACTERES_MENSAJE} caracteres.`;
export const MENSAJE_ERROR_CONSENTIMIENTO = 'Necesitamos que aceptes el tratamiento de datos para poder contactarte.';

const ESQUEMA_CONTACTO = schema<DatosFormularioContacto>((campo) => {
  required(campo.nombre, { message: MENSAJE_ERROR_NOMBRE });
  // "   " pasa el required de Angular (es una cadena no vacia) pero el
  // dominio lo rechaza: sin esto el visitante recibia un 400 generico.
  validate(campo.nombre, ({ value }) =>
    value().length > 0 && value().trim().length === 0
      ? { kind: 'nombre-en-blanco', message: MENSAJE_ERROR_NOMBRE }
      : undefined,
  );
  maxLength(campo.nombre, MAXIMO_CARACTERES_NOMBRE, { message: MENSAJE_ERROR_NOMBRE_LARGO });
  maxLength(campo.empresa, MAXIMO_CARACTERES_EMPRESA, { message: MENSAJE_ERROR_EMPRESA_LARGA });

  required(campo.correo, { message: MENSAJE_ERROR_CORREO });
  pattern(campo.correo, FORMATO_CORREO, {
    message: MENSAJE_ERROR_CORREO,
    when: ({ value }) => value().length > 0,
  });
  maxLength(campo.correo, MAXIMO_CARACTERES_CORREO, { message: MENSAJE_ERROR_CORREO });

  required(campo.telefono, { message: MENSAJE_ERROR_TELEFONO });
  validate(campo.telefono, ({ value }) => {
    if (value().length === 0 || CELULAR_COLOMBIANO.test(normalizarTelefono(value()))) {
      return undefined;
    }
    return { kind: 'telefono-invalido', message: MENSAJE_ERROR_TELEFONO };
  });

  required(campo.servicioDeInteres, { message: MENSAJE_ERROR_SERVICIO });
  required(campo.mensaje, { message: MENSAJE_ERROR_MENSAJE });
  // Signal Forms proyecta esta regla como atributo maxlength del textarea
  // (por eso el template NO lo declara: [formField] lo prohibe).
  maxLength(campo.mensaje, MAXIMO_CARACTERES_MENSAJE, { message: MENSAJE_ERROR_MENSAJE_LARGO });
  required(campo.aceptaConsentimiento, { message: MENSAJE_ERROR_CONSENTIMIENTO });
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
  imports: [FormField, RouterLink, WhatsappCta],
})
export class ContactoPage {
  private readonly solicitudesApi = inject(SolicitudesApi);
  private readonly analitica = inject(Analitica);

  protected readonly opcionesServicio = OPCIONES_SERVICIO;
  protected readonly enviando = signal(false);
  protected readonly enviado = signal(false);
  protected readonly errorEnvio = signal(false);
  // El backend contesta 400 con un {mensaje} que dice exactamente que
  // esta mal; antes se tiraba y se mostraba el texto generico, asi que
  // el visitante no sabia que corregir (auditoria P2-2c).
  protected readonly motivoDelError = signal<string | null>(null);
  protected readonly mensajeWhatsapp = HOME.mensajeWhatsapp;
  // NAP visible en la pagina donde se busca (auditoria P1-7).
  protected readonly empresa = EMPRESA;
  // La autorizacion tiene que ser informada, e "informada" incluye
  // decir que los datos salen del pais (politica v2, seccion 13).
  protected readonly textoConsentimiento = CONSENTIMIENTO_CONTACTO;
  protected readonly urlCorreo = `mailto:${EMPRESA.correo}`;
  protected readonly urlWhatsappNumero = () => urlWhatsapp(HOME.mensajeWhatsapp);

  private readonly datos = signal<DatosFormularioContacto>({
    nombre: '',
    empresa: '',
    correo: '',
    telefono: '',
    servicioDeInteres: '',
    mensaje: '',
    aceptaConsentimiento: false,
    sitioWeb: '',
  });

  protected readonly formulario = form(this.datos, ESQUEMA_CONTACTO);

  constructor() {
    establecerMetadatosDePagina(() => ({ ...METADATOS_CONTACTO, ruta: '/contacto' }));
  }

  /**
   * En orden de aparicion. Antes el foco estaba cableado al checkbox de
   * consentimiento, que ademas es el ultimo campo: al enviar un
   * formulario vacio saltaba al final, y si el consentimiento ya estaba
   * marcado no se movia a ninguna parte (auditoria P2-2d).
   */
  private enfocarElPrimerCampoInvalido(): void {
    const enOrden = [
      this.formulario.nombre,
      this.formulario.correo,
      this.formulario.telefono,
      this.formulario.servicioDeInteres,
      this.formulario.mensaje,
      this.formulario.aceptaConsentimiento,
    ];
    enOrden.find((campo) => campo().invalid())?.().focusBoundControl();
  }

  protected enviar(evento: Event): void {
    evento.preventDefault();
    this.formulario().markAsTouched();
    if (!this.formulario().valid()) {
      this.enfocarElPrimerCampoInvalido();
      this.analitica.registrar('contact_form_error', { estado: 0, motivo: 'validacion-local' });
      return;
    }

    this.enviando.set(true);
    this.errorEnvio.set(false);
    this.motivoDelError.set(null);
    this.solicitudesApi.registrar(this.datos()).subscribe({
      next: () => {
        this.enviando.set(false);
        this.enviado.set(true);
        this.analitica.registrar('contact_form_submit');
      },
      error: (error: unknown) => {
        this.enviando.set(false);
        this.errorEnvio.set(true);
        this.motivoDelError.set(this.motivoDe(error));
        // Solo el estado HTTP: el motivo puede traer datos escritos por
        // el visitante y los eventos dicen que paso, no quien.
        this.analitica.registrar('contact_form_error', {
          estado: error instanceof HttpErrorResponse ? error.status : 0,
        });
      },
    });
  }

  /**
   * Solo se muestra el texto del servidor cuando es un 400: ese lo
   * escribimos nosotros y nombra el campo. Un 500 o una red caida
   * traen mensajes internos que al visitante no le dicen nada.
   */
  private motivoDe(error: unknown): string | null {
    if (error instanceof HttpErrorResponse && error.status === 400) {
      const mensaje = (error.error as { mensaje?: string } | null)?.mensaje;
      return mensaje && mensaje.trim().length > 0 ? mensaje : null;
    }
    return null;
  }
}
