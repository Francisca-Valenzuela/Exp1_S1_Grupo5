package com.duoc.bffatm.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.duoc.bffatm.dto.RetiroEstadoDTO;

/**
 * Estado de cada solicitud de retiro, en memoria. bff-atm no tiene base de
 * datos propia; alcanza para esta actividad porque el estado solo sirve
 * mientras el cajero consulta el resultado de SU retiro. Se pierde si el
 * servicio se reinicia.
 */
@Component
public class RetiroEstadoStore {

    private final Map<String, RetiroEstadoDTO> estados = new ConcurrentHashMap<>();

    public void guardar(RetiroEstadoDTO estado) {
        estados.put(estado.getSolicitudId(), estado);
    }

    public RetiroEstadoDTO buscar(String solicitudId) {
        return estados.get(solicitudId);
    }
}