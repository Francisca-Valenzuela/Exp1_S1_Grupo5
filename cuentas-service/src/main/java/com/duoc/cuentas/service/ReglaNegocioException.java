package com.duoc.cuentas.service;

/** Violacion de una regla de negocio (HTTP 422). No se reintenta: reintentar no cambiaria el resultado. */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
