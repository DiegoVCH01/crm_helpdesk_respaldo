package com.serviclient.crm.service;

import com.serviclient.crm.dto.AgenteDto;
import com.serviclient.crm.dto.CredencialTemporalDto;
import com.serviclient.crm.entity.Usuario;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de servicio para la gestión de usuarios, agentes, roles y credenciales de acceso.
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see com.serviclient.crm.service.UsuarioService
 */
public interface IUsuarioService {

    /**
     * Obtiene el usuario autenticado actualmente a partir del contexto de seguridad de Spring Security.
     *
     * @return {@link Optional} con el usuario actual o vacío si no hay sesión activa
     */
    Optional<Usuario> obtenerUsuarioAutenticado();

    /**
     * Obtiene la lista de usuarios con rol de agente o administrador para una empresa dada.
     *
     * @param empresaId identificador de la empresa
     * @return lista de usuarios con perfil operativo o administrativo
     */
    List<Usuario> obtenerAgentesYAdminsPorEmpresa(Long empresaId);

    /**
     * Retorna todos los usuarios activos e inactivos registrados para una empresa.
     *
     * @param empresaId identificador de la empresa
     * @return lista completa de usuarios
     */
    List<Usuario> obtenerTodosPorEmpresa(Long empresaId);

    /**
     * Busca un usuario por su identificador primario.
     *
     * @param id identificador del usuario
     * @return entidad {@link Usuario}
     * @throws org.springframework.web.server.ResponseStatusException si el usuario no existe
     */
    Usuario obtenerPorId(Long id);

    /**
     * Obtiene la lista de agentes y supervisores pertenecientes a la empresa.
     *
     * @param empresaId identificador de la empresa
     * @return lista de agentes y supervisores
     */
    List<Usuario> listarAgentesPorEmpresa(Long empresaId);

    /**
     * Busca un agente validando su pertenencia a la empresa activa.
     *
     * @param id identificador del agente
     * @param empresaId identificador de la empresa
     * @return entidad {@link Usuario}
     * @throws org.springframework.web.server.ResponseStatusException si no existe o no pertenece a la empresa
     */
    Usuario obtenerAgentePorEmpresa(Long id, Long empresaId);

    /**
     * Registra un nuevo agente o supervisor generando una clave temporal aleatoria.
     *
     * @param empresaId identificador de la empresa
     * @param dto datos personales y rol del nuevo agente
     * @return objeto {@link CredencialTemporalDto} con el usuario creado y la clave generada
     * @throws IllegalArgumentException si el email ya se encuentra en uso
     */
    CredencialTemporalDto crearAgente(Long empresaId, AgenteDto dto);

    /**
     * Actualiza la información de perfil y el rol de un agente existente.
     *
     * @param id identificador del agente
     * @param empresaId identificador de la empresa
     * @param dto nuevos datos del agente
     * @return entidad {@link Usuario} actualizada
     * @throws IllegalArgumentException si el email está duplicado
     * @throws org.springframework.web.server.ResponseStatusException si el usuario no existe
     */
    Usuario actualizarAgente(Long id, Long empresaId, AgenteDto dto);

    /**
     * Alterna el estado activo/inactivo de un agente.
     *
     * @param id identificador del agente
     * @param empresaId identificador de la empresa
     * @throws org.springframework.web.server.ResponseStatusException si el agente no existe
     */
    void cambiarEstadoAgente(Long id, Long empresaId);

    /**
     * Restablece la contraseña de un agente generando una nueva clave temporal obligatoria.
     *
     * @param id identificador del agente
     * @param empresaId identificador de la empresa
     * @return la nueva contraseña temporal generada en texto plano
     * @throws org.springframework.web.server.ResponseStatusException si el agente no existe
     */
    String restablecerPasswordAgente(Long id, Long empresaId);

    /**
     * Valida y actualiza la contraseña definitiva de un usuario con clave temporal.
     *
     * @param usuarioId identificador del usuario
     * @param nuevaPassword nueva contraseña en texto plano
     * @param confirmarPassword confirmación que debe coincidir con la nueva contraseña
     * @throws IllegalArgumentException si las contraseñas no coinciden o no cumplen la longitud requerida
     */
    void cambiarPasswordTemporal(Long usuarioId, String nuevaPassword, String confirmarPassword);
}
