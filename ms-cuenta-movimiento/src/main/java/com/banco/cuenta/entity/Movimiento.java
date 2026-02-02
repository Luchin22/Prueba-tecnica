package com.banco.cuenta.entity;

import com.banco.common.util.IdGenerator;
import com.banco.cuenta.enums.TipoMovimiento;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "movimientos",
    indexes = {
        @Index(name = "idx_mov_numero_cuenta", columnList = "numero_cuenta"),
        @Index(name = "idx_mov_fecha", columnList = "fecha"),
        @Index(name = "idx_mov_tipo", columnList = "tipo_movimiento"),
        @Index(name = "idx_mov_cuenta_fecha", columnList = "numero_cuenta, fecha"),
        @Index(name = "idx_mov_fecha_tipo", columnList = "fecha, tipo_movimiento")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movimiento {

    @Id
    @Column(name = "movimiento_id", length = 16, nullable = false)
    private String movimientoId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 10)
    private TipoMovimiento tipoMovimiento;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "saldo_anterior", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_despues", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoDespues;

    @Column(name = "numero_cuenta", nullable = false, length = 12)
    private String numeroCuenta;

    @Column(length = 500)
    private String descripcion;

    @PrePersist
    public void prePersist() {
        if (this.movimientoId == null) {
            this.movimientoId = IdGenerator.generateMovimientoId();
        }
    }
}
