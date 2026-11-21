import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login {
  usuario: string = '';
  contrasena: string = '';
  cargando: boolean = false;
  errorLogin: string = '';
  cuentaCreada: boolean = false;
  cuentaBloqueada: boolean = false;

  mostrarOlvido: boolean = false;
  correoRecuperacion: string = '';
  enviandoCorreo: boolean = false;
  correoEnviado: boolean = false;

  mostrarRegistro: boolean = false;
  dni: string = '';
  nombres: string = '';
  apellidos: string = '';
  correo: string = '';
  telefono: string = '';
  fechaNacimiento: string = '';
  contrasenaRegistro: string = '';
  confirmarContrasena: string = '';
  errorRegistro: string = '';
  buscandoDni: boolean = false;
  registrando: boolean = false;

  // ===== Configuración del bloqueo =====
  private readonly MAX_INTENTOS = 3;
  private readonly BLOQUEO_MS = 5_000;         // bloqueo real: 5 s
  private readonly BLOQUEO_VISUAL_MS = 1_000;   // mensaje fuerte: 1 s (luego cuenta atrás)

  private timeoutErrorLogin: any;
  private timeoutErrorRegistro: any;
  private intervalBloqueo: any;
  private timeoutFaseVisual: any;

  /** Usuario bloqueado actualmente (para poder limpiar su estado al expirar) */
  private usuarioBloqueado: string = '';

  private apiUrl = 'http://localhost:8080/api';

  constructor(
    private router: Router,
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  // ==================== HELPERS DE ERROR ====================
  private mostrarErrorLogin(mensaje: string) {
    if (this.timeoutErrorLogin) clearTimeout(this.timeoutErrorLogin);
    this.errorLogin = mensaje;
    this.cdr.detectChanges();
    this.timeoutErrorLogin = setTimeout(() => {
      this.errorLogin = '';
      this.cdr.detectChanges();
    }, 3000);
  }

  private mostrarErrorRegistro(mensaje: string) {
    if (this.timeoutErrorRegistro) clearTimeout(this.timeoutErrorRegistro);
    this.errorRegistro = mensaje;
    this.cdr.detectChanges();
    this.timeoutErrorRegistro = setTimeout(() => {
      this.errorRegistro = '';
      this.cdr.detectChanges();
    }, 3000);
  }

  // ==================== BLOQUEO POR USUARIO ====================

  private claveEstado(usuario: string): string {
    return `coltech_login_${usuario.toLowerCase().trim()}`;
  }

  private obtenerEstado(usuario: string): { intentos: number; bloqueadoHasta: number } {
    if (!usuario) return { intentos: 0, bloqueadoHasta: 0 };
    const raw = localStorage.getItem(this.claveEstado(usuario));
    if (!raw) return { intentos: 0, bloqueadoHasta: 0 };
    try {
      const parsed = JSON.parse(raw);
      return {
        intentos: Number(parsed.intentos) || 0,
        bloqueadoHasta: Number(parsed.bloqueadoHasta) || 0
      };
    } catch {
      return { intentos: 0, bloqueadoHasta: 0 };
    }
  }

  private guardarEstado(usuario: string, intentos: number, bloqueadoHasta: number) {
    if (!usuario) return;
    localStorage.setItem(
      this.claveEstado(usuario),
      JSON.stringify({ intentos, bloqueadoHasta })
    );
  }

  private limpiarEstado(usuario: string) {
    if (!usuario) return;
    localStorage.removeItem(this.claveEstado(usuario));
  }

  private estaBloqueado(usuario: string): boolean {
    if (!usuario) return false;
    const { bloqueadoHasta } = this.obtenerEstado(usuario);
    return bloqueadoHasta > Date.now();
  }

  private segundosRestantes(usuario: string): number {
    if (!usuario) return 0;
    const { bloqueadoHasta } = this.obtenerEstado(usuario);
    const ms = bloqueadoHasta - Date.now();
    return ms > 0 ? Math.ceil(ms / 1000) : 0;
  }

  // ==================== INICIAR SESIÓN ====================
  iniciarSesion() {
    if (!this.usuario || !this.contrasena) {
      this.mostrarErrorLogin('Ingresa usuario y contraseña');
      return;
    }

    if (this.estaBloqueado(this.usuario)) {
      const seg = this.segundosRestantes(this.usuario);
      this.cuentaBloqueada = true;
      this.mostrarErrorLogin(`Cuenta bloqueada. Intenta de nuevo en ${seg}s.`);
      return;
    }

    const estado = this.obtenerEstado(this.usuario);
    if (estado.intentos >= this.MAX_INTENTOS) {
      this.limpiarEstado(this.usuario);
    }

    this.cargando = true;
    this.errorLogin = '';
    this.cuentaBloqueada = false;
    this.cdr.detectChanges();

    this.http.post(`${this.apiUrl}/auth/login`, {
      usuario: this.usuario.trim(),
      contrasena: this.contrasena
    }).subscribe({
      next: (resp: any) => {
        this.limpiarEstado(this.usuario);
        this.guardarSesionYRedirigir(resp);
      },
      error: (err) => {
        this.cargando = false;
        this.cdr.detectChanges();
        this.manejarErrorLogin(err);
      }
    });
  }

  private guardarSesionYRedirigir(resp: any) {
    const usuarioData = {
      idPersona: resp.idPersona || '',
      nombre: resp.nombre || '',
      apellido: resp.apellido || '',
      correo: resp.correo || this.usuario,
      telefono: resp.telefono || '',
      dni: resp.dni || '',
      rol: resp.rol?.toUpperCase() || 'CLIENTE'
    };

    localStorage.setItem('usuario', JSON.stringify(usuarioData));
    localStorage.setItem('idPersona', usuarioData.idPersona.toString());
    if (resp.token) localStorage.setItem('token', resp.token);

    this.cargando = false;
    this.cdr.detectChanges();
    this.redirigirPorRol(resp.rol);
  }

  private manejarErrorLogin(err: any) {
    if (err.status === 423 || err.error?.error?.toLowerCase().includes('bloqueada')) {
      this.activarBloqueoConMensaje(
        err.error?.error || 'Cuenta bloqueada temporalmente',
        this.usuario
      );
      return;
    }

    if (err.status === 401 || err.status === 403) {
      const estado = this.obtenerEstado(this.usuario);
      const nuevosIntentos = estado.intentos + 1;

      if (nuevosIntentos >= this.MAX_INTENTOS) {
        this.guardarEstado(this.usuario, nuevosIntentos, Date.now() + this.BLOQUEO_MS);
        this.activarBloqueoConMensaje(
          `Demasiados intentos fallidos. Espera ${this.BLOQUEO_MS / 1000}s.`,
          this.usuario
        );
      } else {
        this.guardarEstado(this.usuario, nuevosIntentos, 0);
        this.mostrarErrorLogin(
          `Credenciales incorrectas. Intento ${nuevosIntentos} de ${this.MAX_INTENTOS}.`
        );
      }
      return;
    }

    if (err.status === 0) {
      this.mostrarErrorLogin('No se pudo conectar al servidor');
      return;
    }

    this.mostrarErrorLogin(err.error?.error || 'Error al iniciar sesión');
  }

  /**
   * Bloqueo en DOS FASES:
   *   - Fase 1 (0–1 s): mensaje fuerte "Demasiados intentos fallidos. Espera 5s."
   *   - Fase 2 (1–5 s): mensaje persistente "Cuenta bloqueada. Intenta en Xs."
   *   - A los 5 s: desbloquea y limpia el estado → 3 intentos nuevos.
   */
  private activarBloqueoConMensaje(mensaje: string, usuario: string) {
    this.limpiarTimersBloqueo();

    // Guardar el usuario bloqueado ANTES de limpiar el campo
    this.usuarioBloqueado = (usuario || '').toLowerCase().trim();

    // FASE 1: mensaje fuerte
    this.cuentaBloqueada = true;
    this.usuario = '';
    this.contrasena = '';
    this.errorLogin = mensaje;
    this.cdr.detectChanges();

    // A los 1 s: cambiar mensaje y arrancar cuenta atrás
    this.timeoutFaseVisual = setTimeout(() => {
      this.actualizarMensajeBloqueo();
      this.cdr.detectChanges();

      this.intervalBloqueo = setInterval(() => {
        this.actualizarMensajeBloqueo();
        this.cdr.detectChanges();
      }, 1000);
    }, this.BLOQUEO_VISUAL_MS);

    // A los 5 s: desbloquear del todo y limpiar intentos
    setTimeout(() => {
      this.limpiarTimersBloqueo();
      this.cuentaBloqueada = false;
      this.errorLogin = '';
      if (this.usuarioBloqueado) {
        this.limpiarEstado(this.usuarioBloqueado);
        this.usuarioBloqueado = '';
      }
      this.cdr.detectChanges();
    }, this.BLOQUEO_MS);
  }

  private actualizarMensajeBloqueo() {
    if (!this.usuarioBloqueado) return;
    const seg = this.segundosRestantes(this.usuarioBloqueado);
    if (seg <= 0) {
      this.limpiarTimersBloqueo();
      this.cuentaBloqueada = false;
      this.errorLogin = '';
      this.limpiarEstado(this.usuarioBloqueado);
      this.usuarioBloqueado = '';
      return;
    }
    this.errorLogin = `Cuenta bloqueada. Intenta de nuevo en ${seg}s.`;
  }

  private limpiarTimersBloqueo() {
    if (this.intervalBloqueo) {
      clearInterval(this.intervalBloqueo);
      this.intervalBloqueo = null;
    }
    if (this.timeoutFaseVisual) {
      clearTimeout(this.timeoutFaseVisual);
      this.timeoutFaseVisual = null;
    }
  }

  private redirigirPorRol(rol: string) {
    const r = rol?.toUpperCase();
    switch (r) {
      case 'ADMINISTRADOR':
      case 'ADMIN':
        this.router.navigate(['/administrador/dashboard']);
        break;
      case 'TECNICO':
      case 'TÉCNICO':
        this.router.navigate(['/tecnico/ordenes-asignadas']);
        break;
      default:
        this.router.navigate(['/cliente/dashboard']);
        break;
    }
  }

  // ==================== OLVIDO CONTRASEÑA ====================
  abrirOlvido() {
    if (this.timeoutErrorLogin) clearTimeout(this.timeoutErrorLogin);
    if (this.timeoutErrorRegistro) clearTimeout(this.timeoutErrorRegistro);
    this.errorLogin = '';
    this.errorRegistro = '';
    this.mostrarOlvido = true;
    this.correoEnviado = false;
    this.correoRecuperacion = '';
  }

  cerrarOlvido() {
    this.mostrarOlvido = false;
  }

  enviarCorreoRecuperacion() {
    if (!this.correoRecuperacion) return;
    this.enviandoCorreo = true;
    this.cdr.detectChanges();

    this.http.post(`${this.apiUrl}/auth/olvide-contrasena`, {
      correo: this.correoRecuperacion.trim()
    }).subscribe({
      next: () => {
        this.enviandoCorreo = false;
        this.correoEnviado = true;
        this.cdr.detectChanges();
      },
      error: () => {
        this.enviandoCorreo = false;
        this.cdr.detectChanges();
      }
    });
  }

  // ==================== REGISTRO ====================
  abrirRegistro() {
    if (this.timeoutErrorRegistro) clearTimeout(this.timeoutErrorRegistro);
    if (this.timeoutErrorLogin) clearTimeout(this.timeoutErrorLogin);
    this.errorRegistro = '';
    this.errorLogin = '';

    this.mostrarRegistro = true;
    this.limpiarFormularioRegistro();
    this.registrando = false;
    this.cdr.detectChanges();
  }

  private limpiarFormularioRegistro() {
    this.dni = '';
    this.nombres = '';
    this.apellidos = '';
    this.correo = '';
    this.telefono = '';
    this.fechaNacimiento = '';
    this.contrasenaRegistro = '';
    this.confirmarContrasena = '';
  }

  cerrarRegistro() {
    if (this.timeoutErrorRegistro) clearTimeout(this.timeoutErrorRegistro);
    this.mostrarRegistro = false;
    this.errorRegistro = '';
    this.registrando = false;
  }

  onDniChange() {
    this.nombres = '';
    this.apellidos = '';
    this.fechaNacimiento = '';
    if (this.dni.length === 8) {
      this.buscarDniReniec();
    }
  }

  buscarDniReniec() {
    if (!this.dni || this.dni.length !== 8) return;
    this.buscandoDni = true;
    this.cdr.detectChanges();

    this.http.get(`${this.apiUrl}/reniec/dni/${this.dni}`).subscribe({
      next: (resp: any) => {
        const data = resp.data || resp;
        if (data) {
          this.nombres = data.nombres || data.nombre || '';
          const paterno = data.apellidoPaterno || data.apellido_paterno || data.paterno || '';
          const materno = data.apellidoMaterno || data.apellido_materno || data.materno || '';
          this.apellidos = (paterno + ' ' + materno).trim() || data.apellidos || data.apellido || '';
          this.fechaNacimiento = data.fechaNacimiento || data.fecha_nacimiento || '';
        }
        this.buscandoDni = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.buscandoDni = false;
        this.cdr.detectChanges();
      }
    });
  }

  soloNumeros(event: any): boolean {
    const charCode = event.which ? event.which : event.keyCode;
    if (charCode > 31 && (charCode < 48 || charCode > 57)) {
      return false;
    }
    return true;
  }

  onDniInput(event: any) {
    const input = event.target as HTMLInputElement;
    const valor = input.value.replace(/\D/g, '').slice(0, 8);
    this.dni = valor;
    input.value = valor;
    this.onDniChange();
  }

  onTelefonoInput(event: any) {
    const input = event.target as HTMLInputElement;
    const valor = input.value.replace(/\D/g, '').slice(0, 9);
    this.telefono = valor;
    input.value = valor;
  }

  // ==================== REGISTRAR ====================
  private validarFormularioRegistro(): string | null {
    if (!this.dni || this.dni.length !== 8) return 'DNI inválido (8 dígitos)';
    if (!this.nombres.trim()) return 'Ingresa tus nombres';
    if (!this.apellidos.trim()) return 'Ingresa tus apellidos';
    if (!this.fechaNacimiento) return 'Debes ingresar tu fecha de nacimiento';
    if (!this.correo?.includes('@')) return 'Correo inválido';

    if (!this.telefono || this.telefono.length !== 9 || !/^\d{9}$/.test(this.telefono)) {
      return 'Teléfono inválido (debe tener 9 dígitos)';
    }

    if (this.contrasenaRegistro.length < 6) return 'Contraseña muy corta (mín 6)';
    if (this.contrasenaRegistro !== this.confirmarContrasena) return 'Las contraseñas no coinciden';

    return null;
  }

  registrarUsuario() {
    this.errorRegistro = '';

    const errorValidacion = this.validarFormularioRegistro();
    if (errorValidacion) {
      this.mostrarErrorRegistro(errorValidacion);
      return;
    }

    this.registrando = true;
    this.cdr.detectChanges();

    const body = {
      dni: this.dni.trim(),
      nombre: this.nombres.trim(),
      apellido: this.apellidos.trim(),
      telefono: this.telefono.trim(),
      usuario: this.correo.trim().toLowerCase(),
      contrasena: this.contrasenaRegistro,
      fechaNacimiento: this.fechaNacimiento
    };

    this.http.post(`${this.apiUrl}/auth/register`, body).subscribe({
      next: (resp: any) => {
        this.registrando = false;
        this.cerrarRegistro();
        this.cuentaCreada = true;
        this.usuario = this.correo.trim().toLowerCase();
        this.cdr.detectChanges();

        setTimeout(() => {
          this.cuentaCreada = false;
          this.cdr.detectChanges();
        }, 5000);
      },
      error: (err) => {
        this.registrando = false;
        this.manejarErrorRegistro(err);
      }
    });
  }

  private manejarErrorRegistro(err: any) {
    switch (err.status) {
      case 409:
        this.mostrarErrorRegistro(err.error?.error || 'El correo ya está registrado');
        break;
      case 400:
        this.mostrarErrorRegistro(err.error?.error || 'Datos inválidos');
        break;
      case 0:
        this.mostrarErrorRegistro('No se pudo conectar al servidor');
        break;
      default:
        this.mostrarErrorRegistro(err.error?.error || 'Error al crear la cuenta');
        break;
    }
  }
}