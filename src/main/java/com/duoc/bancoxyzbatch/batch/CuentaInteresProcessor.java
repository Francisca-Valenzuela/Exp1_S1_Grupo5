package com.duoc.bancoxyzbatch.batch;

import java.util.HashSet;
import java.util.Set;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.duoc.bancoxyzbatch.entity.CuentaInteresEntity;
import com.duoc.bancoxyzbatch.model.CuentaInteresCsv;

@Component
public class CuentaInteresProcessor implements ItemProcessor<CuentaInteresCsv, CuentaInteresEntity> {

    // tasas de interés mensual de ejemplo según tipo de cuenta
    private static final double TASA_AHORRO = 0.02;
    private static final double TASA_PRESTAMO = 0.05;
    private static final double TASA_HIPOTECA = 0.04;

    private static final int EDAD_MINIMA = 18;
    private static final int EDAD_MAXIMA = 100;

    // detecta cuentas con datos idénticos (nombre+saldo+edad+tipo repetidos)
    private final Set<String> clavesVistas = new HashSet<>();

    @Override
    public CuentaInteresEntity process(CuentaInteresCsv item) {
        CuentaInteresEntity entity = new CuentaInteresEntity();
        entity.setCuentaId(item.getCuentaId());
        entity.setNombre(item.getNombre());
        entity.setEdad(item.getEdad());
        entity.setTipo(item.getTipo());

        // saldo vacío/nulo -> se trata como 0 para no romper el cálculo
        double saldoInicial = item.getSaldo() != null ? item.getSaldo() : 0.0;
        entity.setSaldoInicial(saldoInicial);

        double tasa = switch (item.getTipo() != null ? item.getTipo().toLowerCase() : "") {
            case "ahorro" -> TASA_AHORRO;
            case "prestamo" -> TASA_PRESTAMO;
            case "hipoteca" -> TASA_HIPOTECA;
            default -> 0.0;
        };
        entity.setSaldoFinal(saldoInicial + (saldoInicial * tasa));

        // -- validaciones de calidad de datos (quedan registradas en logs de consola) --
        if (item.getEdad() == null || item.getEdad() < EDAD_MINIMA || item.getEdad() > EDAD_MAXIMA) {
            System.out.println("Anomalía: edad fuera de rango para cuenta " + item.getCuentaId()
                    + " (edad=" + item.getEdad() + ")");
        }

        String clave = item.getNombre() + "|" + item.getSaldo() + "|" + item.getEdad() + "|" + item.getTipo();
        if (!clavesVistas.add(clave)) {
            System.out.println("Anomalía: posible cuenta duplicada -> " + item.getNombre()
                    + " (cuenta " + item.getCuentaId() + ")");
        }

        return entity;
    }
}