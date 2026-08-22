package com.duoc.bancoxyzbatch.batch;

import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

@Component
public class CustomSkipPolicy implements SkipPolicy {

    private static final int LIMITE_OMISIONES = 10;

    @Override
    public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {
        // solo se omiten errores esperables de calidad de datos (fila corrupta o inválida);
        // cualquier otro tipo de error (ej. de infraestructura) detiene el step, no se omite a ciegas
        boolean esErrorDeDatos = t instanceof DatoInvalidoException || t instanceof FlatFileParseException;

        if (!esErrorDeDatos) {
            return false;
        }
        if (skipCount >= LIMITE_OMISIONES) {
            throw new SkipLimitExceededException(LIMITE_OMISIONES, t);
        }
        return true;
    }
}