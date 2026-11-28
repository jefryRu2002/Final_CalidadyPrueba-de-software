import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);

  private apiUrl = 'http://localhost:8080/api';

  // Login con tu BD (tabla credenciales)
  login(usuario: string, contrasena: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/login`, { usuario, contrasena });
  }

  // Registro (inserta en persona + credenciales + cliente)
  register(datos: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/register`, datos);
  }

  // Guardar sesión en localStorage
  guardarSesion(data: any) {
    localStorage.setItem('usuario', JSON.stringify(data));
  }

  // Obtener sesión
  obtenerSesion() {
    const data = localStorage.getItem('usuario');
    return data ? JSON.parse(data) : null;
  }

  // Cerrar sesión
  cerrarSesion() {
    localStorage.removeItem('usuario');
    localStorage.removeItem('idPersona'); // Limpieza extra por si acaso
    localStorage.removeItem('token'); // Limpieza extra por si acaso
  }

  // Verificar autenticado
  isAuthenticated(): boolean {
    return !!this.obtenerSesion();
  }

  // ✅ NUEVO MÉTODO: Devuelve el rol en mayúsculas (ADMINISTRADOR, TECNICO o CLIENTE)
  obtenerRol(): string | null {
    const sesion = this.obtenerSesion();
    if (!sesion || !sesion.rol) return null;
    return sesion.rol.toUpperCase();
  }
}
