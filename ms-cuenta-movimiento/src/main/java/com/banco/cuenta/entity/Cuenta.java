package com.banco.cuenta.entity;

import com.banco.common.util.IdGenerator;
import com.banco.cuenta.enums.TipoCuenta;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "cuentas",
    indexes = {
        @Index(name = "idx_cuenta_cliente_id", columnList = "cliente_id"),
        @Index(name = "idx_cuenta_estado", columnList = "estado"),
        @Index(name = "idx_cuenta_tipo", columnList = "tipo_cuenta"),
        @Index(name = "idx_cuenta_cliente_estado", columnList = "cliente_id, estado")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cuenta {

    @Id
    @Column(name = "numero_cuenta", length = 12, nullable = false)
    private String numeroCuenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", nullable = false, length = 10)
    private TipoCuenta tipoCuenta;

    @Column(name = "saldo_inicial", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoInicial;

    @Column(name = "saldo_actual", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoActual;

    @Column(nullable = false)
    @Builder.Default
    private Boolean estado = true;

    @Column(name = "cliente_id", nullable = false, length = 12)
    private String clienteId;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private Instant fechaActualizacion;

    @Version
    @Column(nullable = false)
    private Long version;

    @PrePersist
    public void prePersist() {
        if (this.numeroCuenta == null) {
            this.numeroCuenta = IdGenerator.generateNumeroCuenta();
        }
        if (this.saldoActual == null) {
            this.saldoActual = this.saldoInicial;
        }
    }
}
