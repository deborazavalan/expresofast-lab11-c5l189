package com.expresofast.service;

import com.expresofast.dto.ActualizarEstadoDTO;
import com.expresofast.dto.EnvioDTO;
import com.expresofast.dto.EnvioRegistroDTO;
import com.expresofast.dto.PaqueteDTO;
import com.expresofast.exception.ResourceNotFoundException;
import com.expresofast.exception.TrackingDuplicadoException;
import com.expresofast.model.Envio;
import com.expresofast.model.EstadoEnvio;
import com.expresofast.model.Paquete;
import com.expresofast.repository.EnvioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnvioServiceImpl implements EnvioService {

    private final EnvioRepository envioRepository;

    public EnvioServiceImpl(EnvioRepository envioRepository) {
        this.envioRepository = envioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnvioDTO> obtenerTodos() {
        return envioRepository.findAllByOrderByFechaCreacionDesc()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EnvioDTO obtenerPorCodigoRastreo(String codigoRastreo) {
        Envio envio = envioRepository.findByCodigoRastreo(codigoRastreo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un envío con el código de rastreo: " + codigoRastreo));
        return convertirADTO(envio);
    }

    @Override
    @Transactional
    public EnvioDTO registrarEnvio(EnvioRegistroDTO dto) {
        String tracking = dto.getNumeroTracking().trim();

        if (!dto.getFechaEntregaEstimada().isAfter(dto.getFechaDespacho())) {
            throw new IllegalArgumentException(
                    "La fecha de entrega estimada debe ser posterior a la fecha de despacho");
        }
        if (envioRepository.existsByCodigoRastreo(tracking)) {
            throw new TrackingDuplicadoException("Este número de rastreo ya está en uso");
        }

        Envio envio = new Envio();
        envio.setCodigoRastreo(tracking);
        envio.setDestinatario(dto.getDestinatario().trim());
        envio.setDireccionDestino(dto.getDireccionDestino().trim());
        envio.setMontoFlete(dto.getMontoFlete());
        envio.setEstado(EstadoEnvio.PENDIENTE);
        envio.setFechaDespacho(dto.getFechaDespacho());
        envio.setFechaEntregaEstimada(dto.getFechaEntregaEstimada());

        BigDecimal pesoTotal = BigDecimal.ZERO;
        for (PaqueteDTO paqueteDTO : dto.getPaquetes()) {
            envio.agregarPaquete(new Paquete(paqueteDTO.getDescripcion().trim(), paqueteDTO.getPesoKg()));
            pesoTotal = pesoTotal.add(paqueteDTO.getPesoKg());
        }
        envio.setPesoKg(pesoTotal);

        Envio guardado = envioRepository.save(envio);
        return convertirADTO(guardado);
    }

    @Override
    @Transactional
    public EnvioDTO actualizarEstado(Long id, ActualizarEstadoDTO dto) {
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un envío con id: " + id));
        envio.setEstado(dto.getEstado());
        Envio actualizado = envioRepository.save(envio);
        return convertirADTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeTracking(String trackingNumber) {
        return envioRepository.existsByCodigoRastreo(trackingNumber.trim());
    }

    private EnvioDTO convertirADTO(Envio envio) {
        List<PaqueteDTO> paquetes = envio.getPaquetes().stream()
                .map(p -> new PaqueteDTO(p.getId(), p.getDescripcion(), p.getPesoKg()))
                .collect(Collectors.toList());

        return new EnvioDTO(
                envio.getId(),
                envio.getCodigoRastreo(),
                envio.getDestinatario(),
                envio.getDireccionDestino(),
                envio.getMontoFlete(),
                envio.getPesoKg(),
                envio.getEstado(),
                envio.getFechaDespacho(),
                envio.getFechaEntregaEstimada(),
                envio.getFechaCreacion(),
                paquetes
        );
    }
}
