import { AbstractControl, AsyncValidatorFn, ValidationErrors } from '@angular/forms';
import { Observable, catchError, map, of, switchMap, timer } from 'rxjs';
import { EnvioService } from '../services/envio.service';

const DEBOUNCE_MS = 400;

export function trackingUnicoValidator(envioService: EnvioService): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    const valor = String(control.value ?? '').trim();

    if (!valor) {
      return of(null);
    }

    return timer(DEBOUNCE_MS).pipe(
      switchMap(() => envioService.verificarTracking(valor)),
      map((respuesta) => (respuesta.existe ? { trackingTomado: true } : null)),
      catchError(() => of(null))
    );
  };
}
