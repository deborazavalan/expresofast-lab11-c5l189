package com.expresofast.repository;

import com.expresofast.model.Envio;
import com.expresofast.model.EstadoEnvio;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnvioRepository extends JpaRepository<Envio, Long> {

    @EntityGraph(attributePaths = "paquetes")
    Optional<Envio> findByCodigoRastreo(String codigoRastreo);

    boolean existsByCodigoRastreo(String codigoRastreo);

    @Query("SELECT e FROM Envio e WHERE e.estado = :estado ORDER BY e.fechaCreacion DESC")
    List<Envio> buscarPorEstado(@Param("estado") EstadoEnvio estado);

    @EntityGraph(attributePaths = "paquetes")
    List<Envio> findAllByOrderByFechaCreacionDesc();
}
