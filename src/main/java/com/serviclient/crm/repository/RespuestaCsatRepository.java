package com.serviclient.crm.repository;

import com.serviclient.crm.entity.RespuestaCsat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RespuestaCsatRepository extends JpaRepository<RespuestaCsat, Long> {
    Optional<RespuestaCsat> findByEncuestaId(Long encuestaId);
}
