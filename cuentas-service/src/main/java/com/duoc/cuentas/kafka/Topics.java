package com.duoc.cuentas.kafka;

/** Contrato de topicos Kafka del ecosistema (mismos nombres en todos los microservicios). */
public final class Topics {

    private Topics() {
    }

    public static final String CUENTAS_MIGRADAS = "cuentas.migradas";
    public static final String PAGOS_SOLICITADOS = "pagos.solicitados";
    public static final String PAGOS_RESULTADO = "pagos.resultado";
    public static final String RETIROS_SOLICITADOS = "retiros.solicitados";
    public static final String RETIROS_RESULTADO = "retiros.resultado";
}
