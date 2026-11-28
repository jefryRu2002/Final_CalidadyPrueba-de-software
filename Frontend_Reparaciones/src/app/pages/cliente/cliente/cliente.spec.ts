import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';

import { Cliente } from './cliente';

describe('Cliente', () => {
  let component: Cliente;
  let fixture: ComponentFixture<Cliente>;

  beforeEach(async () => {
    localStorage.clear();

    await TestBed.configureTestingModule({
      imports: [Cliente],
    }).compileComponents();

    fixture = TestBed.createComponent(Cliente);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('keeps email read-only while allowing only the phone to be edited', async () => {
    fixture.detectChanges();
    component.cargando = false;
    fixture.detectChanges();

    const editButton = fixture.nativeElement.querySelector('.btn-editar-perfil') as HTMLButtonElement;
    editButton.click();
    await fixture.whenStable();
    fixture.detectChanges();

    const phone = fixture.nativeElement.querySelector('#perfilTelefono') as HTMLInputElement;
    const email = fixture.nativeElement.querySelector('#perfilCorreo') as HTMLInputElement;

    expect(component.editando).toBe(true);
    expect(phone.disabled).toBe(false);
    expect(email.disabled).toBe(true);
  });

  it('saves only the phone and shows success after the server responds', async () => {
    fixture.detectChanges();
    component.perfil.idPersona = '3';
    component.perfil.telefono = '900000001';
    component.editando = true;
    fixture.detectChanges();

    const fetchSpy = vi.spyOn(globalThis, 'fetch')
      .mockResolvedValue(new Response(null, { status: 200 }));

    component.guardar();
    await Promise.resolve();
    fixture.detectChanges();

    expect(fetchSpy).toHaveBeenCalledWith(
      'http://localhost:8080/api/clientes/3',
      expect.objectContaining({
        method: 'PUT',
        body: JSON.stringify({ persona: { telefono: '900000001' } })
      })
    );
    expect(component.editando).toBe(false);
    expect(component.guardado).toBe(true);

    fetchSpy.mockRestore();
  });
});
