package com.serviclient.crm.config.security;

import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de carga de detalles de usuarios internos del sistema CRM para Spring Security.
 * <p>
 * Recupera las credenciales y el perfil del usuario a partir de su dirección de correo electrónico
 * y lo transforma en un {@link CustomUserDetails}.
 * </p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see CustomUserDetails
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Localiza y autentica a un usuario interno a partir de su correo electrónico.
     *
     * @param email dirección de correo del usuario
     * @return instancia de {@link UserDetails} con permisos y datos del usuario
     * @throws UsernameNotFoundException si no existe ningún usuario registrado con el correo suministrado
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
        return new CustomUserDetails(usuario);
    }
}
