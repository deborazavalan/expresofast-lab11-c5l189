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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvioServiceImplTest {

    @Mock
    private EnvioRepository envioRepository;

    @InjectMocks
    private EnvioServiceImpl envioService;

    private Envio envioEjemplo;

    @BeforeEach
    void setUp() {
        envioEjemplo = new Envio("EXP-2026-1001", "Maria Fernandez", "San Jose Centro",
                new BigDecimal("5000.00"), EstadoEnvio.PENDIENTE);
        envioEjemplo.setId(1L);
        envioEjemplo.setPesoKg(new BigDecimal("3.50"));
        envioEjemplo.setFechaCreacion(LocalDateTime.now());
    }

    private EnvioRegistroDTO crearRegistroValido() {
        EnvioRegistroDTO dto = new EnvioRegistroDTO();
        dto.setNumeroTracking("TRK-0001");
        dto.setDestinatario("Carlos Vindas");
        dto.setDireccionDestino("Cartago, Paraiso");
        dto.setMontoFlete(new BigDecimal("4200.00"));
        dto.setFechaDespacho(LocalDate.of(2026, 10, 5));
        dto.setFechaEntregaEstimada(LocalDate.of(2026, 10, 8));
        dto.setPaquetes(List.of(
                new PaqueteDTO(null, "Caja de repuestos", new BigDecimal("2.50")),
                new PaqueteDTO(null, "Sobre documentos", new BigDecimal("0.75"))
        ));
        return dto;
    }

    @Test
    void obtenerTodos_debeRetornarListaDeEnvios() {
        when(envioRepository.findAllByOrderByFechaCreacionDesc()).thenReturn(List.of(envioEjemplo));

        List<EnvioDTO> resultado = envioService.obtenerTodos();

        assertEquals(1, resultado.size());
        assertEquals("EXP-2026-1001", resultado.get(0).getCodigoRastreo());
    }

    @Test
    void obtenerPorCodigoRastreo_debeLanzarExcepcionSiNoExiste() {
        when(envioRepository.findByCodigoRastreo("NO-EXISTE")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> envioService.obtenerPorCodigoRastreo("NO-EXISTE"));
    }

    @Test
    void registrarEnvio_debeGuardarEnvioConSusPaquetesYCalcularPesoTotal() {
        when(envioRepository.existsByCodigoRastreo("TRK-0001")).thenReturn(false);
        when(envioRepository.save(any(Envio.class))).thenAnswer(invocation -> {
            Envio e = invocation.getArgument(0);
            e.setId(2L);
            e.setFechaCreacion(LocalDateTime.now());
            return e;
        });

        EnvioDTO resultado = envioService.registrarEnvio(crearRegistroValido());

        ArgumentCaptor<Envio> captor = ArgumentCaptor.forClass(Envio.class);
        verify(envioRepository, times(1)).save(captor.capture());
        Envio guardado = captor.getValue();

        assertEquals(2, guardado.getPaquetes().size());
        for (Paquete paquete : guardado.getPaquetes()) {
            assertSame(guardado, paquete.getEnvio());
        }
        assertEquals(new BigDecimal("3.25"), guardado.getPesoKg());
        assertEquals("TRK-0001", resultado.getCodigoRastreo());
        assertEquals(EstadoEnvio.PENDIENTE, resultado.getEstado());
        assertEquals(2, resultado.getPaquetes().size());
    }

    @Test
    void registrarEnvio_debeRechazarTrackingDuplicado() {
        when(envioRepository.existsByCodigoRastreo("TRK-0001")).thenReturn(true);

        assertThrows(TrackingDuplicadoException.class,
                () -> envioService.registrarEnvio(crearRegistroValido()));
        verify(envioRepository, never()).save(any(Envio.class));
    }

    @Test
    void registrarEnvio_debeRechazarEntregaNoPosteriorADespacho() {
        EnvioRegistroDTO dto = crearRegistroValido();
        dto.setFechaEntregaEstimada(dto.getFechaDespacho());

        assertThrows(IllegalArgumentException.class, () -> envioService.registrarEnvio(dto));
        verify(envioRepository, never()).save(any(Envio.class));
    }

    @Test
    void existeTracking_debeConsultarElRepositorio() {
        when(envioRepository.existsByCodigoRastreo("EXP-2026-1001")).thenReturn(true);
        when(envioRepository.existsByCodigoRastreo("LIBRE-1")).thenReturn(false);

        assertTrue(envioService.existeTracking("EXP-2026-1001"));
        assertFalse(envioService.existeTracking("LIBRE-1"));
    }

    @Test
    void actualizarEstado_debeActualizarCorrectamente() {
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envioEjemplo));
        when(envioRepository.save(any(Envio.class))).thenReturn(envioEjemplo);

        ActualizarEstadoDTO dto = new ActualizarEstadoDTO();
        dto.setEstado(EstadoEnvio.EN_TRANSITO);

        EnvioDTO resultado = envioService.actualizarEstado(1L, dto);

        assertEquals(EstadoEnvio.EN_TRANSITO, resultado.getEstado());
    }

    @Test
    void actualizarEstado_debeLanzarExcepcionSiIdNoExiste() {
        when(envioRepository.findById(99L)).thenReturn(Optional.empty());

        ActualizarEstadoDTO dto = new ActualizarEstadoDTO();
        dto.setEstado(EstadoEnvio.ENTREGADO);

        assertThrows(ResourceNotFoundException.class,
                () -> envioService.actualizarEstado(99L, dto));
    }
}
