package com.duoc.bancoxyzbatch.batch.partition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.core.io.Resource;

/**
 * Particiona un CSV en rangos de líneas de datos (sin contar el header),
 * para que cada partición sea procesada por un step independiente en paralelo.
 *
 * Cada ExecutionContext generado expone:
 *  - startLine: primera línea de datos (0-index, sin contar el header) que le corresponde a la partición
 *  - linesToRead: cantidad de líneas de datos que debe leer esa partición
 */
public class LineRangePartitioner implements Partitioner {

    private final Resource resource;

    public LineRangePartitioner(Resource resource) {
        this.resource = resource;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        long totalDataLines = contarLineasDeDatos();
        Map<String, ExecutionContext> partitions = new LinkedHashMap<>();

        if (totalDataLines == 0) {
            ExecutionContext context = new ExecutionContext();
            context.putLong("startLine", 0L);
            context.putLong("linesToRead", 0L);
            partitions.put("partition0", context);
            return partitions;
        }

        long linesPerPartition = (long) Math.ceil((double) totalDataLines / gridSize);
        long start = 0;
        int partitionNumber = 0;

        while (start < totalDataLines) {
            long end = Math.min(start + linesPerPartition, totalDataLines);

            ExecutionContext context = new ExecutionContext();
            context.putLong("startLine", start);
            context.putLong("linesToRead", end - start);
            context.putInt("partitionNumber", partitionNumber);
            partitions.put("partition" + partitionNumber, context);

            start = end;
            partitionNumber++;
        }
        return partitions;
    }

    private long contarLineasDeDatos() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            // se resta 1 para no contar la fila de encabezado (header)
            long totalLineas = reader.lines().count();
            return Math.max(0, totalLineas - 1);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo contar las lineas de datos de " + resource.getFilename(), e);
        }
    }
}
