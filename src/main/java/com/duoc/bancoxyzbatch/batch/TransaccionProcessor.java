package com.duoc.bancoxyzbatch.batch;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.duoc.bancoxyzbatch.entity.TransaccionEntity;
import com.duoc.bancoxyzbatch.model.TransaccionCsv;

@Component
public class TransaccionProcessor implements ItemProcessor<TransaccionCsv, TransaccionEntity> {

    // guarda claves ya vistas para detectar duplicados dentro del mismo archivo
    private final Set<String> clavesVistas = new HashSet<>();

    private static final DateTimeFormatter FORMATO_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_LEGACY = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Override
    public TransaccionEntity process(TransaccionCsv item) {
        TransaccionEntity entity = new TransaccionEntity();
        entity.setMonto(item.getMonto());
        entity.setTipo(item.getTipo());
        entity.setFecha(parseFecha(item.getFecha()));

        StringBuilder anomalias = new StringBuilder();

        if (item.getMonto() == null || item.getMonto() == 0) {
            anomalias.append("monto cero; ");
        } else if (item.getMonto() < 0) {
            anomalias.append("monto negativo; ");
        }

        String clave = item.getFecha() + "|" + item.getMonto() + "|" + item.getTipo();
        if (!clavesVistas.add(clave)) {
            anomalias.append("registro duplicado; ");
        }

        entity.setAnomalia(anomalias.isEmpty() ? null : anomalias.toString().trim());
        return entity;
    }

    private LocalDate parseFecha(String fecha) {
        if (fecha == null) {
            throw new DatoInvalidoException("Fecha nula en registro de transacción");
        }
        try {
            return LocalDate.parse(fecha, FORMATO_ISO);
        } catch (Exception e1) {
            try {
                return LocalDate.parse(fecha, FORMATO_LEGACY);
            } catch (Exception e2) {
                throw new DatoInvalidoException("Fecha con formato irreconocible: " + fecha, e2);
            }
        }
    }
}