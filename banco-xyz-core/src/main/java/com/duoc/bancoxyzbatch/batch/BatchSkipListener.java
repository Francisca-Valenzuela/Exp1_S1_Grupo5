package com.duoc.bancoxyzbatch.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

@Component
public class BatchSkipListener implements SkipListener<Object, Object> {

    private static final Logger log = LoggerFactory.getLogger(BatchSkipListener.class);

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("SKIP en lectura -> {}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(Object item, Throwable t) {
        log.warn("SKIP en procesamiento del item {} -> {}", item, t.getMessage());
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        log.warn("SKIP en escritura del item {} -> {}", item, t.getMessage());
    }
}