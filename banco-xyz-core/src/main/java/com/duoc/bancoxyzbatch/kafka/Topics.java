package com.duoc.bancoxyzbatch.kafka;

/** Topicos Kafka del ecosistema Banco XYZ (contrato compartido entre microservicios). */
public final class Topics {

    private Topics() {
    }

    /** El core publica una cuenta migrada desde el sistema legacy (productor: banco-xyz-core). */
    public static final String CUENTAS_MIGRADAS = "cuentas.migradas";
}
