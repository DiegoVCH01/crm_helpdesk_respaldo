package com.serviclient.crm.repository;

import com.serviclient.crm.entity.TicketHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketHistorialRepository extends JpaRepository<TicketHistorial, Long> {
    List<TicketHistorial> findByTicketIdOrderByCreatedAtDesc(Long ticketId);

    default List<TicketHistorial> findByTicketIdOrderByFechaCreacionDesc(Long ticketId) {
        return findByTicketIdOrderByCreatedAtDesc(ticketId);
    }
}
