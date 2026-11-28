import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ReniecService {
  private http = inject(HttpClient);

  private apiUrl = 'http://localhost:8080/api/reniec';

  buscarDni(dni: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/dni/${dni}`);
  }
}
