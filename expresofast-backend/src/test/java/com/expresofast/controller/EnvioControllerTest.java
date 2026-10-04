package com.expresofast.controller;

import com.expresofast.dto.EnvioDTO;
import com.expresofast.dto.EnvioRegistroDTO;
import com.expresofast.dto.PaqueteDTO;
import com.expresofast.exception.TrackingDuplicadoException;
import com.expresofast.model.EstadoEnvio;
import com.expresofast.service.EnvioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnvioController.class)
class EnvioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnvioService envioService;

    private EnvioRegistroDTO crearRegistroValido() {
        EnvioRegistroDTO request = new EnvioRegistroDTO();
        request.setNumeroTracking("TRK-2002");
        request.setDestinatario("Carlos Vindas");
        request.setDireccionDestino("Cartago, Paraiso");
        request.setMontoFlete(new BigDecimal("4200.00"));
        request.setFechaDespacho(LocalDate.of(2026, 10, 5));
        request.setFechaEntregaEstimada(LocalDate.of(2026, 10, 8));
        request.setPaquetes(List.of(new PaqueteDTO(null, "Caja pequeña", new BigDecimal("1.50"))));
        return request;
    }

    private EnvioDTO crearRespuesta() {
        return new EnvioDTO(2L, "TRK-2002", "Carlos Vindas", "Cartago, Paraiso",
                new BigDecimal("4200.00"), new BigDecimal("1.50"), EstadoEnvio.PENDIENTE,
                LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 8), LocalDateTime.now(),
                List.of(new PaqueteDTO(1L, "Caja pequeña", new BigDecimal("1.50"))));
    }

    private String aJson(Object objeto) throws Exception {
        return objectMapper.writeValueAsString(objeto);
    }

    @Test
    void obtenerTodos_debeRetornar200ConListaDeEnvios() throws Exception {
        when(envioService.obtenerTodos()).thenReturn(List.of(crearRespuesta()));

        mockMvc.perform(get("/api/v1/envios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoRastreo").value("TRK-2002"))
                .andExpect(jsonPath("$[0].paquetes.length()").value(1));
    }

    @Test
    void verificarTracking_debeRetornarTrueSiExiste() throws Exception {
        when(envioService.existeTracking("TRK-2002")).thenReturn(true);

        mockMvc.perform(get("/api/envios/check-tracking/TRK-2002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.existe").value(true));
    }

    @Test
    void verificarTracking_debeRetornarFalseSiNoExiste() throws Exception {
        when(envioService.existeTracking("LIBRE-1")).thenReturn(false);

        mockMvc.perform(get("/api/v1/envios/check-tracking/LIBRE-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.existe").value(false));
    }

    @Test
    void registrarEnvio_debeRetornar201ParaPayloadValido() throws Exception {
        when(envioService.registrarEnvio(any(EnvioRegistroDTO.class))).thenReturn(crearRespuesta());

        mockMvc.perform(post("/api/v1/envios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aJson(crearRegistroValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoRastreo").value("TRK-2002"));
    }

    @Test
    void registrarEnvio_debeRetornar400SiNoHayPaquetes() throws Exception {
        EnvioRegistroDTO request = crearRegistroValido();
        request.setPaquetes(new ArrayList<>());

        mockMvc.perform(post("/api/v1/envios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aJson(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarEnvio_debeRetornar400ParaPayloadInvalido() throws Exception {
        EnvioRegistroDTO request = crearRegistroValido();
        request.setDestinatario("");
        request.setDireccionDestino("");
        request.setMontoFlete(new BigDecimal("-10"));
        request.setPaquetes(List.of(new PaqueteDTO(null, "", new BigDecimal("0"))));

        mockMvc.perform(post("/api/v1/envios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aJson(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarEnvio_debeRetornar409SiTrackingDuplicado() throws Exception {
        when(envioService.registrarEnvio(any(EnvioRegistroDTO.class)))
                .thenThrow(new TrackingDuplicadoException("Este número de rastreo ya está en uso"));

        mockMvc.perform(post("/api/v1/envios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aJson(crearRegistroValido())))
                .andExpect(status().isConflict());
    }
}
