package com.duoc.bancoxyzbatch.batch;

import java.time.LocalDate;
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
        return FechaParser.parse(fechaRaw, "transaccion");
    }
}
