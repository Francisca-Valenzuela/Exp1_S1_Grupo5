package com.duoc.pagos.kafka;

public final class Topics {
    private Topics() {
    }

    public static final String PAGOS_SOLICITADOS = "pagos.solicitados";
    public static final String PAGOS_RESULTADO = "pagos.resultado";
    public static final String TRANSACCIONES_COMPLETADAS = "transacciones.completadas";
    public static final String ALERTAS_SEGURIDAD = "alertas.seguridad";
}
