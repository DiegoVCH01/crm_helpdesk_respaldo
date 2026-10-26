package com.serviclient.crm.config.security;

import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.Rol;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Implementación personalizada de {@link UserDetails} para representar el usuario autenticado en Spring Security.
 * <p>
 * Encapsula la información del usuario interno (administradores, agentes, supervisores), su pertenencia
 * a una empresa tenant específica y su rol mapeado a una autoridad con prefijo "ROLE_".
 * </p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see org.springframework.security.core.userdetails.UserDetails
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String nombreCompleto;
    private final String nombre;
    private final String apellido;
    private final String email;
    private final String password;
    private final Rol rol;
    private final Long empresaId;
    private final String empresaNombre;
    private final String avatarUrl;
    private final boolean activo;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Construye un objeto CustomUserDetails a partir de la entidad de dominio {@link Usuario}.
     *
     * @param usuario entidad de usuario con sus relaciones de rol y empresa
     */
    public CustomUserDetails(Usuario usuario) {
        this.id = usuario.getId();
        this.nombreCompleto = usuario.getNombreCompleto();
        this.nombre = usuario.getNombre();
        this.apellido = usuario.getApellido();
        this.email = usuario.getEmail();
        this.password = usuario.getPassword();
        this.rol = usuario.getRol();
        this.empresaId = (usuario.getEmpresa() != null) ? usuario.getEmpresa().getId() : null;
        this.empresaNombre = (usuario.getEmpresa() != null) ? usuario.getEmpresa().getNombreComercial() : "ServiClient";
        this.avatarUrl = usuario.getAvatarUrl();
        this.activo = Boolean.TRUE.equals(usuario.getActivo());
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
    }

    /**
     * Obtiene las iniciales del nombre y apellido para avatares por defecto.
     *
     * @return iniciales en mayúsculas (ej: "JD") o "U" si no están disponibles
     */
    public String getIniciales() {
        String n = (nombre != null && !nombre.isBlank()) ? String.valueOf(nombre.charAt(0)).toUpperCase() : "";
        String a = (apellido != null && !apellido.isBlank()) ? String.valueOf(apellido.charAt(0)).toUpperCase() : "";
        return (n + a).isBlank() ? "U" : n + a;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
