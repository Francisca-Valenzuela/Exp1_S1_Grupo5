package com.duoc.bancoxyzbatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.bancoxyzbatch.entity.CuentaInteresEntity;

public interface CuentaInteresRepository extends JpaRepository<CuentaInteresEntity, Long> {

    /**
     * Upsert atomico por cuenta. El archivo legacy trae la misma cuenta muchas veces (1.000 filas, 50 cuentas),
     * por lo que un INSERT simple falla por clave duplicada. Con ON CONFLICT el resultado es idempotente:
     * la cuenta se crea la primera vez y se actualiza las siguientes, sin importar el orden ni el paralelismo.
     */
    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO cuentas_interes (cuenta_id, nombre, saldo_inicial, saldo_final, edad, tipo, version)
            VALUES (:#{#c.cuentaId}, :#{#c.nombre}, :#{#c.saldoInicial}, :#{#c.saldoFinal}, :#{#c.edad}, :#{#c.tipo}, 0)
            ON CONFLICT (cuenta_id) DO UPDATE SET
                nombre = EXCLUDED.nombre,
                saldo_inicial = EXCLUDED.saldo_inicial,
                saldo_final = EXCLUDED.saldo_final,
                edad = EXCLUDED.edad,
                tipo = EXCLUDED.tipo,
                version = cuentas_interes.version + 1
            """, nativeQuery = true)
    int upsertar(@Param("c") CuentaInteresEntity c);
}