import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ARTICULOS } from '../../../contenido/blog';

@Component({
  selector: 'app-pagina-blog-listado',
  imports: [RouterLink],
  templateUrl: './blog-listado.html',
  styleUrl: './blog-listado.scss',
})
export class BlogListadoPage {
  protected readonly articulos = [...ARTICULOS].sort((a, b) => b.fecha.localeCompare(a.fecha));
}
