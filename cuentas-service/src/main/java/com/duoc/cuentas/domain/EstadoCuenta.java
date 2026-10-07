package com.duoc.cuentas.domain;

public enum EstadoCuenta {
    ABIERTA,
    CERRADA,
    /** Cuenta abierta en modo degradado: clientes-service no estaba disponible para validar al cliente. */
    PENDIENTE_VALIDACION
}
