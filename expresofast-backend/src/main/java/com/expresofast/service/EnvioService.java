package com.expresofast.service;

import com.expresofast.dto.ActualizarEstadoDTO;
import com.expresofast.dto.EnvioDTO;
import com.expresofast.dto.EnvioRegistroDTO;

import java.util.List;

public interface EnvioService {

    List<EnvioDTO> obtenerTodos();

    EnvioDTO obtenerPorCodigoRastreo(String codigoRastreo);

    EnvioDTO registrarEnvio(EnvioRegistroDTO dto);

    EnvioDTO actualizarEstado(Long id, ActualizarEstadoDTO dto);

    boolean existeTracking(String trackingNumber);
}
