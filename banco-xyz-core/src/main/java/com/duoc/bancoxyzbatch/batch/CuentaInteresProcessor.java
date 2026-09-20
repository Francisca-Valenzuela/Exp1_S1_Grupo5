package com.duoc.bancoxyzbatch.batch;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.duoc.bancoxyzbatch.entity.CuentaInteresEntity;
import com.duoc.bancoxyzbatch.model.CuentaInteresCsv;

@Component
public class CuentaInteresProcessor implements ItemProcessor<CuentaInteresCsv, CuentaInteresEntity> {

    private static final Logger log = LoggerFactory.getLogger(CuentaInteresProcessor.class);

    private static final double TASA_AHORRO = 0.02;
    private static final double TASA_PRESTAMO = 0.05;
    private static final double TASA_HIPOTECA = 0.04;

    private static final int EDAD_MINIMA = 18;
    private static final int EDAD_MAXIMA = 100;

    // set concurrente: el step corre en 3 hilos en paralelo
    private final Set<String> clavesVistas = ConcurrentHashMap.newKeySet();

    @Override
    public CuentaInteresEntity process(CuentaInteresCsv item) {
        CuentaInteresEntity entity = new CuentaInteresEntity();
        entity.setCuentaId(item.getCuentaId());
        entity.setNombre(item.getNombre());
        entity.setEdad(item.getEdad());

        String tipo = item.getTipo() != null ? item.getTipo().trim().toLowerCase() : "";
        entity.setTipo(tipo);

        double saldoInicial = item.getSaldo() != null ? item.getSaldo() : 0.0;
        entity.setSaldoInicial(saldoInicial);

        double tasa = switch (tipo) {
            case "ahorro" -> TASA_AHORRO;
            case "prestamo" -> TASA_PRESTAMO;
            case "hipoteca" -> TASA_HIPOTECA;
            default -> throw new DatoInvalidoException(
                    "Tipo de cuenta no reconocido '" + tipo + "' para cuenta " + item.getCuentaId());
        };
        entity.setSaldoFinal(saldoInicial + (saldoInicial * tasa));

        if (item.getEdad() == null || item.getEdad() < EDAD_MINIMA || item.getEdad() > EDAD_MAXIMA) {
            log.warn("Anomalia: edad fuera de rango para cuenta {} (edad={})", item.getCuentaId(), item.getEdad());
        }

        String clave = item.getNombre() + "|" + item.getSaldo() + "|" + item.getEdad() + "|" + tipo;
        if (!clavesVistas.add(clave)) {
            log.warn("Anomalia: posible cuenta duplicada -> {} (cuenta {})", item.getNombre(), item.getCuentaId());
        }

        return entity;
    }
}
