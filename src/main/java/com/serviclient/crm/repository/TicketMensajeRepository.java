package com.serviclient.crm.repository;

import com.serviclient.crm.entity.TicketMensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketMensajeRepository extends JpaRepository<TicketMensaje, Long> {
    List<TicketMensaje> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    default List<TicketMensaje> findByTicketIdOrderByFechaCreacionAsc(Long ticketId) {
        return findByTicketIdOrderByCreatedAtAsc(ticketId);
    }
}
