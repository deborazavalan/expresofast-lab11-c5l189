package com.expresofast.dto;

import com.expresofast.model.EstadoEnvio;
import jakarta.validation.constraints.NotNull;

public class ActualizarEstadoDTO {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoEnvio estado;

    public ActualizarEstadoDTO() {
    }

    public EstadoEnvio getEstado() {
        return estado;
    }

    public void setEstado(EstadoEnvio estado) {
        this.estado = estado;
    }
}
