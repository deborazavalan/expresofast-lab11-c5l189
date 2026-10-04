import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import {
  FormArray,
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { Router } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { EnvioRegistroPayload } from '../../models/envio.model';
import { EnvioService } from '../../services/envio.service';
import { fechasCruzadasValidator } from '../../validators/fechas.validator';
import { trackingUnicoValidator } from '../../validators/tracking-unico.validator';

interface PaqueteForm {
  descripcion: FormControl<string>;
  pesoKg: FormControl<number | null>;
}

interface EnvioAvanzadoForm {
  numeroTracking: FormControl<string>;
  destinatario: FormControl<string>;
  direccionDestino: FormControl<string>;
  montoFlete: FormControl<number | null>;
  fechaDespacho: FormControl<string>;
  fechaEntregaEstimada: FormControl<string>;
  paquetes: FormArray<FormGroup<PaqueteForm>>;
}

@Component({
  selector: 'app-envio-avanzado-form',
  standalone: true,
  imports: [ReactiveFormsModule, DecimalPipe],
  templateUrl: './envio-avanzado-form.component.html',
  styleUrl: './envio-avanzado-form.component.css'
})
export class EnvioAvanzadoFormComponent {
  private fb = inject(NonNullableFormBuilder);
  private envioService = inject(EnvioService);
  private router = inject(Router);

  enviando = false;
  mensajeExito = '';
  mensajeError = '';

  readonly form: FormGroup<EnvioAvanzadoForm> = this.fb.group<EnvioAvanzadoForm>(
    {
      numeroTracking: this.fb.control('', {
        validators: [
          Validators.required,
          Validators.minLength(4),
          Validators.maxLength(30),
          Validators.pattern(/^[A-Za-z0-9-]+$/)
        ],
        asyncValidators: [trackingUnicoValidator(this.envioService)]
      }),
      destinatario: this.fb.control('', [Validators.required, Validators.maxLength(150)]),
      direccionDestino: this.fb.control('', [Validators.required, Validators.maxLength(200)]),
      montoFlete: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.01)]),
      fechaDespacho: this.fb.control('', Validators.required),
      fechaEntregaEstimada: this.fb.control('', Validators.required),
      paquetes: this.fb.array<FormGroup<PaqueteForm>>([this.crearPaquete()])
    },
    { validators: [fechasCruzadasValidator] }
  );

  get paquetes(): FormArray<FormGroup<PaqueteForm>> {
    return this.form.controls.paquetes;
  }

  get pesoTotal(): number {
    return this.paquetes.controls.reduce(
      (total, paquete) => total + (paquete.controls.pesoKg.value ?? 0),
      0
    );
  }

  get puedeEliminar(): boolean {
    return this.paquetes.length > 1;
  }

  private crearPaquete(): FormGroup<PaqueteForm> {
    return this.fb.group<PaqueteForm>({
      descripcion: this.fb.control('', [Validators.required, Validators.maxLength(255)]),
      pesoKg: this.fb.control<number | null>(null, [
        Validators.required,
        Validators.min(0.01),
        Validators.max(999.99)
      ])
    });
  }

  agregarPaquete(): void {
    this.paquetes.push(this.crearPaquete());
  }

  eliminarPaquete(indice: number): void {
    if (!this.puedeEliminar) {
      return;
    }
    this.paquetes.removeAt(indice);
  }

  mostrarError(control: FormControl<string> | FormControl<number | null>, error: string): boolean {
    return control.hasError(error) && (control.touched || control.dirty);
  }

  onSubmit(): void {
    if (this.form.invalid || this.form.pending) {
      this.form.markAllAsTouched();
      return;
    }

    const valor = this.form.getRawValue();
    const payload: EnvioRegistroPayload = {
      numeroTracking: valor.numeroTracking.trim(),
      destinatario: valor.destinatario.trim(),
      direccionDestino: valor.direccionDestino.trim(),
      montoFlete: Number(valor.montoFlete),
      fechaDespacho: valor.fechaDespacho,
      fechaEntregaEstimada: valor.fechaEntregaEstimada,
      paquetes: valor.paquetes.map((paquete) => ({
        descripcion: paquete.descripcion.trim(),
        pesoKg: Number(paquete.pesoKg)
      }))
    };

    this.enviando = true;
    this.mensajeError = '';
    this.mensajeExito = '';

    this.envioService.crearEnvio(payload).subscribe({
      next: (envio) => {
        this.enviando = false;
        this.mensajeExito = `Envío ${envio.codigoRastreo} registrado con ${envio.paquetes.length} paquete(s).`;
        this.reiniciarFormulario();
        setTimeout(() => this.router.navigate(['/envios']), 1500);
      },
      error: (err: HttpErrorResponse) => {
        this.enviando = false;
        if (err.status === 409) {
          this.form.controls.numeroTracking.setErrors({ trackingTomado: true });
          this.form.controls.numeroTracking.markAsTouched();
          this.mensajeError = 'Este número de rastreo ya está en uso.';
        } else {
          this.mensajeError = 'Ocurrió un error al registrar el envío. Intente nuevamente.';
        }
      }
    });
  }

  private reiniciarFormulario(): void {
    this.paquetes.clear();
    this.paquetes.push(this.crearPaquete());
    this.form.reset();
  }
}
