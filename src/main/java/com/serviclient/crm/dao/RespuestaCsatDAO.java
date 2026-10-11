package com.serviclient.crm.dao;

import com.serviclient.crm.entity.RespuestaCsat;
import java.util.Optional;

public interface RespuestaCsatDAO {
    RespuestaCsat save(RespuestaCsat respuesta);
    Optional<RespuestaCsat> findByEncuestaId(Long encuestaId);
}
