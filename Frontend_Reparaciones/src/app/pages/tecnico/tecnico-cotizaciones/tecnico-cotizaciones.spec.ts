import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';

import { TecnicoCotizaciones } from './tecnico-cotizaciones';

describe('TecnicoCotizaciones', () => {
  let component: TecnicoCotizaciones;
  let fixture: ComponentFixture<TecnicoCotizaciones>;

  beforeEach(async () => {
    localStorage.clear();

    await TestBed.configureTestingModule({
      imports: [TecnicoCotizaciones],
      providers: [provideHttpClient()],
    }).compileComponents();

    fixture = TestBed.createComponent(TecnicoCotizaciones);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('calculates quotation totals from spare parts and labor only', () => {
    component.repuestos = [{ nombre: 'SSD', cantidad: 1, precioUnitario: 100 }];
    component.manoObra = 50;

    expect(component.total).toBe(150);
    expect('trabajos' in component).toBe(false);
    expect('notas' in component).toBe(false);
  });
});
