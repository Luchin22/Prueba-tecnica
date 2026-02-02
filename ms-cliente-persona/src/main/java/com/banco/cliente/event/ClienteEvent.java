package com.banco.cliente.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteEvent implements Serializable {
    private String clienteId;
    private String nombre;
    private String identificacion;
    private Boolean estado;
    private String eventType; // CREATED, UPDATED, DELETED
    @Builder.Default
    private Instant timestamp = Instant.now();
}
