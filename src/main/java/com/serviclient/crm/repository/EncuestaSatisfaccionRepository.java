package com.serviclient.crm.repository;

import com.serviclient.crm.entity.EncuestaSatisfaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EncuestaSatisfaccionRepository extends JpaRepository<EncuestaSatisfaccion, Long> {

    List<EncuestaSatisfaccion> findByClienteId(Long clienteId);

    List<EncuestaSatisfaccion> findByTicketId(Long ticketId);

    @Query("SELECT COALESCE(AVG(e.puntuacion), 0.0) FROM EncuestaSatisfaccion e WHERE e.cliente.id = :clienteId")
    Double obtenerPromedioCsatPorCliente(@Param("clienteId") Long clienteId);

    @Query("SELECT COALESCE(AVG(e.puntuacion), 0.0) FROM EncuestaSatisfaccion e WHERE e.cliente.empresa.id = :empresaId")
    Double obtenerPromedioCsatPorEmpresa(@Param("empresaId") Long empresaId);
}
