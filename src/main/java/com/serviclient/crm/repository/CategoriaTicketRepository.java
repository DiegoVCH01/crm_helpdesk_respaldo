package com.serviclient.crm.repository;

import com.serviclient.crm.entity.CategoriaTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaTicketRepository extends JpaRepository<CategoriaTicketEntity, Long> {
    List<CategoriaTicketEntity> findByEmpresaId(Long empresaId);
    Optional<CategoriaTicketEntity> findByEmpresaIdAndNombre(Long empresaId, String nombre);
}
