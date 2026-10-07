package com.duoc.clientes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.clientes.domain.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** Carga inicial idempotente desde el batch (no pisa clientes existentes). */
    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO clientes (id, nombre, edad, perfil, estado, alertas_seguridad,
                                  operaciones_completadas, fecha_registro, origen, version)
            VALUES (:id, :nombre, :edad, 'BASICO', 'ACTIVO', 0, 0, now(), 'BATCH', 0)
            ON CONFLICT (id) DO NOTHING
            """, nativeQuery = true)
    int insertarMigradoSiNoExiste(@Param("id") Long id,
                                  @Param("nombre") String nombre,
                                  @Param("edad") Integer edad);

    /** Incremento atomico en BD: seguro ante consumidores concurrentes (sin conflictos de version). */
    @Transactional
    @Modifying
    @Query(value = "UPDATE clientes SET alertas_seguridad = alertas_seguridad + 1 WHERE id = :id",
           nativeQuery = true)
    int registrarAlerta(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query(value = """
            UPDATE clientes SET operaciones_completadas = operaciones_completadas + 1,
                                ultima_actividad = now()
            WHERE id = :id
            """, nativeQuery = true)
    int registrarOperacion(@Param("id") Long id);
}
