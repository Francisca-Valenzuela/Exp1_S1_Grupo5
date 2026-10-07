package com.duoc.pagos.domain;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Pago/transferencia/deposito. Se guarda ANTES de publicar (patron Transactional Outbox):
 * si Kafka esta caido, el registro queda con enviado=false y OutboxScheduler lo reenvia.
 * Estados: PENDIENTE -> COMPLETADO | RECHAZADO.
 */
@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    private String solicitudId;

    private String tipo;               // PAGO | TRANSFERENCIA | DEPOSITO
    private Long cuentaOrigenId;
    private Long cuentaDestinoId;

    @Column(precision = 19, scale = 2)
    private BigDecimal monto;

    private String descripcion;
    private String canal;
    private String estado;
    private String motivo;
    private Long clienteId;

    private boolean enviado;              // la solicitud ya fue publicada en Kafka
    private boolean eventosPublicados;    // transacciones.completadas / alertas.seguridad ya publicados

    @Column(unique = true)
    private String idempotencyKey;

    private Instant creado;
    private Instant actualizado;

    @Version
    private Long version;

    public String getSolicitudId() { return solicitudId; }
    public void setSolicitudId(String solicitudId) { this.solicitudId = solicitudId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Long getCuentaOrigenId() { return cuentaOrigenId; }
    public void setCuentaOrigenId(Long cuentaOrigenId) { this.cuentaOrigenId = cuentaOrigenId; }
    public Long getCuentaDestinoId() { return cuentaDestinoId; }
    public void setCuentaDestinoId(Long cuentaDestinoId) { this.cuentaDestinoId = cuentaDestinoId; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public boolean isEnviado() { return enviado; }
    public void setEnviado(boolean enviado) { this.enviado = enviado; }
    public boolean isEventosPublicados() { return eventosPublicados; }
    public void setEventosPublicados(boolean eventosPublicados) { this.eventosPublicados = eventosPublicados; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public Instant getCreado() { return creado; }
    public void setCreado(Instant creado) { this.creado = creado; }
    public Instant getActualizado() { return actualizado; }
    public void setActualizado(Instant actualizado) { this.actualizado = actualizado; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
