import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { TecnicoLayout } from './tecnico-layout';

describe('TecnicoLayout', () => {
  let component: TecnicoLayout;
  let fixture: ComponentFixture<TecnicoLayout>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TecnicoLayout],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(TecnicoLayout);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
