package com.duoc.pagos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.pagos.domain.Pago;

public interface PagoRepository extends JpaRepository<Pago, String> {

    Optional<Pago> findByIdempotencyKey(String idempotencyKey);

    List<Pago> findTop50ByEnviadoFalseOrderByCreadoAsc();

    @Query("select p from Pago p where p.cuentaOrigenId = :cuentaId or p.cuentaDestinoId = :cuentaId order by p.creado desc")
    List<Pago> buscarPorCuenta(@Param("cuentaId") Long cuentaId);

    // Actualizaciones puntuales: no pisan otros campos que cambien en paralelo (p. ej. el resultado)
    @Transactional
    @Modifying
    @Query("update Pago p set p.enviado = true where p.solicitudId = :id")
    int marcarEnviado(@Param("id") String id);

    @Transactional
    @Modifying
    @Query("update Pago p set p.eventosPublicados = true where p.solicitudId = :id")
    int marcarEventosPublicados(@Param("id") String id);
}
