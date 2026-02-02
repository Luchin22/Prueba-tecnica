package com.banco.cliente.entity;

import com.banco.common.util.IdGenerator;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(
    name = "clientes",
    indexes = {
        @Index(name = "idx_cliente_identificacion", columnList = "identificacion", unique = true),
        @Index(name = "idx_cliente_estado", columnList = "estado"),
        @Index(name = "idx_cliente_fecha_creacion", columnList = "fecha_creacion")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Cliente extends Persona {

    @Id
    @Column(name = "cliente_id", length = 12, nullable = false)
    private String clienteId;

    @Column(nullable = false, length = 100)
    private String contrasena;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estado = true;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private Instant fechaActualizacion;

    @PrePersist
    public void prePersist() {
        if (this.clienteId == null) {
            this.clienteId = IdGenerator.generateClienteId();
        }
    }
}
