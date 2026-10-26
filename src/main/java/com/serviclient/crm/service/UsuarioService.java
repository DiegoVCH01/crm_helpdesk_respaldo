package com.serviclient.crm.service;

import com.serviclient.crm.config.security.CustomUserDetails;
import com.serviclient.crm.repository.EmpresaRepository;
import com.serviclient.crm.repository.RolRepository;
import com.serviclient.crm.repository.UsuarioRepository;
import com.serviclient.crm.dto.AgenteDto;
import com.serviclient.crm.dto.CredencialTemporalDto;
import com.serviclient.crm.entity.Empresa;
import com.serviclient.crm.entity.RolEntity;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.Rol;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de gestión de usuarios y agentes del sistema.
 * <p>
 * Proporciona la lógica de negocio para la administración de agentes y supervisores,
 * consulta de usuarios autenticados, restablecimiento y actualización de contraseñas,
 * asignación de roles y control de acceso por empresa.
 * </p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see IUsuarioService
 */
@Service
@RequiredArgsConstructor
public class UsuarioService implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtiene el usuario actualmente autenticado a partir del contexto de Spring Security.
     *
     * @return {@link Optional} con la entidad del usuario autenticado o vacío si no hay sesión
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }
        if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return usuarioRepository.findById(userDetails.getId());
        }
        return usuarioRepository.findByEmail(auth.getName());
    }

    /**
     * Obtiene todos los usuarios (agentes y administradores) asociados a una empresa.
     *
     * @param empresaId identificador de la empresa
     * @return lista de usuarios pertenecientes a la empresa
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerAgentesYAdminsPorEmpresa(Long empresaId) {
        return usuarioRepository.findByEmpresaId(empresaId);
    }

    /**
     * Obtiene la totalidad de los usuarios registrados para una empresa.
     *
     * @param empresaId identificador de la empresa
     * @return lista completa de usuarios de la empresa
     */
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> obtenerTodosPorEmpresa(Long empresaId) {
        return usuarioRepository.findByEmpresaId(empresaId);
    }

    /**
     * Busca un usuario por su identificador primario.
     *
     * @param id identificador del usuario
     * @return entidad {@link Usuario} correspondiente
     * @throws IllegalArgumentException si el usuario no existe
     */
    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + id));
    }

    /**
     * Lista únicamente los usuarios con rol operativo (excluyendo administradores) de una empresa.
     *
     * @param empresaId identificador de la empresa
     * @return lista de agentes y supervisores
     */
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarAgentesPorEmpresa(Long empresaId) {
        return usuarioRepository.findByEmpresaId(empresaId).stream()
                .filter(usuario -> usuario.getRol() != Rol.ADMIN)
                .toList();
    }

    /**
     * Obtiene un agente específico validando su existencia y pertenencia a la empresa activa.
     *
     * @param id identificador del usuario
     * @param empresaId identificador de la empresa
     * @return entidad {@link Usuario} encontrada
     * @throws IllegalArgumentException si el usuario no existe, pertenece a otra empresa o es administrador
     */
    @Transactional(readOnly = true)
    public Usuario obtenerAgentePorEmpresa(Long id, Long empresaId) {
        Usuario usuario = usuarioRepository
                .findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "El usuario no existe o no pertenece a tu empresa."));

        if (usuario.getRol() == Rol.ADMIN) {
            throw new IllegalArgumentException(
                    "Los administradores no pueden modificarse desde Gestión de Agentes.");
        }

        return usuario;
    }

    /**
     * Registra un nuevo agente o supervisor para la empresa especificada y genera una contraseña temporal.
     *
     * @param empresaId identificador de la empresa
     * @param dto datos del agente a registrar
     * @return {@link CredencialTemporalDto} con el usuario creado y la contraseña generada en texto plano
     * @throws IllegalArgumentException si el rol no es gestionable, el email ya está en uso o la empresa no existe
     * @throws IllegalStateException si el rol solicitado no se encuentra en el repositorio
     */
    @Transactional
    public CredencialTemporalDto crearAgente(Long empresaId, AgenteDto dto) {

        validarRolGestionable(dto.getRol());

        String email = dto.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese correo.");
        }

        Empresa empresa = empresaRepository
                .findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        RolEntity rolEntity = rolRepository
                .findByNombre(nombreRol(dto.getRol()))
                .orElseThrow(() -> new IllegalStateException("No se encontró el rol solicitado."));

        String passwordTemporal = generarPasswordTemporal();

        Usuario usuario = Usuario.builder()
                .empresa(empresa)
                .rolEntity(rolEntity)
                .nombre(dto.getNombre().trim())
                .apellido(dto.getApellido().trim())
                .email(email)
                .telefono(dto.getTelefono() != null ? dto.getTelefono().trim() : null)
                .password(passwordEncoder.encode(passwordTemporal))
                .debeCambiarPassword(true)
                .estado("ACTIVO")
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        return new CredencialTemporalDto(usuarioGuardado, passwordTemporal);
    }

    /**
     * Actualiza los datos personales y el rol de un agente existente.
     *
     * @param id identificador del agente
     * @param empresaId identificador de la empresa para validación de acceso
     * @param dto nuevos datos del agente
     * @return entidad {@link Usuario} actualizada
     * @throws IllegalArgumentException si el rol no es gestionable o el correo ya pertenece a otro usuario
     * @throws IllegalStateException si el rol no existe en el catálogo
     */
    @Transactional
    public Usuario actualizarAgente(Long id, Long empresaId, AgenteDto dto) {

        validarRolGestionable(dto.getRol());

        Usuario usuario = obtenerAgentePorEmpresa(id, empresaId);

        Optional<Usuario> existente = usuarioRepository.findByEmail(dto.getEmail().trim());

        if (existente.isPresent() && !existente.get().getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese correo.");
        }

        RolEntity rolEntity = rolRepository
                .findByNombre(nombreRol(dto.getRol()))
                .orElseThrow(() -> new IllegalStateException("No se encontró el rol solicitado."));

        usuario.setNombre(dto.getNombre().trim());
        usuario.setApellido(dto.getApellido().trim());
        usuario.setEmail(dto.getEmail().trim().toLowerCase());
        usuario.setTelefono(dto.getTelefono() != null ? dto.getTelefono().trim() : null);
        usuario.setRolEntity(rolEntity);

        return usuarioRepository.save(usuario);
    }

    /**
     * Alterna el estado activo/inactivo de un agente.
     *
     * @param id identificador del agente
     * @param empresaId identificador de la empresa
     * @throws IllegalArgumentException si el agente no pertenece a la empresa o es administrador
     */
    @Transactional
    public void cambiarEstadoAgente(Long id, Long empresaId) {

        Usuario usuario = obtenerAgentePorEmpresa(id, empresaId);

        usuario.setActivo(!Boolean.TRUE.equals(usuario.getActivo()));

        usuarioRepository.save(usuario);
    }

    /**
     * Valida que el rol asignado sea un rol operativo permitido (AGENTE o SUPERVISOR).
     *
     * @param rol rol a validar
     * @throws IllegalArgumentException si el rol es nulo o no es gestionable
     */
    private void validarRolGestionable(Rol rol) {
        if (rol != Rol.AGENTE && rol != Rol.SUPERVISOR) {

            throw new IllegalArgumentException("Solo se puede asignar el rol Agente o Supervisor.");
        }
    }

    /**
     * Traduce el enum {@link Rol} al nombre descriptivo almacenado en la tabla de roles.
     *
     * @param rol enumeración del rol
     * @return nombre del rol en la entidad
     * @throws IllegalArgumentException si el rol no es compatible
     */
    private String nombreRol(Rol rol) {
        return switch (rol) {
            case AGENTE -> "Agente de Soporte";
            case SUPERVISOR -> "Supervisor Customer Success";
            default -> throw new IllegalArgumentException("Rol no permitido para un agente.");
        };
    }

    /**
     * Restablece la contraseña de un agente generando una nueva clave temporal aleatoria.
     *
     * @param id identificador del agente
     * @param empresaId identificador de la empresa
     * @return contraseña temporal generada en texto plano
     * @throws IllegalArgumentException si el usuario no es accesible
     */
    @Transactional
    public String restablecerPasswordAgente(Long id, Long empresaId) {

        Usuario usuario = obtenerAgentePorEmpresa(id, empresaId);

        String passwordTemporal = generarPasswordTemporal();

        usuario.setPassword(passwordEncoder.encode(passwordTemporal));
        usuario.setDebeCambiarPassword(true);

        usuarioRepository.save(usuario);

        return passwordTemporal;
    }

    /**
     * Valida el formato y la coincidencia de una nueva contraseña.
     *
     * @param nuevaPassword nueva contraseña propuesta
     * @param confirmarPassword confirmación de la contraseña
     * @throws IllegalArgumentException si la longitud no está entre 8 y 72 caracteres, está en blanco o no coinciden
     */
    private void validarNuevaPassword(
            String nuevaPassword, String confirmarPassword) {

        if (nuevaPassword == null ||
                nuevaPassword.length() < 8 ||
                nuevaPassword.length() > 72) {
            throw new IllegalArgumentException(
                    "La nueva contraseña debe tener entre 8 y 72 caracteres.");
        }

        if (nuevaPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "La contraseña no puede contener solo espacios.");
        }

        if (!nuevaPassword.equals(confirmarPassword)) {
            throw new IllegalArgumentException(
                    "Las contraseñas no coinciden.");
        }
    }

    /**
     * Permite a un usuario cambiar su contraseña temporal por una definitiva en su primer inicio de sesión.
     *
     * @param usuarioId identificador del usuario
     * @param nuevaPassword nueva contraseña elegida
     * @param confirmarPassword confirmación de la nueva contraseña
     * @throws IllegalArgumentException si el usuario no existe, no requiere cambio, las claves no coinciden o es igual a la anterior
     */
    @Transactional
    public void cambiarPasswordTemporal(
            Long usuarioId, String nuevaPassword, String confirmarPassword) {

        validarNuevaPassword(nuevaPassword, confirmarPassword);

        Usuario usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        if (!Boolean.TRUE.equals(usuario.getDebeCambiarPassword())) {
            throw new IllegalArgumentException("Este usuario no requiere cambiar su contraseña.");
        }

        if (passwordEncoder.matches(nuevaPassword, usuario.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la temporal.");
        }

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setDebeCambiarPassword(false);

        usuarioRepository.save(usuario);
    }

    /**
     * Genera una cadena aleatoria de 10 caracteres alfanuméricos segura para su uso como contraseña temporal.
     *
     * @return contraseña temporal generada
     */
    private String generarPasswordTemporal() {

        String caracteres = "ABCDEFGHJKLMNPQRSTUVWXYZ"
                + "abcdefghijkmnopqrstuvwxyz"
                + "23456789";

        SecureRandom random = new SecureRandom();

        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            int indice = random.nextInt(caracteres.length());
            password.append(caracteres.charAt(indice));
        }

        return password.toString();
    }
}
