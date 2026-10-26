package com.serviclient.crm.config.security;

import com.serviclient.crm.entity.CuentaPortal;
import com.serviclient.crm.repository.CuentaPortalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Servicio de autenticación para los usuarios externos que acceden al portal de clientes.
 * <p>
 * Implementa {@link UserDetailsService} consultando el repositorio {@link CuentaPortalRepository}
 * y asignando el rol de seguridad {@code ROLE_CLIENTE}.
 * </p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see CuentaPortal
 */
@Service("clienteUserDetailsService")
@RequiredArgsConstructor
public class ClienteUserDetailsService implements UserDetailsService {

    private final CuentaPortalRepository cuentaPortalRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Carga y autentica un contacto cliente del portal a partir de su dirección de correo.
     *
     * @param username correo electrónico del contacto cliente
     * @return instancia de {@link UserDetails} con rol ROLE_CLIENTE y estado de activación
     * @throws UsernameNotFoundException si no existe una cuenta en el portal para el correo indicado
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CuentaPortal cuenta = cuentaPortalRepository.findByEmail(username != null ? username.trim() : "")
                .orElseThrow(() -> new UsernameNotFoundException("Cliente no encontrado con email: " + username));

        // Diagnóstico en consola:
        String hashBD = cuenta.getPasswordHash();
        boolean coincide = passwordEncoder.matches("admin123", hashBD);
        String nuevoHashGenerado = passwordEncoder.encode("admin123");

        System.out.println("=================================================");
        System.out.println(">>> USUARIO ENCONTRADO: " + cuenta.getEmail());
        System.out.println(">>> HASH LEÍDO DE LA BD: [" + hashBD + "]");
        System.out.println(">>> LONGITUD DEL HASH LEÍDO: " + (hashBD != null ? hashBD.length() : 0));
        System.out.println(">>> ¿COINCIDE CON 'admin123'?: " + coincide);
        System.out.println(">>> HASH VÁLIDO GENERADO AHORA: " + nuevoHashGenerado);
        System.out.println("=================================================");

        return new User(
                cuenta.getEmail(),
                cuenta.getPasswordHash(),
                cuenta.estaActiva(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );
    }
}