package com.serviclient.crm.repository;

import com.serviclient.crm.entity.TicketAdjunto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketAdjuntoRepository extends JpaRepository<TicketAdjunto, Long> {
    List<TicketAdjunto> findByTicketId(Long ticketId);
}
