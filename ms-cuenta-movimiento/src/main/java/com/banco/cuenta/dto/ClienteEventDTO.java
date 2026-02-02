package com.banco.cuenta.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteEventDTO implements Serializable {
    private String clienteId;
    private String nombre;
    private String identificacion;
    private Boolean estado;
    private String eventType; // CREATED, UPDATED, DELETED
}
