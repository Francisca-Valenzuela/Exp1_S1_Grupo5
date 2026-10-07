package com.duoc.bancoxyzbatch.batch;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.duoc.bancoxyzbatch.entity.CuentaAnualEntity;
import com.duoc.bancoxyzbatch.model.CuentaAnualCsv;

@Component
public class CuentaAnualProcessor implements ItemProcessor<CuentaAnualCsv, CuentaAnualEntity> {

    private static final Logger log = LoggerFactory.getLogger(CuentaAnualProcessor.class);


    @Override
    public CuentaAnualEntity process(CuentaAnualCsv item) {
        CuentaAnualEntity entity = new CuentaAnualEntity();
        entity.setCuentaId(item.getCuentaId());
        entity.setFecha(parseFecha(item.getFecha()));
        entity.setTransaccion(item.getTransaccion());
        entity.setMonto(item.getMonto());

        String descripcion = item.getDescripcion() != null ? item.getDescripcion().trim() : null;
        if (descripcion == null || descripcion.isBlank()) {
            descripcion = "Sin descripcion";
            log.warn("Anomalia: descripcion faltante para cuenta {} (fecha={})", item.getCuentaId(), item.getFecha());
        }
        entity.setDescripcion(descripcion);

        if (item.getMonto() == null || item.getMonto() == 0) {
            throw new DatoInvalidoException(
                    "Monto inválido (nulo o cero) para cuenta " + item.getCuentaId() + " (" + descripcion + ")");
        }

        return entity;
    }

    private LocalDate parseFecha(String fechaRaw) {
        return FechaParser.parse(fechaRaw, "cuenta anual");
    }
}
