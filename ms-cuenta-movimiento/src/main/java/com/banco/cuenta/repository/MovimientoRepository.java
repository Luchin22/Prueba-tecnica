package com.banco.cuenta.repository;

import com.banco.cuenta.entity.Movimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, String> {

    Page<Movimiento> findByNumeroCuenta(String numeroCuenta, Pageable pageable);

  
    @Query("""
        SELECT m FROM Movimiento m
        WHERE m.numeroCuenta IN :numerosCuenta
        AND m.fecha BETWEEN :fechaInicio AND :fechaFin
        ORDER BY m.numeroCuenta, m.fecha DESC
        """)
    List<Movimiento> findMovimientosParaReporte(
        @Param("numerosCuenta") List<String> numerosCuenta,
        @Param("fechaInicio") Instant fechaInicio,
        @Param("fechaFin") Instant fechaFin
    );

    @Query("SELECT m FROM Movimiento m WHERE m.numeroCuenta = :numeroCuenta ORDER BY m.fecha DESC")
    List<Movimiento> findTopByNumeroCuentaOrderByFechaDesc(@Param("numeroCuenta") String numeroCuenta, Pageable pageable);
}
