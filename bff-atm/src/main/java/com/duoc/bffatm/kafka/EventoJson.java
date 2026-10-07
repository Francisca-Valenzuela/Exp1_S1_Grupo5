package com.duoc.bffatm.kafka;

import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

/** Unico punto que depende de Jackson para (de)serializar los eventos Kafka (JSON en texto plano). */
@Component
public class EventoJson {

    private final ObjectMapper mapper;

    public EventoJson(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String toJson(Object evento) {
        return mapper.writeValueAsString(evento);
    }

    public <T> T fromJson(String json, Class<T> tipo) {
        return mapper.readValue(json, tipo);
    }
}
