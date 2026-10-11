package com.serviclient.crm.dao;

import com.serviclient.crm.entity.EncuestaCsat;
import java.util.List;
import java.util.Optional;

public interface EncuestaCsatDAO {
    // Método de escritura
    EncuestaCsat save(EncuestaCsat encuestaCsat);
    
    // Métodos de lectura
    List<EncuestaCsat> findByEmpresaId(Long empresaId);
    List<EncuestaCsat> findByClienteId(Long clienteId);
    Optional<EncuestaCsat> findByTicketId(Long ticketId);
    
    // Cálculos (promedios)
    Double findAverageScoreByClienteId(Long clienteId);
    Double findAverageScoreByEmpresaId(Long empresaId);
}
