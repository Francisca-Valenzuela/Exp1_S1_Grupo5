package com.duoc.bffmobile.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.duoc.bffmobile.client.CoreClient;
import com.duoc.bffmobile.client.CuentaServiceDTO;
import com.duoc.bffmobile.client.CuentasClient;
import com.duoc.bffmobile.dto.CuentaCoreDTO;
import com.duoc.bffmobile.dto.CuentaMobileDTO;
import com.duoc.bffmobile.dto.MovimientoCoreDTO;
import com.duoc.bffmobile.dto.MovimientoMobileDTO;

@Service
public class MobileBffService {

    // Solo los movimientos recientes viajan al movil (no el historial completo del ano)
    private static final int MAX_MOVIMIENTOS_RECIENTES = 5;

    private final CuentasClient cuentasClient;
    private final CoreClient coreClient;

    public MobileBffService(CuentasClient cuentasClient, CoreClient coreClient) {
        this.cuentasClient = cuentasClient;
        this.coreClient = coreClient;
    }

    public CuentaMobileDTO obtenerCuenta(Long cuentaId) {
        CuentaServiceDTO cuenta = cuentasClient.obtener(cuentaId);   // esencial: saldo actual
        CuentaCoreDTO legacy = coreClient.obtenerCuenta(cuentaId);   // opcional: nombre + historial

        List<MovimientoMobileDTO> recientes = legacy == null || legacy.getMovimientos() == null
                ? List.of()
                : legacy.getMovimientos().stream()
                        .filter(m -> m.getFecha() != null)
                        .sorted(Comparator.comparing(MovimientoCoreDTO::getFecha).reversed())
                        .limit(MAX_MOVIMIENTOS_RECIENTES)
                        .map(m -> new MovimientoMobileDTO(m.getFecha(), m.getMonto(), m.getDescripcion()))
                        .toList();

        String nombre = legacy == null ? null : legacy.getNombre();
        return new CuentaMobileDTO(Objects.requireNonNull(cuenta.getCuentaId()), nombre, cuenta.getSaldo(), recientes);
    }
}
