import { ChangeDetectorRef, Component, NgZone, OnInit, inject } from '@angular/core';

import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-cliente-perfil',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './cliente.html',
  styleUrls: ['./cliente.css'],
})
export class Cliente implements OnInit {
  private ngZone = inject(NgZone);
  private changeDetector = inject(ChangeDetectorRef);

  perfil = {
    idPersona: '',
    nombre: '',
    apellido: '',
    dni: '',
    telefono: '',
    correo: '',
  };

  editando = false;
  guardado = false;
  error = '';
  cargando = true;
  guardando = false;
  nombreUsuario = '';
  iniciales = '';

  mostrarModalMFA = false;
  secretoMFA = '';
  codigoVerificacion = '';
  mfaActivado = false;

  private apiUrl = 'http://localhost:8080/api';

  ngOnInit(): void {
    this.cargarPerfil();
  }

  cargarPerfil(): void {
    this.cargarDesdeLocalStorage();
    this.cargando = false;

    const idPersona = this.obtenerIdPersona();

    if (idPersona) {
      fetch(`${this.apiUrl}/clientes/${idPersona}`)
        .then((res) => res.json())
        .then((cliente) =>
          this.ngZone.run(() => {
            this.error = '';
            this.perfil.dni = cliente.persona?.dni || cliente.dni || this.perfil.dni;
            this.perfil.nombre = cliente.persona?.nombre || cliente.nombre || this.perfil.nombre;
            this.perfil.apellido =
              cliente.persona?.apellido || cliente.apellido || this.perfil.apellido;
            this.perfil.telefono =
              cliente.persona?.telefono || cliente.telefono || this.perfil.telefono;
            this.perfil.correo = cliente.persona?.correo || cliente.correo || this.perfil.correo;
            this.perfil.idPersona = cliente.idPersona || idPersona;
            this.actualizarVista();
            this.guardarEnLocalStorage();
            this.changeDetector.markForCheck();
          }),
        )
        .catch(() =>
          this.ngZone.run(() => {
            this.error = 'No se pudo cargar el perfil desde el servidor';
            this.changeDetector.markForCheck();
          }),
        );
    }
  }

  private cargarDesdeLocalStorage(): void {
    const usuario = localStorage.getItem('usuario');
    const clienteActual = localStorage.getItem('clienteActual');

    if (clienteActual) {
      try {
        const data = JSON.parse(clienteActual);
        this.perfil.dni = data.dni || this.perfil.dni;
      } catch {
        this.error = 'No se pudieron leer los datos del perfil almacenados.';
        localStorage.removeItem('clienteActual');
      }
    }

    if (usuario) {
      try {
        const data = JSON.parse(usuario);
        this.perfil.nombre = data.persona?.nombre || data.nombre || '';
        this.perfil.apellido = data.persona?.apellido || data.apellido || '';
        this.perfil.dni = data.persona?.dni || data.dni || this.perfil.dni;
        this.perfil.telefono = data.persona?.telefono || data.telefono || '';
        this.perfil.correo = data.persona?.correo || data.correo || data.email || '';
        this.perfil.idPersona = data.idPersona || data.id_persona || data.id || '';
        this.actualizarVista();
      } catch {
        this.error = 'No se pudieron leer los datos del perfil almacenados.';
        localStorage.removeItem('usuario');
      }
    }
  }

  private obtenerIdPersona(): string {
    const id = localStorage.getItem('clienteId');
    if (id) return id;
    try {
      const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
      return usuario.idPersona || usuario.id || '';
    } catch {
      this.error = 'No se pudieron leer los datos del perfil almacenados.';
      return '';
    }
  }

  guardar(): void {
    this.error = '';

    if (!/^\d{9}$/.test(this.perfil.telefono)) {
      this.error = 'El teléfono debe tener 9 dígitos';
      return;
    }

    this.guardando = true;

    const datos = {
      persona: {
        telefono: this.perfil.telefono.trim(),
      },
    };

    fetch(`${this.apiUrl}/clientes/${this.perfil.idPersona}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(datos),
    })
      .then((res) => {
        if (!res.ok) {
          throw new Error('No se pudo actualizar el teléfono');
        }

        this.ngZone.run(() => {
          this.guardarEnLocalStorage();
          this.guardando = false;
          this.editando = false;
          this.guardado = true;
          this.changeDetector.markForCheck();
          setTimeout(
            () =>
              this.ngZone.run(() => {
                this.guardado = false;
                this.changeDetector.markForCheck();
              }),
            3000,
          );
        });
      })
      .catch((error: unknown) => {
        this.ngZone.run(() => {
          this.guardando = false;
          this.error = error instanceof Error ? error.message : 'No se pudo actualizar el teléfono';
          this.changeDetector.markForCheck();
        });
      });
  }

  private actualizarVista(): void {
    this.nombreUsuario = `${this.perfil.nombre} ${this.perfil.apellido}`.trim();
    this.iniciales =
      (
        (this.perfil.nombre?.charAt(0) || '') + (this.perfil.apellido?.charAt(0) || '')
      ).toUpperCase() || 'CL';
  }

  private guardarEnLocalStorage(): void {
    localStorage.setItem(
      'clienteActual',
      JSON.stringify({
        idPersona: this.perfil.idPersona,
        nombre: this.nombreUsuario,
        dni: this.perfil.dni,
        telefono: this.perfil.telefono,
        correo: this.perfil.correo,
      }),
    );
  }

  habilitarEdicion(): void {
    this.editando = true;
    this.guardado = false;
    this.error = '';
  }

  cancelar(): void {
    this.editando = false;
    this.error = '';
    this.cargarPerfil();
  }

  validarTelefono(): void {
    this.perfil.telefono = this.perfil.telefono.replace(/[^0-9]/g, '').substring(0, 9);
  }

  validarCodigoVerificacion(): void {
    this.codigoVerificacion = this.codigoVerificacion.replace(/[^0-9]/g, '').substring(0, 6);
  }

  iniciarConfiguracionMFA(): void {
    this.secretoMFA = this.generarSecretoAleatorio();
    this.mostrarModalMFA = true;
  }

  confirmarConfiguracionMFA(): void {
    if (this.codigoVerificacion.length !== 6) {
      this.error = 'Ingresa un código de 6 dígitos';
      return;
    }
    this.mfaActivado = true;
    this.mostrarModalMFA = false;
    this.codigoVerificacion = '';
  }

  desactivarMFA(): void {
    if (confirm('¿Desactivar autenticación?')) this.mfaActivado = false;
  }

  cerrarModalMFA(): void {
    this.mostrarModalMFA = false;
    this.codigoVerificacion = '';
    this.error = '';
  }

  copiarSecreto(): void {
    navigator.clipboard
      .writeText(this.secretoMFA)
      .then(() => alert('✅ Copiada'))
      .catch(() => alert('Clave: ' + this.secretoMFA));
  }

  private generarSecretoAleatorio(): string {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ234567';
    return Array.from({ length: 16 }, () => chars[Math.floor(Math.random() * chars.length)]).join(
      '',
    );
  }
}
