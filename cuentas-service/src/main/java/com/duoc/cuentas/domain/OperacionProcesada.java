package com.duoc.cuentas.domain;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Registro de idempotencia: cada solicitud (solicitudId) se aplica UNA sola vez
 * aunque Kafka la entregue mas de una vez (semantica at-least-once). Garantiza
 * consistencia: nunca se descuenta dos veces el mismo retiro o transferencia.
 */
@Entity
@Table(name = "operaciones_procesadas")
public class OperacionProcesada {

    @Id
    private String solicitudId;
    private String tipo;
    private String estado;   // APLICADO | RECHAZADO
    private String motivo;
    private Long clienteId;
    private Long cuentaOrigenId;
    private Long cuentaDestinoId;
    private BigDecimal monto;
    private BigDecimal saldoOrigen;
    private Instant fecha;

    public OperacionProcesada() {
    }

    public String getSolicitudId() { return solicitudId; }
    public void setSolicitudId(String solicitudId) { this.solicitudId = solicitudId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public Long getCuentaOrigenId() { return cuentaOrigenId; }
    public void setCuentaOrigenId(Long cuentaOrigenId) { this.cuentaOrigenId = cuentaOrigenId; }
    public Long getCuentaDestinoId() { return cuentaDestinoId; }
    public void setCuentaDestinoId(Long cuentaDestinoId) { this.cuentaDestinoId = cuentaDestinoId; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public BigDecimal getSaldoOrigen() { return saldoOrigen; }
    public void setSaldoOrigen(BigDecimal saldoOrigen) { this.saldoOrigen = saldoOrigen; }
    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }
}
