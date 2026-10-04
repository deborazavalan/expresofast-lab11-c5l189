import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EnvioService } from '../../services/envio.service';
import { ESTADOS_ENVIO, Envio, EstadoEnvio } from '../../models/envio.model';

@Component({
  selector: 'app-envio-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './envio-list.component.html',
  styleUrl: './envio-list.component.css'
})
export class EnvioListComponent implements OnInit {
  private envioService = inject(EnvioService);

  envios: Envio[] = [];
  estadosDisponibles: EstadoEnvio[] = ESTADOS_ENVIO;
  cargando = false;
  mensajeError = '';
  mensajeExito = '';

  ngOnInit(): void {
    this.cargarEnvios();
  }

  cargarEnvios(): void {
    this.cargando = true;
    this.mensajeError = '';
    this.envioService.obtenerEnvios().subscribe({
      next: (data) => {
        this.envios = data;
        this.cargando = false;
      },
      error: (err) => {
        this.mensajeError = 'No se pudo cargar la lista de envíos. Verifique que el backend esté disponible.';
        this.cargando = false;
        console.error(err);
      }
    });
  }

  onCambiarEstado(envio: Envio, nuevoEstado: EstadoEnvio): void {
    if (nuevoEstado === envio.estado) {
      return;
    }
    this.mensajeExito = '';
    this.mensajeError = '';
    this.envioService.actualizarEstado(envio.id, nuevoEstado).subscribe({
      next: (actualizado) => {
        envio.estado = actualizado.estado;
        this.mensajeExito = `Estado del envío ${envio.codigoRastreo} actualizado a ${nuevoEstado}.`;
      },
      error: (err) => {
        this.mensajeError = `No se pudo actualizar el estado del envío ${envio.codigoRastreo}.`;
        console.error(err);
      }
    });
  }

  claseEstado(estado: EstadoEnvio): string {
    switch (estado) {
      case 'PENDIENTE': return 'badge badge-pendiente';
      case 'EN_TRANSITO': return 'badge badge-transito';
      case 'ENTREGADO': return 'badge badge-entregado';
      case 'CANCELADO': return 'badge badge-cancelado';
      default: return 'badge';
    }
  }
}
