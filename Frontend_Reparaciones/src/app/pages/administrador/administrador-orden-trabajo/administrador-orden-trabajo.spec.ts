import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClient } from '@angular/common/http';
import { of } from 'rxjs';

import { AdministradorOrdenTrabajo } from './administrador-orden-trabajo';

describe('AdministradorOrdenTrabajo', () => {
  let component: AdministradorOrdenTrabajo;
  let fixture: ComponentFixture<AdministradorOrdenTrabajo>;

  const mockHttpClient = {
    get: vi.fn((_url: string) => of([])),
    put: vi.fn((_url: string, _body: unknown) => of({})),
    delete: vi.fn((_url: string) => of({})),
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [AdministradorOrdenTrabajo],
      providers: [{ provide: HttpClient, useValue: mockHttpClient }],
    }).compileComponents();

    fixture = TestBed.createComponent(AdministradorOrdenTrabajo);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('only exposes technician assignment in the edit modal', () => {
    component.abrirModalEditar({
      id: 'OT-2',
      idOrden: 2,
      cliente: 'Cliente',
      marca: 'Marca',
      modelo: 'Modelo',
      descripcion: 'Problema',
      idTecnico: 3,
      estado: 'EN_REPARACION',
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('#tecnicoAsignado')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('#estadoOrden')).toBeNull();
    expect(fixture.nativeElement.querySelectorAll('.edit-section select')).toHaveLength(1);
  });

  it('saves technician assignment without sending an order state', () => {
    component.ordenSeleccionada = {
      idOrden: 2,
      idTecnico: 4,
      estado: 'EN_REPARACION',
    };

    component.guardarCambios();

    expect(mockHttpClient.put).toHaveBeenCalledWith(
      'http://localhost:8080/api/ordenes-trabajo/2',
      { idTecnico: 4 },
    );
  });
});
