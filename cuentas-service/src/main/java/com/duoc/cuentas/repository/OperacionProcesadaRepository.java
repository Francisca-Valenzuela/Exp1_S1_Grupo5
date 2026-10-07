package com.duoc.cuentas.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.duoc.cuentas.domain.OperacionProcesada;

public interface OperacionProcesadaRepository extends JpaRepository<OperacionProcesada, String> {
}
