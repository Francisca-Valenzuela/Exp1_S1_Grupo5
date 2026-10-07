package com.duoc.clientes.kafka;

public final class Topics {
    private Topics() {
    }

    public static final String CUENTAS_MIGRADAS = "cuentas.migradas";
    public static final String TRANSACCIONES_COMPLETADAS = "transacciones.completadas";
    public static final String ALERTAS_SEGURIDAD = "alertas.seguridad";
}
