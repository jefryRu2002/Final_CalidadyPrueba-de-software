import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { TecnicoReparacionesCurso } from './tecnico-reparaciones-curso';

describe('TecnicoReparacionesCurso', () => {
  let component: TecnicoReparacionesCurso;
  let fixture: ComponentFixture<TecnicoReparacionesCurso>;
  let httpTestingController: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();

    await TestBed.configureTestingModule({
      imports: [TecnicoReparacionesCurso],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpTestingController = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(TecnicoReparacionesCurso);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads spare parts from the customer-approved quotation as read-only data', () => {
    component.idTecnico = 7;
    component.cargarReparaciones();

    const ordersRequest = httpTestingController.expectOne(
      'http://localhost:8080/api/ordenes-trabajo',
    );
    const quotesRequest = httpTestingController.expectOne('http://localhost:8080/api/cotizaciones');

    ordersRequest.flush([
      {
        idOrden: 4,
        idTecnico: 7,
        estado: 'EN_REPARACION',
        marca: 'Dell',
        modelo: 'Latitude',
        repuestosUsados: [{ nombre: 'Repuesto anterior', cantidad: 1, precio: 1 }],
      },
    ]);
    quotesRequest.flush([
      {
        idCotizacion: 10,
        idOrden: 4,
        estado: 'APROBADA_CLIENTE',
        repuestos: [{ nombre: 'Pantalla LCD', cantidad: 1, precioUnitario: 250 }],
      },
      {
        idCotizacion: 11,
        idOrden: 4,
        estado: 'RECHAZADA_CLIENTE',
        repuestos: [{ nombre: 'Batería', cantidad: 1, precioUnitario: 80 }],
      },
    ]);

    expect(component.reparaciones[0].repuestos).toEqual([
      { nombre: 'Pantalla LCD', cantidad: 1, precio: 250 },
    ]);
  });
});
