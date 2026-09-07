package com.duoc.bancoxyzbatch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.duoc.bancoxyzbatch.entity.CuentaAnualEntity;

public interface CuentaAnualRepository extends JpaRepository<CuentaAnualEntity, Long> {
    List<CuentaAnualEntity> findByCuentaId(Long cuentaId);
}