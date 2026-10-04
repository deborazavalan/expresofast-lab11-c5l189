export type EstadoEnvio = 'PENDIENTE' | 'EN_TRANSITO' | 'ENTREGADO' | 'CANCELADO';

export interface Paquete {
  id: number;
  descripcion: string;
  pesoKg: number;
}

export interface Envio {
  id: number;
  codigoRastreo: string;
  destinatario: string;
  direccionDestino: string;
  montoFlete: number;
  pesoTotalKg: number;
  estado: EstadoEnvio;
  fechaDespacho: string | null;
  fechaEntregaEstimada: string | null;
  fechaCreacion: string;
  paquetes: Paquete[];
}

export interface PaqueteRegistroPayload {
  descripcion: string;
  pesoKg: number;
}

export interface EnvioRegistroPayload {
  numeroTracking: string;
  destinatario: string;
  direccionDestino: string;
  montoFlete: number;
  fechaDespacho: string;
  fechaEntregaEstimada: string;
  paquetes: PaqueteRegistroPayload[];
}

export interface TrackingExisteResponse {
  trackingNumber: string;
  existe: boolean;
}

export interface ActualizarEstadoPayload {
  estado: EstadoEnvio;
}

export const ESTADOS_ENVIO: EstadoEnvio[] = [
  'PENDIENTE',
  'EN_TRANSITO',
  'ENTREGADO',
  'CANCELADO'
];
