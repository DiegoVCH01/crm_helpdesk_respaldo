package com.serviclient.crm.service;

import com.serviclient.crm.config.security.CustomUserDetails;
import com.serviclient.crm.dao.EmpresaDAO;
import com.serviclient.crm.dao.RolDAO;
import com.serviclient.crm.dao.UsuarioDAO;
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

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final EmpresaDAO empresaDAO;
    private final RolDAO rolDAO;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }
        if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return usuarioDAO.findById(userDetails.getId());
        }
        return usuarioDAO.findByEmail(auth.getName());
    }

    @Transactional(readOnly = true)
    public List<Usuario> obtenerAgentesYAdminsPorEmpresa(Long empresaId) {
        return usuarioDAO.findByEmpresaId(empresaId);
    }

    @Transactional(readOnly = true)
    public List<Usuario> obtenerTodosPorEmpresa(Long empresaId) {
        return usuarioDAO.findByEmpresaId(empresaId);
    }

    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long id) {
    return usuarioDAO
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarAgentesPorEmpresa(Long empresaId) {
        return usuarioDAO.findByEmpresaId(empresaId).stream()
            .filter(usuario -> usuario.getRol() != Rol.ADMIN)
            .toList();
    }

    @Transactional(readOnly = true)
    public Usuario obtenerAgentePorEmpresa(Long id, Long empresaId) {
        Usuario usuario =
            usuarioDAO
                .findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "El usuario no existe o no pertenece a tu empresa."));

        if (usuario.getRol() == Rol.ADMIN) {
        throw new IllegalArgumentException(
            "Los administradores no pueden modificarse desde Gestión de Agentes.");
        }

        return usuario;
    }

    @Transactional
    public CredencialTemporalDto crearAgente(Long empresaId, AgenteDto dto) {

        validarRolGestionable(dto.getRol());

        String email = dto.getEmail().trim().toLowerCase();

        if (usuarioDAO.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese correo.");
        }

        Empresa empresa = empresaDAO
                .findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        RolEntity rolEntity = rolDAO
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

        Usuario usuarioGuardado = usuarioDAO.save(usuario);

        return new CredencialTemporalDto(usuarioGuardado, passwordTemporal);
    }

    @Transactional
    public Usuario actualizarAgente(Long id, Long empresaId, AgenteDto dto) {

        validarRolGestionable(dto.getRol());

        Usuario usuario = obtenerAgentePorEmpresa(id, empresaId);

        Optional<Usuario> existente = usuarioDAO.findByEmail(dto.getEmail().trim());

        if (existente.isPresent() && !existente.get().getId().equals(usuario.getId())) {
        throw new IllegalArgumentException("Ya existe un usuario registrado con ese correo.");
        }

        RolEntity rolEntity =
            rolDAO
                .findByNombre(nombreRol(dto.getRol()))
                .orElseThrow(() -> new IllegalStateException("No se encontró el rol solicitado."));

        usuario.setNombre(dto.getNombre().trim());
        usuario.setApellido(dto.getApellido().trim());
        usuario.setEmail(dto.getEmail().trim().toLowerCase());
        usuario.setTelefono(dto.getTelefono() != null ? dto.getTelefono().trim() : null);
        usuario.setRolEntity(rolEntity);

        return usuarioDAO.save(usuario);
    }

    @Transactional
    public void cambiarEstadoAgente(Long id, Long empresaId) {

        Usuario usuario = obtenerAgentePorEmpresa(id, empresaId);

        usuario.setActivo(!Boolean.TRUE.equals(usuario.getActivo()));

        usuarioDAO.save(usuario);
    }

    private void validarRolGestionable(Rol rol) {
        if (rol != Rol.AGENTE && rol != Rol.SUPERVISOR) {

        throw new IllegalArgumentException("Solo se puede asignar el rol Agente o Supervisor.");
        }
    }

    private String nombreRol(Rol rol) {
        return switch (rol) {
        case AGENTE -> "Agente de Soporte";
        case SUPERVISOR -> "Supervisor Customer Success";
        default -> throw new IllegalArgumentException("Rol no permitido para un agente.");
        };
    }

    @Transactional
    public String restablecerPasswordAgente(Long id, Long empresaId) {

        Usuario usuario = obtenerAgentePorEmpresa(id, empresaId);

        String passwordTemporal = generarPasswordTemporal();

        usuario.setPassword(passwordEncoder.encode(passwordTemporal));
        usuario.setDebeCambiarPassword(true);

        usuarioDAO.save(usuario);

        return passwordTemporal;
    }

    @Transactional
    public void cambiarPasswordTemporal(
            Long usuarioId, String nuevaPassword, String confirmarPassword) {

        if (nuevaPassword == null || nuevaPassword.length() < 8 || nuevaPassword.length() > 72) {
            throw new IllegalArgumentException("La nueva contraseña debe tener entre 8 y 72 caracteres.");
        }

        if (!nuevaPassword.equals(confirmarPassword)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden.");
        }

        Usuario usuario = usuarioDAO
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

        usuarioDAO.save(usuario);
    }

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
