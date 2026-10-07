package com.duoc.bancoxyzbatch.batch;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Los archivos del sistema legacy mezclan 4 formatos de fecha
 * (yyyy-MM-dd, yyyy/MM/dd, dd-MM-yyyy, dd/MM/yyyy). Para que el resultado sea
 * equivalente al legacy (sin descartar filas validas) se aceptan todos; el
 * formato con dia primero sigue la convencion del legacy (p. ej. 24/03/2024).
 */
public final class FechaParser {

    private static final List<DateTimeFormatter> FORMATOS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"));

    private FechaParser() {
    }

    public static LocalDate parse(String fechaRaw, String contexto) {
        String fecha = fechaRaw != null ? fechaRaw.trim() : null;
        if (fecha == null || fecha.isBlank()) {
            throw new DatoInvalidoException("Fecha nula en registro de " + contexto);
        }
        for (DateTimeFormatter formato : FORMATOS) {
            try {
                return LocalDate.parse(fecha, formato);
            } catch (Exception ignorado) {
                // se prueba el siguiente formato
            }
        }
        throw new DatoInvalidoException("Fecha con formato irreconocible: " + fecha);
    }
}
