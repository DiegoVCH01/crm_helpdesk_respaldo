package com.serviclient.crm.service;

import com.serviclient.crm.repository.UsuarioRepository;
import com.serviclient.crm.repository.EmpresaRepository;
import com.serviclient.crm.repository.RolRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Debe rechazar una contraseña formada solo por espacios")
    void rechazarPasswordSoloEspacios() {

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> usuarioService.cambiarPasswordTemporal(
                1L, "        ", "        ")
        );

        assertEquals(
            "La contraseña no puede contener solo espacios.",
            error.getMessage()
        );

        verifyNoInteractions(usuarioRepository, passwordEncoder);
    }
}