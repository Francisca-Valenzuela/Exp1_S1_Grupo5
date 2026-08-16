package com.duoc.bancoxyzbatch.batch;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.duoc.bancoxyzbatch.entity.CuentaAnualEntity;
import com.duoc.bancoxyzbatch.model.CuentaAnualCsv;

@Component
public class CuentaAnualProcessor implements ItemProcessor<CuentaAnualCsv, CuentaAnualEntity> {

    private static final DateTimeFormatter FORMATO_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_LEGACY = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Override
    public CuentaAnualEntity process(CuentaAnualCsv item) {
        CuentaAnualEntity entity = new CuentaAnualEntity();
        entity.setCuentaId(item.getCuentaId());
        entity.setFecha(parseFecha(item.getFecha()));
        entity.setTransaccion(item.getTransaccion());
        entity.setMonto(item.getMonto());

        String descripcion = item.getDescripcion();
        if (descripcion == null || descripcion.isBlank()) {
            descripcion = "Sin descripción";
            System.out.println("Anomalía: descripción faltante para cuenta " + item.getCuentaId()
                    + " (fecha=" + item.getFecha() + ")");
        }
        entity.setDescripcion(descripcion);

        if (item.getMonto() == null || item.getMonto() == 0) {
            System.out.println("Anomalía: monto en cero para cuenta " + item.getCuentaId()
                    + " (" + item.getDescripcion() + ")");
        }

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