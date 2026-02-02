package com.banco.cuenta.repository;

import com.banco.cuenta.entity.Cuenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, String> {

    List<Cuenta> findByClienteIdAndEstadoTrue(String clienteId);

    Page<Cuenta> findByEstadoTrue(Pageable pageable);

    @Query("SELECT c FROM Cuenta c WHERE c.clienteId = :clienteId AND c.estado = true")
    List<Cuenta> findCuentasActivasPorCliente(@Param("clienteId") String clienteId);

    boolean existsByClienteIdAndEstadoTrue(String clienteId);
}
