import { Component, OnInit, inject } from '@angular/core';

import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-cliente-layout',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './cliente-layout.html',
  styleUrls: ['./cliente-layout.css'],
})
export class ClienteLayout implements OnInit {
  private router = inject(Router);

  nombreCliente = '';

  ngOnInit(): void {
    this.cargarNombreCliente();
  }

  cargarNombreCliente(): void {
    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    if (usuario.nombre) {
      this.nombreCliente = `${usuario.nombre} ${usuario.apellido || ''}`.trim();
      return;
    }
    this.nombreCliente = 'Cliente';
  }

  cerrarSesion(): void {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}
