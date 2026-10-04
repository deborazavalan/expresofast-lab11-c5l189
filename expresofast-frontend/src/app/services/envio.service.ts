import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ActualizarEstadoPayload,
  Envio,
  EnvioRegistroPayload,
  EstadoEnvio,
  TrackingExisteResponse
} from '../models/envio.model';

@Injectable({
  providedIn: 'root'
})
export class EnvioService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}envios`;

  obtenerEnvios(): Observable<Envio[]> {
    return this.http.get<Envio[]>(this.baseUrl);
  }

  obtenerPorRastreo(codigo: string): Observable<Envio> {
    return this.http.get<Envio>(`${this.baseUrl}/rastreo/${encodeURIComponent(codigo)}`);
  }

  crearEnvio(payload: EnvioRegistroPayload): Observable<Envio> {
    return this.http.post<Envio>(this.baseUrl, payload);
  }

  verificarTracking(trackingNumber: string): Observable<TrackingExisteResponse> {
    return this.http.get<TrackingExisteResponse>(
      `${this.baseUrl}/check-tracking/${encodeURIComponent(trackingNumber)}`
    );
  }

  actualizarEstado(id: number, nuevoEstado: EstadoEnvio): Observable<Envio> {
    const body: ActualizarEstadoPayload = { estado: nuevoEstado };
    return this.http.patch<Envio>(`${this.baseUrl}/${id}/estado`, body);
  }
}
