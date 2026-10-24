package com.serviclient.crm.service;

import com.serviclient.crm.dto.AgenteDto;
import com.serviclient.crm.dto.CredencialTemporalDto;
import com.serviclient.crm.entity.Usuario;
import java.util.List;
import java.util.Optional;

public interface IUsuarioService {

    Optional<Usuario> obtenerUsuarioAutenticado();

    List<Usuario> obtenerAgentesYAdminsPorEmpresa(Long empresaId);

    List<Usuario> obtenerTodosPorEmpresa(Long empresaId);

    Usuario obtenerPorId(Long id);

    List<Usuario> listarAgentesPorEmpresa(Long empresaId);

    Usuario obtenerAgentePorEmpresa(Long id, Long empresaId);

    CredencialTemporalDto crearAgente(Long empresaId, AgenteDto dto);

    Usuario actualizarAgente(Long id, Long empresaId, AgenteDto dto);

    void cambiarEstadoAgente(Long id, Long empresaId);

    String restablecerPasswordAgente(Long id, Long empresaId);

    void cambiarPasswordTemporal(Long usuarioId, String nuevaPassword, String confirmarPassword);
}
