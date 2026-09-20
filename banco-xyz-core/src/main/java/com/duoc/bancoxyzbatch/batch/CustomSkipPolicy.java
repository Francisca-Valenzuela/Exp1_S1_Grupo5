package com.duoc.bancoxyzbatch.batch;

import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CustomSkipPolicy implements SkipPolicy {

    // configurable: el CSV oficial (Semana 3, ~1000 filas) trae bastantes mas
    // filas con datos invalidos a proposito que el CSV de prueba de las Semanas 1-2,
    // y al particionar cada worker step tiene su propio contador de omisiones.
    private final int limiteOmisiones;

    public CustomSkipPolicy(@Value("${batch.skip-limit:300}") int limiteOmisiones) {
        this.limiteOmisiones = limiteOmisiones;
    }

    @Override
    public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {
        // solo se omiten errores esperables de calidad de datos (fila corrupta o inválida);
        // cualquier otro tipo de error (ej. de infraestructura) detiene el step, no se omite a ciegas
        boolean esErrorDeDatos = t instanceof DatoInvalidoException || t instanceof FlatFileParseException;

        if (!esErrorDeDatos) {
            return false;
        }
        if (skipCount >= limiteOmisiones) {
            throw new SkipLimitExceededException(limiteOmisiones, t);
        }
        return true;
    }
}