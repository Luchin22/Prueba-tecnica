package com.banco.cliente.repository;

import com.banco.cliente.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {

    boolean existsByIdentificacion(String identificacion);

    Optional<Cliente> findByIdentificacion(String identificacion);

    Page<Cliente> findByEstadoTrue(Pageable pageable);

    Optional<Cliente> findByClienteIdAndEstadoTrue(String clienteId);

    @Query("SELECT c FROM Cliente c WHERE c.estado = true")
    Page<Cliente> findAllActivos(Pageable pageable);
}
