package com.duoc.cuentas.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.cuentas.domain.Cuenta;

import jakarta.persistence.LockModeType;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    List<Cuenta> findByClienteId(Long clienteId);

    /** Bloqueo pesimista (SELECT ... FOR UPDATE): serializa debitos/creditos concurrentes sobre la misma cuenta. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cuenta c where c.id = :id")
    Optional<Cuenta> buscarParaActualizar(@Param("id") Long id);

    /**
     * Carga inicial idempotente desde el batch: inserta solo si la cuenta no existe, de modo que
     * reprocesar la migracion NUNCA pisa saldos operativos ya modificados.
     */
    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO cuentas (id, cliente_id, tipo, saldo, estado, fecha_apertura, origen, version)
            VALUES (:id, :clienteId, :tipo, :saldo, 'ABIERTA', now(), 'BATCH', 0)
            ON CONFLICT (id) DO NOTHING
            """, nativeQuery = true)
    int insertarMigradaSiNoExiste(@Param("id") Long id,
                                  @Param("clienteId") Long clienteId,
                                  @Param("tipo") String tipo,
                                  @Param("saldo") BigDecimal saldo);
}
