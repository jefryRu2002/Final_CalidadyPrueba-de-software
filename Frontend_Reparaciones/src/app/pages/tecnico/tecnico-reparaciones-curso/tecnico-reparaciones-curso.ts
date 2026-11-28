import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { forkJoin } from 'rxjs';

interface OrdenTrabajoApi {
  idOrden?: number;
  id_orden?: number;
  idTecnico?: number;
  id_tecnico?: number;
  estado?: string;
  fechaInicio?: string;
  fecha_inicio?: string;
  marca?: string;
  modelo?: string;
  nombreCliente?: string;
  descripcion?: string;
  descripcionFalla?: string;
  trabajosRealizados?: string[];
  notasTecnico?: string;
  puedePagar?: boolean;
}

interface CotizacionRepuestoApi {
  nombre: string;
  cantidad: number;
  precioUnitario: number | string;
}

interface CotizacionApi {
  idOrden?: number;
  id_orden?: number;
  estado?: string;
  repuestos?: CotizacionRepuestoApi[];
}

export interface Repuesto {
  nombre: string;
  cantidad: number;
  precio: number;
}

export interface Reparacion {
  id: number;
  ticket: string;
  equipo: string;
  cliente: string;
  estado: string;
  fechaIngreso: string;
  trabajosRealizados: string[];
  repuestos: Repuesto[];
  notas: string;
  puedePagar?: boolean;
}

