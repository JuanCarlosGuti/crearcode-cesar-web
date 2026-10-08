import { isPlatformBrowser } from '@angular/common';
import { Directive, ElementRef, OnInit, PLATFORM_ID, afterNextRender, inject } from '@angular/core';

type Campo = HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement;

interface LoEscrito {
  campo: Campo;
  valor: string | boolean;
}

/**
 * Que no se pierda lo que el visitante escribe antes de que la página
 * hidrate (ISS-228).
 *
 * <p>
 * Las páginas públicas llegan prerenderizadas: los campos están en
 * pantalla antes de que Angular tome el control. Al hidratar,
 * `[formField]` escribe en cada campo el valor de su modelo, que todavía
 * está vacío, y borra lo escrito. En un celular lento eso es el primer
 * segundo y medio, y en producción se perdían así el nombre y el correo
 * del formulario de contacto.
 *
 * <p>
 * La directiva va en el contenedor del formulario. Su `ngOnInit` corre
 * antes de que se actualicen los campos que contiene: ahí anota lo que
 * cambió respecto de lo prerenderizado. Terminada la hidratación lo
 * devuelve a cada campo con un evento `input`, que es lo que escucha
 * Signal Forms, así el modelo lo recibe y el campo no queda «tocado».
 * Se devuelve aunque el campo no se haya borrado: en ese caso el modelo
 * igual estaba vacío y el envío lo habría rechazado.
 */
@Directive({ selector: '[appConservarLoEscrito]' })
export class ConservarLoEscrito implements OnInit {
  private readonly contenedor = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly esNavegador = isPlatformBrowser(inject(PLATFORM_ID));
  private anotado: LoEscrito[] = [];

  constructor() {
    afterNextRender(() => {
      devolver(this.anotado);
      this.anotado = [];
    });
  }

  ngOnInit(): void {
    if (this.esNavegador) {
      this.anotado = anotar(this.contenedor);
    }
  }
}

function anotar(contenedor: HTMLElement): LoEscrito[] {
  const anotado: LoEscrito[] = [];
  for (const campo of Array.from(contenedor.querySelectorAll<Campo>('input, textarea, select'))) {
    if (campo instanceof HTMLInputElement && (campo.type === 'checkbox' || campo.type === 'radio')) {
      if (campo.checked !== campo.defaultChecked) {
        anotado.push({ campo, valor: campo.checked });
      }
    } else if (campo instanceof HTMLSelectElement) {
      if (campo.selectedIndex !== opcionPorDefecto(campo)) {
        anotado.push({ campo, valor: campo.value });
      }
    } else if (campo.value !== campo.defaultValue) {
      anotado.push({ campo, valor: campo.value });
    }
  }
  return anotado;
}

/**
 * La que el navegador muestra sin que nadie toque la lista: la última
 * marcada en el HTML o, si no hay ninguna, la primera.
 */
function opcionPorDefecto(lista: HTMLSelectElement): number {
  const opciones = Array.from(lista.options);
  for (let i = opciones.length - 1; i >= 0; i--) {
    if (opciones[i].defaultSelected) {
      return i;
    }
  }
  return opciones.length > 0 ? 0 : -1;
}

function devolver(anotado: LoEscrito[]): void {
  for (const { campo, valor } of anotado) {
    if (!campo.isConnected) {
      continue;
    }
    if (typeof valor === 'boolean') {
      (campo as HTMLInputElement).checked = valor;
    } else {
      campo.value = valor;
    }
    campo.dispatchEvent(new Event('input', { bubbles: true }));
  }
}
