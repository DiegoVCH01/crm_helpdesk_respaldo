package com.serviclient.crm.dao;

import com.serviclient.crm.entity.Notificacion;
import java.util.List;

public interface NotificacionDAO {

  Notificacion save(Notificacion notificacion);

  List<Notificacion> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);

  List<Notificacion> findByUsuarioIdAndLeidaFalseOrderByCreatedAtDesc(Long usuarioId);

  long countByUsuarioIdAndLeidaFalse(Long usuarioId);
}
