import { Component, inject } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-recuperar-contrasena',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './recuperar-contrasena.html',
  styleUrl: './recuperar-contrasena.css',
})
export class RecuperarContrasena {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private http = inject(HttpClient);
  // ← OJO: nombre de clase
  contrasena = '';
  confirmar = '';
  token = '';

  constructor() {
    this.token = this.route.snapshot.queryParams['token'];
  }

  restablecer() {
    if (this.contrasena !== this.confirmar) {
      return alert('Las contraseñas no coinciden');
    }

    this.http
      .post('http://localhost:8080/api/auth/restablecer-contrasena', {
        token: this.token,
        contrasena: this.contrasena,
      })
      .subscribe({
        next: () => {
          alert('Contraseña actualizada. Ya puedes iniciar sesión.');
          this.router.navigate(['/']);
        },
        error: () => alert('Error al restablecer'),
      });
  }
}
