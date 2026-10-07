package com.duoc.bffweb.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.duoc.bffweb.client.ClienteServiceDTO;
import com.duoc.bffweb.client.ClientesClient;
import com.duoc.bffweb.client.CoreClient;
import com.duoc.bffweb.client.CuentaCoreDTO;
import com.duoc.bffweb.client.CuentaServiceDTO;
import com.duoc.bffweb.client.CuentasClient;
import com.duoc.bffweb.client.MovimientoCoreDTO;
import com.duoc.bffweb.dto.CuentaWebDTO;
import com.duoc.bffweb.dto.MovimientoWebDTO;
import com.duoc.bffweb.dto.TransaccionWebDTO;

/**
 * Composicion de datos para el canal Web: agrega 3 servicios (cuentas, clientes, core).
 * cuentas-service es esencial (si cae => 503); clientes y core son opcionales (si caen => datosParciales=true).
 * Cada llamada esta protegida con Resilience4j dentro de su cliente.
 */
@Service
public class WebBffService {

    private final CuentasClient cuentasClient;
    private final ClientesClient clientesClient;
    private final CoreClient coreClient;

    public WebBffService(CuentasClient cuentasClient, ClientesClient clientesClient, CoreClient coreClient) {
        this.cuentasClient = cuentasClient;
        this.clientesClient = clientesClient;
        this.coreClient = coreClient;
    }

    public CuentaWebDTO obtenerCuenta(Long cuentaId) {
        CuentaServiceDTO cuenta = cuentasClient.obtener(cuentaId);   // esencial
        ClienteServiceDTO cliente = cuenta.getClienteId() == null ? null : clientesClient.obtener(cuenta.getClienteId());
        CuentaCoreDTO legacy = coreClient.obtenerCuenta(cuentaId);

        CuentaWebDTO dto = new CuentaWebDTO();
        dto.setCuentaId(cuenta.getCuentaId());
        dto.setClienteId(cuenta.getClienteId());
        dto.setTipo(cuenta.getTipo());
        dto.setEstado(cuenta.getEstado());
        dto.setSaldoActual(cuenta.getSaldo());

        if (cliente != null) {
            dto.setNombre(cliente.getNombre());
            dto.setEdad(cliente.getEdad());
            dto.setEmail(cliente.getEmail());
            dto.setPerfil(cliente.getPerfil());
            dto.setNivelRiesgo(cliente.getNivelRiesgo());
        }
        List<MovimientoWebDTO> historial = List.of();
        if (legacy != null) {
            dto.setSaldoInicialLegacy(legacy.getSaldoInicial());
            if (cliente == null) {          // respaldo: el core tambien conoce nombre y edad
                dto.setNombre(legacy.getNombre());
                dto.setEdad(legacy.getEdad());
            }
            if (legacy.getMovimientos() != null) {
                historial = legacy.getMovimientos().stream().map(this::aMovimientoWeb).toList();
            }
        }
        dto.setHistorialMovimientos(historial);
        dto.setDatosParciales(cliente == null || legacy == null);
        return dto;
    }

    public Page<TransaccionWebDTO> listarTransacciones(Pageable pageable) {
        return coreClient.listarTransacciones(pageable);
    }

    private MovimientoWebDTO aMovimientoWeb(MovimientoCoreDTO m) {
        return new MovimientoWebDTO(m.getId(), m.getFecha(), m.getTransaccion(), m.getMonto(), m.getDescripcion());
    }
}
