package com.duoc.bffweb.client;

import java.util.List;

/**
 * Espejo minimo de los campos que Jackson serializa para un
 * org.springframework.data.domain.Page&lt;T&gt; devuelto por banco-xyz-core.
 * Se usa solo para deserializar la respuesta del cliente HTTP.
 */
public class CorePageResponse<T> {

    private List<T> content;
    private long totalElements;

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
}
