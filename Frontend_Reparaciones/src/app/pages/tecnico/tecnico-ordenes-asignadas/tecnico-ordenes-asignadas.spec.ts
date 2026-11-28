import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClient } from '@angular/common/http';
import { of } from 'rxjs';

import { TecnicoOrdenesAsignadas } from './tecnico-ordenes-asignadas';

describe('TecnicoOrdenesAsignadas', () => {
  let component: TecnicoOrdenesAsignadas;
  let fixture: ComponentFixture<TecnicoOrdenesAsignadas>;

  const ordenReasignada = {
    idOrden: 5,
    idTecnico: 8,
    estado: 'ASIGNADO',
  };
  const mockHttpClient = {
    get: vi.fn((_url: string) => of([ordenReasignada])),
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [TecnicoOrdenesAsignadas],
      providers: [{ provide: HttpClient, useValue: mockHttpClient }],
    }).compileComponents();

    fixture = TestBed.createComponent(TecnicoOrdenesAsignadas);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows a reassigned order only to its current technician', () => {
    component.idTecnico = 3;
    component.cargarOrdenes();
    expect(component.ordenes).toEqual([]);

    component.idTecnico = 8;
    component.cargarOrdenes();
    expect(component.ordenes).toEqual([ordenReasignada]);
  });
});
