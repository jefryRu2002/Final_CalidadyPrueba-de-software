import { Routes } from '@angular/router';
import { authGuard } from './services/auth.guard'; // Ajusta la ruta si es necesario

import { Login } from './pages/login/login/login';

export const routes: Routes = [
    { path: '', component: Login },
    { path: 'login', component: Login },

    {
        path: 'administrador',
        loadComponent: () => import('./layout/admin-layout/admin-layout').then(m => m.AdminLayout),
        canActivate: [authGuard],
        data: { roles: ['ADMINISTRADOR', 'ADMIN'] },
        children: [
            { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
            { path: 'dashboard', loadComponent: () => import('./pages/administrador/administrador-dashboard/administrador-dashboard').then(m => m.AdministradorDashboard) },
            { path: 'tickets', loadComponent: () => import('./pages/administrador/administrador-tickets/administrador-tickets').then(m => m.AdministradorTickets) },
            { path: 'orden-trabajo', loadComponent: () => import('./pages/administrador/administrador-orden-trabajo/administrador-orden-trabajo').then(m => m.AdministradorOrdenTrabajo) },
            { path: 'usuarios', loadComponent: () => import('./pages/administrador/administrador-usuarios/administrador-usuarios').then(m => m.AdministradorUsuarios) },
            { path: 'notificaciones', loadComponent: () => import('./pages/administrador/administrador-notificaciones/administrador-notificaciones').then(m => m.AdministradorNotificaciones) },
            { path: 'gestion-pagos', loadComponent: () => import('./pages/administrador/administrador-gestion-pagos/administrador-gestion-pagos').then(m => m.AdministradorGestionPagos) },
            { path: 'cotizaciones', loadComponent: () => import('./pages/administrador/administrador-cotizaciones/administrador-cotizaciones').then(m => m.AdministradorCotizaciones) },
            { path: 'devoluciones', loadComponent: () => import('./pages/administrador/administrador-devoluciones/administrador-devoluciones').then(m => m.AdministradorDevoluciones) },
        ]
    },

    {
        path: 'tecnico',
        loadComponent: () => import('./layout/tecnico-layout/tecnico-layout').then(m => m.TecnicoLayout),
        canActivate: [authGuard],
        data: { roles: ['TECNICO', 'TÉCNICO'] },
        children: [
            { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
            { path: 'dashboard', loadComponent: () => import('./pages/tecnico/tecnico-dashboard/tecnico-dashboard').then(m => m.TecnicoDashboard) },
            { path: 'ordenes-asignadas', loadComponent: () => import('./pages/tecnico/tecnico-ordenes-asignadas/tecnico-ordenes-asignadas').then(m => m.TecnicoOrdenesAsignadas) },
            { path: 'evaluacion-equipos', loadComponent: () => import('./pages/tecnico/tecnico-evaluacion-equipos/tecnico-evaluacion-equipos').then(m => m.TecnicoEvaluacionEquipos) },
            { path: 'cotizaciones', loadComponent: () => import('./pages/tecnico/tecnico-cotizaciones/tecnico-cotizaciones').then(m => m.TecnicoCotizaciones) },
            { path: 'reparaciones', loadComponent: () => import('./pages/tecnico/tecnico-reparaciones-curso/tecnico-reparaciones-curso').then(m => m.TecnicoReparacionesCurso) },
            { path: 'pruebas-funcionamiento', loadComponent: () => import('./pages/tecnico/tecnico-pruebas-funcionamiento/tecnico-pruebas-funcionamiento').then(m => m.TecnicoPruebasFuncionamiento) }
        ]
    },

    {
        path: 'cliente',
        loadComponent: () => import('./layout/cliente-layout/cliente-layout').then(m => m.ClienteLayout),
        canActivate: [authGuard],
        data: { roles: ['CLIENTE'] },
        children: [
            { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
            { path: 'dashboard', loadComponent: () => import('./pages/cliente/cliente-dashboard/cliente-dashboard').then(m => m.ClienteDashboard) },
            { path: 'generar-solicitud', loadComponent: () => import('./pages/cliente/cliente-generar-solicitud/cliente-generar-solicitud').then(m => m.ClienteGenerarSolicitud) },
            { path: 'estado-solicitud', loadComponent: () => import('./pages/cliente/cliente-estado-solicitud/cliente-estado-solicitud').then(m => m.ClienteEstadoSolicitud) },
            { path: 'cotizacion', loadComponent: () => import('./pages/cliente/cliente-cotizacion/cliente-cotizacion').then(m => m.ClienteCotizacion) },
            { path: 'pago', loadComponent: () => import('./pages/cliente/cliente-pago/cliente-pago').then(m => m.ClientePago) },
            { path: 'notificacion', loadComponent: () => import('./pages/cliente/cliente-notificacion/cliente-notificacion').then(m => m.ClienteNotificacion) },
            { path: 'perfil', loadComponent: () => import('./pages/cliente/cliente/cliente').then(m => m.Cliente) }
        ]
    },

    { path: '**', redirectTo: '' }
];