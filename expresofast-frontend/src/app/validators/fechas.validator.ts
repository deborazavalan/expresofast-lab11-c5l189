import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const fechasCruzadasValidator: ValidatorFn = (
  group: AbstractControl
): ValidationErrors | null => {
  const despacho = group.get('fechaDespacho')?.value as string | null | undefined;
  const entrega = group.get('fechaEntregaEstimada')?.value as string | null | undefined;

  if (!despacho || !entrega) {
    return null;
  }

  const fechaDespacho = new Date(despacho).getTime();
  const fechaEntrega = new Date(entrega).getTime();

  if (Number.isNaN(fechaDespacho) || Number.isNaN(fechaEntrega)) {
    return null;
  }

  return fechaEntrega > fechaDespacho ? null : { fechasInvalidas: true };
};