@Component({
  selector: 'app-reparaciones',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './tecnico-reparaciones-curso.html',
  styleUrls: ['./tecnico-reparaciones-curso.css'],
})
export class TecnicoReparacionesCurso implements OnInit {
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);

  reparaciones: Reparacion[] = [];
  reparacionesFiltradas: Reparacion[] = [];
  ordenSeleccionada: Reparacion | null = null;
  cargando = true;
  error = '';
  exito = '';
  procesando = false;
  filtroActivo = 'todas';

  nuevoTrabajo = '';

  private apiUrl = 'http://localhost:8080/api';
  idTecnico = 0;

  ngOnInit(): void {
    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    this.idTecnico = Number(usuario.idPersona) || 0;
    if (this.idTecnico > 0) {
      this.cargarReparaciones();
    } else {
      this.cargando = false;
      this.error = '❌ No se pudo identificar al técnico';
      this.cdr.detectChanges();
    }
  }

  cargarReparaciones(): void {
    this.cargando = true;
    this.error = '';
    this.cdr.detectChanges();

    forkJoin({
      ordenes: this.http.get<OrdenTrabajoApi[]>(`${this.apiUrl}/ordenes-trabajo`),
      cotizaciones: this.http.get<CotizacionApi[]>(`${this.apiUrl}/cotizaciones`),
    }).subscribe({
      next: ({ ordenes, cotizaciones }) => {
        this.reparaciones = ordenes
          .filter((ot) => {
            const idTecnicoOrden = Number(ot.idTecnico || ot.id_tecnico || 0);
            return idTecnicoOrden === this.idTecnico && ot.estado === 'EN_REPARACION';
          })
          .map((ot) => {
            const idOrden = Number(ot.idOrden || ot.id_orden || 0);
            const cotizacionAprobada = cotizaciones.find(
              (cotizacion) =>
                Number(cotizacion.idOrden || cotizacion.id_orden || 0) === idOrden &&
                cotizacion.estado === 'APROBADA_CLIENTE',
            );

            return {
              id: idOrden,
              ticket: 'OT-' + idOrden,
              equipo: `${ot.marca || ''} ${ot.modelo || ''}`.trim() || 'Sin equipo',
              cliente: ot.nombreCliente || 'Sin cliente',
              estado: ot.estado || '',
              fechaIngreso: ot.fechaInicio || ot.fecha_inicio || '',
              trabajosRealizados: ot.trabajosRealizados || [],
              repuestos: (cotizacionAprobada?.repuestos || []).map((repuesto) => ({
                nombre: repuesto.nombre,
                cantidad: Number(repuesto.cantidad),
                precio: Number(repuesto.precioUnitario),
              })),
              notas: ot.notasTecnico || '',
              puedePagar: ot.puedePagar || false,
            };
          });

        this.reparacionesFiltradas = [...this.reparaciones];
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = '❌ Error al cargar reparaciones y cotizaciones';
        this.cargando = false;
        this.cdr.detectChanges();
      },
    });
  }

  filtrarPorEstado(filtro: string): void {
    this.filtroActivo = filtro;
    this.reparacionesFiltradas = [...this.reparaciones];
    this.cdr.detectChanges();
  }

  seleccionarOrden(orden: any): void {
    this.ordenSeleccionada = orden;
    this.nuevoTrabajo = '';
    this.error = '';
    this.exito = '';
    this.cdr.detectChanges();
  }

  cerrarModal(): void {
    this.ordenSeleccionada = null;
    this.error = '';
    this.cdr.detectChanges();
  }

  // ==================== TRABAJOS ====================
  agregarTrabajo(): void {
    if (!this.ordenSeleccionada || !this.nuevoTrabajo.trim()) return;
    if (!this.ordenSeleccionada.trabajosRealizados) {
      this.ordenSeleccionada.trabajosRealizados = [];
    }
    this.ordenSeleccionada.trabajosRealizados.push(this.nuevoTrabajo.trim());
    this.nuevoTrabajo = '';
    this.cdr.detectChanges();
  }

  quitarTrabajo(index: number): void {
    if (!this.ordenSeleccionada) return;
    this.ordenSeleccionada.trabajosRealizados.splice(index, 1);
    this.cdr.detectChanges();
  }

  calcularTotalRepuestos(): number {
    if (!this.ordenSeleccionada?.repuestos) return 0;
    return this.ordenSeleccionada.repuestos.reduce((t, r) => t + r.cantidad * r.precio, 0);
  }

  // ==================== GUARDAR (solo guarda, NO cambia estado) ====================
  guardar(): void {
    if (!this.ordenSeleccionada) return;

    this.procesando = true;
    this.error = '';
    this.exito = '';
    this.cdr.detectChanges();

    const datos = {
      trabajos: (this.ordenSeleccionada.trabajosRealizados || []).map((t) => ({
        descripcion: t,
      })),
      notas: this.ordenSeleccionada.notas || '',
    };

    this.http
      .put(`${this.apiUrl}/ordenes-trabajo/${this.ordenSeleccionada.id}/reparacion`, datos)
      .subscribe({
        next: () => {
          this.exito = '✅ Reparación guardada correctamente';
          this.procesando = false;
          this.cdr.detectChanges();
          setTimeout(() => {
            this.exito = '';
            this.cdr.detectChanges();
          }, 3000);
        },
        error: (err) => {
          console.error('❌ Error:', err);
          this.error = err.error?.error || '❌ Error al guardar';
          this.procesando = false;
          this.cdr.detectChanges();
        },
      });
  }

  // ==================== AVANZAR A PRUEBAS (guarda + cambia estado) ====================
  avanzarAPruebas(): void {
    if (!this.ordenSeleccionada) return;

    if (!confirm('¿Avanzar a la fase de pruebas? Asegúrate de haber guardado los cambios.')) {
      return;
    }

    this.procesando = true;
    this.error = '';
    this.exito = '';
    this.cdr.detectChanges();

    // Primero guardar
    const datos = {
      trabajos: (this.ordenSeleccionada.trabajosRealizados || []).map((t) => ({
        descripcion: t,
      })),
      notas: this.ordenSeleccionada.notas || '',
    };

    this.http
      .put(`${this.apiUrl}/ordenes-trabajo/${this.ordenSeleccionada.id}/reparacion`, datos)
      .subscribe({
        next: () => {
          // Después cambiar de estado
          this.http
            .put(`${this.apiUrl}/ordenes-trabajo/${this.ordenSeleccionada!.id}/avanzar-pruebas`, {})
            .subscribe({
              next: () => {
                this.exito = '✅ Orden pasada a pruebas';
                this.procesando = false;
                this.cdr.detectChanges();
                setTimeout(() => {
                  this.exito = '';
                  this.ordenSeleccionada = null;
                  this.cargarReparaciones();
                  this.cdr.detectChanges();
                }, 2000);
              },
              error: (err) => {
                console.error('❌ Error:', err);
                this.error = '❌ Error al avanzar a pruebas';
                this.procesando = false;
                this.cdr.detectChanges();
              },
            });
        },
        error: (err) => {
          console.error('❌ Error:', err);
          this.error = '❌ Error al guardar antes de avanzar';
          this.procesando = false;
          this.cdr.detectChanges();
        },
      });
  }

  formatearMonto(monto: number): string {
    return new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' }).format(
      monto || 0,
    );
  }

  recargarDatos(): void {
    this.ordenSeleccionada = null;
    this.error = '';
    this.exito = '';
    this.cargarReparaciones();
  }
}
