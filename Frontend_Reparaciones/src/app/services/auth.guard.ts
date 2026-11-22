import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './authservice'; // Asegúrate que el nombre coincida exactamente

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // 1. Obtenemos el rol actual desde el servicio
  const rolActual = authService.obtenerRol(); 

  // 2. Si no hay rol, no hay sesión -> al login
  if (!rolActual) {
    router.navigate(['/login']);
    return false;
  }

  // 3. Obtenemos los roles permitidos para esta ruta
  const rolesPermitidos = route.data['roles'] as Array<string>;

  // 4. Verificamos si el rol actual está permitido
  if (rolesPermitidos && rolesPermitidos.includes(rolActual)) {
    return true; // ✅ Tiene permiso
  }

  // 5. Si no tiene permiso, lo mandamos a su propio panel
  redirigirAlPanel(rolActual, router);
  return false;
};

// Función auxiliar para redirigir
function redirigirAlPanel(rol: string, router: Router) {
  switch (rol) {
    case 'ADMINISTRADOR':
    case 'ADMIN':
      router.navigate(['/administrador/dashboard']);
      break;
    case 'TECNICO':
    case 'TÉCNICO':
      router.navigate(['/tecnico/ordenes-asignadas']);
      break;
    default: // CLIENTE
      router.navigate(['/cliente/dashboard']);
      break;
  }
}