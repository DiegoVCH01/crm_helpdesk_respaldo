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

@Service("clienteUserDetailsService")
@RequiredArgsConstructor
public class ClienteUserDetailsService implements UserDetailsService {

    private final CuentaPortalRepository cuentaPortalRepository;
    private final PasswordEncoder passwordEncoder;

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