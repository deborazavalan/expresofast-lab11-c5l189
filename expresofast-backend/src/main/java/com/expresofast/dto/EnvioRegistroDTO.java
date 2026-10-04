package com.expresofast.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EnvioRegistroDTO {

    @NotBlank(message = "El número de tracking es obligatorio")
    @Size(min = 4, max = 30, message = "El tracking debe tener entre 4 y 30 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "El tracking solo admite letras, números y guiones")
    private String numeroTracking;

    @NotBlank(message = "El destinatario es obligatorio")
    @Size(max = 150, message = "El destinatario no puede exceder 150 caracteres")
    private String destinatario;

    @NotBlank(message = "La dirección de destino es obligatoria")
    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String direccionDestino;

    @NotNull(message = "El monto de flete es obligatorio")
    @Positive(message = "El monto de flete debe ser mayor a cero")
    private BigDecimal montoFlete;

    @NotNull(message = "La fecha de despacho es obligatoria")
    private LocalDate fechaDespacho;

    @NotNull(message = "La fecha de entrega estimada es obligatoria")
    private LocalDate fechaEntregaEstimada;

    @NotEmpty(message = "El envío debe contener al menos un paquete")
    @Valid
    private List<PaqueteDTO> paquetes = new ArrayList<>();

    public EnvioRegistroDTO() {
    }

    public String getNumeroTracking() {
        return numeroTracking;
    }

    public void setNumeroTracking(String numeroTracking) {
        this.numeroTracking = numeroTracking;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getDireccionDestino() {
        return direccionDestino;
    }

    public void setDireccionDestino(String direccionDestino) {
        this.direccionDestino = direccionDestino;
    }

    public BigDecimal getMontoFlete() {
        return montoFlete;
    }

    public void setMontoFlete(BigDecimal montoFlete) {
        this.montoFlete = montoFlete;
    }

    public LocalDate getFechaDespacho() {
        return fechaDespacho;
    }

    public void setFechaDespacho(LocalDate fechaDespacho) {
        this.fechaDespacho = fechaDespacho;
    }

    public LocalDate getFechaEntregaEstimada() {
        return fechaEntregaEstimada;
    }

    public void setFechaEntregaEstimada(LocalDate fechaEntregaEstimada) {
        this.fechaEntregaEstimada = fechaEntregaEstimada;
    }

    public List<PaqueteDTO> getPaquetes() {
        return paquetes;
    }

    public void setPaquetes(List<PaqueteDTO> paquetes) {
        this.paquetes = paquetes;
    }
}
