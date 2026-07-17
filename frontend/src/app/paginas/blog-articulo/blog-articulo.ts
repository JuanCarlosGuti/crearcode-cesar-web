import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { marked } from 'marked';

import { ARTICULOS } from '../../../contenido/blog';

@Component({
  selector: 'app-pagina-blog-articulo',
  imports: [RouterLink],
  templateUrl: './blog-articulo.html',
  styleUrl: './blog-articulo.scss',
})
export class BlogArticuloPage {
  readonly slug = input.required<string>();

  protected readonly articulo = computed(() => ARTICULOS.find((a) => a.slug === this.slug()));
  protected readonly cuerpoHtml = computed(() => {
    const articulo = this.articulo();
    return articulo ? (marked.parse(articulo.cuerpoMarkdown, { async: false }) as string) : '';
  });
}
