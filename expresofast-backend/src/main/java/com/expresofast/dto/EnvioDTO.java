package com.expresofast.dto;

import com.expresofast.model.EstadoEnvio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EnvioDTO {

    private Long id;
    private String codigoRastreo;
    private String destinatario;
    private String direccionDestino;
    private BigDecimal montoFlete;
    private BigDecimal pesoTotalKg;
    private EstadoEnvio estado;
    private LocalDate fechaDespacho;
    private LocalDate fechaEntregaEstimada;
    private LocalDateTime fechaCreacion;
    private List<PaqueteDTO> paquetes = new ArrayList<>();

    public EnvioDTO() {
    }

    public EnvioDTO(Long id, String codigoRastreo, String destinatario, String direccionDestino,
                    BigDecimal montoFlete, BigDecimal pesoTotalKg, EstadoEnvio estado,
                    LocalDate fechaDespacho, LocalDate fechaEntregaEstimada,
                    LocalDateTime fechaCreacion, List<PaqueteDTO> paquetes) {
        this.id = id;
        this.codigoRastreo = codigoRastreo;
        this.destinatario = destinatario;
        this.direccionDestino = direccionDestino;
        this.montoFlete = montoFlete;
        this.pesoTotalKg = pesoTotalKg;
        this.estado = estado;
        this.fechaDespacho = fechaDespacho;
        this.fechaEntregaEstimada = fechaEntregaEstimada;
        this.fechaCreacion = fechaCreacion;
        this.paquetes = paquetes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoRastreo() {
        return codigoRastreo;
    }

    public void setCodigoRastreo(String codigoRastreo) {
        this.codigoRastreo = codigoRastreo;
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

    public BigDecimal getPesoTotalKg() {
        return pesoTotalKg;
    }

    public void setPesoTotalKg(BigDecimal pesoTotalKg) {
        this.pesoTotalKg = pesoTotalKg;
    }

    public EstadoEnvio getEstado() {
        return estado;
    }

    public void setEstado(EstadoEnvio estado) {
        this.estado = estado;
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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<PaqueteDTO> getPaquetes() {
        return paquetes;
    }

    public void setPaquetes(List<PaqueteDTO> paquetes) {
        this.paquetes = paquetes;
    }
}
