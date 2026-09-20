package com.duoc.bancoxyzbatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.duoc.bancoxyzbatch.entity.TransaccionEntity;

public interface TransaccionRepository extends JpaRepository<TransaccionEntity, Long> {}