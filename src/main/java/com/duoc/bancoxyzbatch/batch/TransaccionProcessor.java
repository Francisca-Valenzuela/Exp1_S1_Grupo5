package com.duoc.bancoxyzbatch.batch;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.duoc.bancoxyzbatch.entity.TransaccionEntity;
import com.duoc.bancoxyzbatch.model.TransaccionCsv;

@Component
public class TransaccionProcessor implements ItemProcessor<TransaccionCsv, TransaccionEntity> {

    // set concurrente: el step ahora corre en 3 hilos, así que esta colección
    // compartida debe soportar escritura simultánea sin corromperse
    private final Set<String> clavesVistas = ConcurrentHashMap.newKeySet();

    private static final DateTimeFormatter FORMATO_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_LEGACY = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Override
    public TransaccionEntity process(TransaccionCsv item) {
        TransaccionEntity entity = new TransaccionEntity();
        entity.setMonto(item.getMonto());
        String tipo = item.getTipo() != null ? item.getTipo().trim() : null;
        entity.setTipo(tipo);
        entity.setFecha(parseFecha(item.getFecha()));

        StringBuilder anomalias = new StringBuilder();

        if (item.getMonto() == null || item.getMonto() == 0) {
            anomalias.append("monto cero o vacio; ");
        } else if (item.getMonto() < 0) {
            anomalias.append("monto negativo; ");
        }

        if (tipo == null || !(tipo.equalsIgnoreCase("debito") || tipo.equalsIgnoreCase("credito"))) {
            anomalias.append("tipo invalido: '").append(tipo).append("'; ");
        }

        String clave = item.getFecha() + "|" + item.getMonto() + "|" + tipo;
        if (!clavesVistas.add(clave)) {
            anomalias.append("registro duplicado; ");
        }

        entity.setAnomalia(anomalias.isEmpty() ? null : anomalias.toString().trim());
        return entity;
    }

    private LocalDate parseFecha(String fechaRaw) {
        String fecha = fechaRaw != null ? fechaRaw.trim() : null;
        if (fecha == null || fecha.isBlank()) {
            throw new DatoInvalidoException("Fecha nula en registro de transaccion");
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