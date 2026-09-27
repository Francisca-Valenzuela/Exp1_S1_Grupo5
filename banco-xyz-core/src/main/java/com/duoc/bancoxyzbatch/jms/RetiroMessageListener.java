package com.duoc.bancoxyzbatch.jms;

import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.duoc.bancoxyzbatch.internal.InternalCuentaService;
import com.duoc.bancoxyzbatch.internal.exception.SaldoInsuficienteException;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

@Component
public class RetiroMessageListener {

    private static final Logger log = LoggerFactory.getLogger(RetiroMessageListener.class);

    public static final String COLA_PENDIENTES = "cola.retiros.pendientes";
    public static final String COLA_RESULTADO = "cola.retiros.resultado";

    private final InternalCuentaService cuentaService;
    private final JmsTemplate jmsTemplate;

    public RetiroMessageListener(InternalCuentaService cuentaService, JmsTemplate jmsTemplate) {
        this.cuentaService = cuentaService;
        this.jmsTemplate = jmsTemplate;
    }

    @JmsListener(destination = COLA_PENDIENTES)
    public void procesarRetiro(Message message, Double monto) throws JMSException {
        Long cuentaId = message.getLongProperty("cuentaId");
        String solicitudId = message.getStringProperty("solicitudId");
        log.info("Iniciando procesamiento asíncrono. Solicitud: {}, Cuenta: {}, Monto a retirar: {}",
                solicitudId, cuentaId, monto);

        try {
            cuentaService.retirar(cuentaId, monto);
            publicarResultado(solicitudId, cuentaId, monto, "PROCESADO", "Retiro realizado con éxito");
            log.info("Retiro {} procesado para la cuenta {}", solicitudId, cuentaId);
        } catch (SaldoInsuficienteException | NoSuchElementException | IllegalArgumentException e) {
            // Rechazo de NEGOCIO: no se reintenta, se informa el motivo (RetiroRechazado)
            log.warn("Retiro {} rechazado para la cuenta {}: {}", solicitudId, cuentaId, e.getMessage());
            publicarResultado(solicitudId, cuentaId, monto, "RECHAZADO", e.getMessage());
        }
        // Cualquier OTRA excepción (error técnico) se propaga a propósito: la sesión hace
        // rollback, Artemis reentrega el mensaje y, agotados los intentos, lo envía a la DLQ.
    }

    private void publicarResultado(String solicitudId, Long cuentaId, Double monto,
                                   String estado, String motivo) {
        jmsTemplate.convertAndSend(COLA_RESULTADO, monto, message -> {
            message.setStringProperty("solicitudId", solicitudId);
            message.setLongProperty("cuentaId", cuentaId);
            message.setStringProperty("estado", estado);
            message.setStringProperty("motivo", motivo);
            return message;
        });
    }
}