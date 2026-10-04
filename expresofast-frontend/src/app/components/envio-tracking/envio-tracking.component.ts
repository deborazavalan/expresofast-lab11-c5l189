import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EnvioService } from '../../services/envio.service';
import { Envio, EstadoEnvio } from '../../models/envio.model';

const ORDEN_ESTADOS: EstadoEnvio[] = ['PENDIENTE', 'EN_TRANSITO', 'ENTREGADO'];

@Component({
  selector: 'app-envio-tracking',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './envio-tracking.component.html',
  styleUrl: './envio-tracking.component.css'
})
export class EnvioTrackingComponent {
  private envioService = inject(EnvioService);

  codigoRastreo = '';
  envio: Envio | null = null;
  buscando = false;
  mensajeError = '';
  buscado = false;

  buscar(): void {
    if (!this.codigoRastreo.trim()) {
      this.mensajeError = 'Ingrese un código de rastreo para buscar.';
      return;
    }

    this.buscando = true;
    this.buscado = true;
    this.mensajeError = '';
    this.envio = null;

    this.envioService.obtenerPorRastreo(this.codigoRastreo.trim()).subscribe({
      next: (data) => {
        this.envio = data;
        this.buscando = false;
      },
      error: (err) => {
        this.buscando = false;
        this.mensajeError = `No se encontró ningún envío con el código "${this.codigoRastreo}".`;
        console.error(err);
      }
    });
  }

  porcentajeProgreso(): number {
    if (!this.envio) return 0;
    if (this.envio.estado === 'CANCELADO') return 100;
    const index = ORDEN_ESTADOS.indexOf(this.envio.estado);
    if (index === -1) return 0;
    return ((index + 1) / ORDEN_ESTADOS.length) * 100;
  }

  claseEstado(): string {
    if (!this.envio) return 'badge';
    switch (this.envio.estado) {
      case 'PENDIENTE': return 'badge badge-pendiente';
      case 'EN_TRANSITO': return 'badge badge-transito';
      case 'ENTREGADO': return 'badge badge-entregado';
      case 'CANCELADO': return 'badge badge-cancelado';
      default: return 'badge';
    }
  }
}
