package com.expresofast.controller;

import com.expresofast.dto.ActualizarEstadoDTO;
import com.expresofast.dto.EnvioDTO;
import com.expresofast.dto.EnvioRegistroDTO;
import com.expresofast.dto.TrackingExisteDTO;
import com.expresofast.service.EnvioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/envios", "/api/envios"})
@CrossOrigin(origins = "http://localhost:4200")
public class EnvioController {

    private final EnvioService envioService;

    public EnvioController(EnvioService envioService) {
        this.envioService = envioService;
    }

    @GetMapping
    public ResponseEntity<List<EnvioDTO>> obtenerTodos() {
        return ResponseEntity.ok(envioService.obtenerTodos());
    }

    @GetMapping("/rastreo/{codigo}")
    public ResponseEntity<EnvioDTO> obtenerPorRastreo(@PathVariable String codigo) {
        return ResponseEntity.ok(envioService.obtenerPorCodigoRastreo(codigo));
    }

    @GetMapping("/check-tracking/{trackingNumber}")
    public ResponseEntity<TrackingExisteDTO> verificarTracking(@PathVariable String trackingNumber) {
        boolean existe = envioService.existeTracking(trackingNumber);
        return ResponseEntity.ok(new TrackingExisteDTO(trackingNumber, existe));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnvioDTO registrarEnvio(@Valid @RequestBody EnvioRegistroDTO dto) {
        return envioService.registrarEnvio(dto);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EnvioDTO> actualizarEstado(@PathVariable Long id,
                                                       @Valid @RequestBody ActualizarEstadoDTO dto) {
        return ResponseEntity.ok(envioService.actualizarEstado(id, dto));
    }
}
