package com.serviclient.crm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.security.test.context.support.WithUserDetails;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class SecurityAndAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testLoginAnonimoPermitido() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void testRegistroWizardPasosPermitidos() throws Exception {
        mockMvc.perform(get("/registro/paso-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro-paso1"));

        mockMvc.perform(get("/registro/paso-2"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro-paso2"));

        mockMvc.perform(get("/registro/paso-3"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/registro-paso3"));
    }

    @Test
    void testLoginExitoso() throws Exception {
        mockMvc.perform(formLogin().user("email", "admin@serviclient.com").password("admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(authenticated().withUsername("admin@serviclient.com"));
    }

    @Test
    void testLoginFallido() throws Exception {
        mockMvc.perform(formLogin().user("email", "admin@serviclient.com").password("clave_erronea"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"))
                .andExpect(unauthenticated());
    }

    @Test
    @WithUserDetails(
        value = "admin@serviclient.com",
        userDetailsServiceBeanName = "customUserDetailsService")
    void testAccesoAClientesAutenticado() throws Exception {
        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/index"))
                .andExpect(model().attributeExists("metricas", "clientesPage"));
    }

    @Test
    @WithUserDetails(
        value = "admin@serviclient.com",
        userDetailsServiceBeanName = "customUserDetailsService")
    void testAccesoATicketsAutenticado() throws Exception {
        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(view().name("helpdesk/index"))
                .andExpect(model().attributeExists("metricas", "ticketsPage"));
    }

    @Test
    @WithUserDetails(
        value = "admin@serviclient.com",
        userDetailsServiceBeanName = "customUserDetailsService")
    void testAccesoARenovacionesAutenticado() throws Exception {
        mockMvc.perform(get("/renovaciones"))
                .andExpect(status().isOk())
                .andExpect(view().name("renovaciones/index"))
                .andExpect(model().attributeExists("metricas", "renovaciones"));
    }
}
