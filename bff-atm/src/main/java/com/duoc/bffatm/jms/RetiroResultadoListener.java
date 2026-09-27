package com.duoc.bffatm.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.duoc.bffatm.dto.RetiroEstadoDTO;
import com.duoc.bffatm.store.RetiroEstadoStore;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

@Component
public class RetiroResultadoListener {

    private static final Logger log = LoggerFactory.getLogger(RetiroResultadoListener.class);
    public static final String COLA_RESULTADO = "cola.retiros.resultado";

    private final RetiroEstadoStore store;

    public RetiroResultadoListener(RetiroEstadoStore store) {
        this.store = store;
    }

    @JmsListener(destination = COLA_RESULTADO)
    public void recibirResultado(Message message, Double monto) throws JMSException {
        String solicitudId = message.getStringProperty("solicitudId");
        Long cuentaId = message.getLongProperty("cuentaId");
        String estado = message.getStringProperty("estado");
        String motivo = message.getStringProperty("motivo");

        store.guardar(new RetiroEstadoDTO(solicitudId, cuentaId, monto, estado, motivo));
        log.info("Resultado recibido. Solicitud: {}, estado: {}", solicitudId, estado);
    }
}