package com.duoc.bancoxyzbatch.bff.dto;

import java.util.List;

/**
 * Vista completa de una cuenta para el frontend Web: incluye datos
 * personales, saldos y el historial completo de movimientos del año.
 * Un frontend de escritorio tiene ancho de banda y pantalla para mostrar
 * todo esto de una vez (tabla de movimientos, gráficos, etc.).
 */
public class CuentaWebDTO {

    private Long cuentaId;
    private String nombre;
    private Integer edad;
    private String tipo;
    private Double saldoInicial;
    private Double saldoFinal;
    private List<MovimientoWebDTO> historialMovimientos;

    public CuentaWebDTO(Long cuentaId, String nombre, Integer edad, String tipo,
                         Double saldoInicial, Double saldoFinal,
                         List<MovimientoWebDTO> historialMovimientos) {
        this.cuentaId = cuentaId;
        this.nombre = nombre;
        this.edad = edad;
        this.tipo = tipo;
        this.saldoInicial = saldoInicial;
        this.saldoFinal = saldoFinal;
        this.historialMovimientos = historialMovimientos;
    }

    public Long getCuentaId() { 
        return cuentaId; 
    }

    public String getNombre() { 
        return nombre; 
    }
    
    public Integer getEdad() { 
        return edad; 
    }
    
    public String getTipo() { 
        return tipo; 
    }
    
    public Double getSaldoInicial() { 
        return saldoInicial; 
    }
    
    public Double getSaldoFinal() { 
        return saldoFinal; 
    }
    
    public List<MovimientoWebDTO> getHistorialMovimientos() { 
        return historialMovimientos; 
    }
}