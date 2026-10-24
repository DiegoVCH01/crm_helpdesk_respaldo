package com.serviclient.crm.controller;

import com.serviclient.crm.dto.RegistroEmpresaDto;
import com.serviclient.crm.service.IEmpresaService;
import com.serviclient.crm.config.security.CustomUserDetails;
import com.serviclient.crm.service.IUsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador MVC responsable del flujo de autenticación y registro de empresa.
 *
 * <p>Gestiona tres grupos de endpoints:
 *
 * <ol>
 *   <li><b>Redirección raíz</b> — {@code GET /} redirige al listado de clientes.
 *   <li><b>Login/Logout</b> — Spring Security procesa el formulario; este controlador solo
 *       renderiza la vista con mensajes de error o éxito.
 *   <li><b>Wizard de registro</b> — Flujo en 3 pasos que crea la empresa, el usuario administrador
 *       y la configuración inicial (SLA, CSAT, Health Score). El DTO {@link
 *       com.serviclient.crm.dto.RegistroEmpresaDto} se mantiene en la sesión HTTP entre pasos
 *       mediante {@code @SessionAttributes}.
 * </ol>
 *
 * <p><b>Rutas expuestas:</b>
 *
 * <pre>
 * GET  /              → redirect:/clientes
 * GET  /login         → auth/login
 * POST /login         → procesado por Spring Security
 * GET  /registro/paso-1  → auth/registro-paso1
 * POST /registro/paso-1  → valida y redirige a paso-2
 * GET  /registro/paso-2  → auth/registro-paso2
 * POST /registro/paso-2  → valida y redirige a paso-3
 * GET  /registro/paso-3  → auth/registro-paso3
 * POST /registro/paso-3  → persiste empresa y redirige a /login
 * </pre>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see
 *     com.serviclient.crm.service.IEmpresaService#registrarEmpresaCompleta(com.serviclient.crm.dto.RegistroEmpresaDto)
 * @see com.serviclient.crm.dto.RegistroEmpresaDto
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@SessionAttributes("registroForm")
@Tag(
    name = "Autenticación",
    description = "Inicio de sesión, cierre de sesión y wizard de registro de empresa en 3 pasos")
public class AuthController {

  private final IEmpresaService empresaService;
  private final IUsuarioService usuarioService;

  /**
   * Inicializa el DTO de registro en la sesión si aún no existe. Spring lo reutiliza entre los 3
   * pasos del wizard gracias a {@code @SessionAttributes}.
   *
   * @return nuevo {@link RegistroEmpresaDto} vacío
   */
  @ModelAttribute("registroForm")
  public RegistroEmpresaDto initRegistroForm() {
    return new RegistroEmpresaDto();
  }

  /**
   * Redirige la raíz del sitio al dashboard general.
   *
   * @return redirección a {@code /dashboard}
   */
  @Operation(
      summary = "Raíz del sitio",
      description = "Redirige automáticamente al dashboard principal")
  @ApiResponse(responseCode = "302", description = "Redirección a /dashboard")
  @GetMapping("/")
  public String index() {
    return "redirect:/dashboard";
  }

  /**
   * Muestra la página de inicio de sesión con mensajes contextuales.
   *
   * @param error presente cuando Spring Security rechaza las credenciales
   * @param logout presente tras un cierre de sesión exitoso
   * @param registered presente tras un registro exitoso de empresa
   * @param model modelo Thymeleaf para pasar mensajes a la vista
   * @return nombre de la vista {@code auth/login}
   */
  @Operation(
      summary = "Pantalla de login",
      description =
          "Renderiza el formulario de inicio de sesión. Spring Security procesa el POST en esta"
              + " misma ruta.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Formulario de login renderizado"),
    @ApiResponse(responseCode = "302", description = "Redirección si ya hay sesión activa")
  })
  @GetMapping("/login")
  public String loginPage(
      @RequestParam(value = "error", required = false) String error,
      @RequestParam(value = "logout", required = false) String logout,
      @RequestParam(value = "registered", required = false) String registered,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      Model model) {
    if (error != null) {
      model.addAttribute(
          "errorMessage", "Credenciales incorrectas. Verifique su correo y contraseña.");
    }
    if (logout != null) {
      model.addAttribute("successMessage", "Ha cerrado sesión correctamente.");
    }
    if (registered != null) {
      model.addAttribute(
          "successMessage",
          "¡Empresa y administrador registrados exitosamente! Inicie sesión a continuación.");
    }

    if (userDetails != null) {

      boolean debeCambiar = Boolean.TRUE.equals(
          usuarioService.obtenerPorId(userDetails.getId())
              .getDebeCambiarPassword());

      model.addAttribute("debeCambiarPassword", debeCambiar);

      if (!debeCambiar) {
        return "redirect:/dashboard";
      }
    }
    return "auth/login";
  }

  // ── Paso 1: Datos de Empresa ──────────────────────────────────────────────

  /**
   * Muestra el formulario del Paso 1: datos de la empresa (nombre, RUC, industria, etc.).
   *
   * @param form DTO de registro mantenido en sesión
   * @param model modelo Thymeleaf
   * @return nombre de la vista {@code auth/registro-paso1}
   */
  @Operation(
      summary = "Paso 1 del registro — Datos de empresa",
      description =
          "Muestra el formulario con los datos de la empresa: nombre comercial, razón social, RUC,"
              + " industria y país.")
  @ApiResponse(responseCode = "200", description = "Formulario del paso 1 renderizado")
  @GetMapping("/registro/paso-1")
  public String registroPaso1(
      @ModelAttribute("registroForm") RegistroEmpresaDto form, Model model) {
    model.addAttribute("pasoActual", 1);
    return "auth/registro-paso1";
  }

  /**
   * Procesa el Paso 1 del wizard: valida nombre comercial y razón social. Si hay errores, regresa
   * al formulario; si no, avanza al Paso 2.
   *
   * @param form DTO con los datos del paso 1
   * @param bindingResult resultado de las validaciones manuales
   * @param model modelo Thymeleaf para reportar errores
   * @return redirección a {@code /registro/paso-2} o de vuelta al paso 1 si hay errores
   */
  @Operation(
      summary = "Procesar paso 1 — Datos de empresa",
      description =
          "Valida nombre comercial y razón social. Si son válidos avanza al paso 2; de lo contrario"
              + " devuelve el formulario con errores.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "302",
        description = "Validación exitosa — redirige a /registro/paso-2"),
    @ApiResponse(
        responseCode = "200",
        description = "Errores de validación — devuelve el formulario con mensajes")
  })
  @PostMapping("/registro/paso-1")
  public String procesarPaso1(
      @ModelAttribute("registroForm") RegistroEmpresaDto form,
      BindingResult bindingResult,
      Model model) {
    if (form.getNombreComercial() == null || form.getNombreComercial().isBlank()) {
      bindingResult.rejectValue(
          "nombreComercial", "error.nombreComercial", "El nombre comercial es obligatorio");
    }
    if (form.getRazonSocial() == null || form.getRazonSocial().isBlank()) {
      bindingResult.rejectValue(
          "razonSocial", "error.razonSocial", "La razón social es obligatoria");
    }
    if (form.getRuc() == null || form.getRuc().isBlank()) {
      bindingResult.rejectValue("ruc", "error.ruc", "El RUC es obligatorio");
    }

    if (bindingResult.hasErrors()) {
      // ¡NUEVO! Extraemos el mensaje de error y lo mandamos a la alerta roja
      model.addAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
      model.addAttribute("pasoActual", 1);
      return "auth/registro-paso1";
    }

    return "redirect:/registro/paso-2";
  }

  // ── Paso 2: Usuario Administrador ────────────────────────────────────────

  /**
   * Muestra el formulario del Paso 2: datos del usuario administrador (nombre, apellido, email,
   * contraseña).
   *
   * @param form DTO de registro mantenido en sesión
   * @param model modelo Thymeleaf
   * @return nombre de la vista {@code auth/registro-paso2}
   */
  @Operation(
      summary = "Paso 2 del registro — Datos del administrador",
      description =
          "Muestra el formulario para crear el usuario administrador de la empresa: nombre,"
              + " apellido, email y contraseña.")
  @ApiResponse(responseCode = "200", description = "Formulario del paso 2 renderizado")
  @GetMapping("/registro/paso-2")
  public String registroPaso2(
      @ModelAttribute("registroForm") RegistroEmpresaDto form, Model model) {
    model.addAttribute("pasoActual", 2);
    return "auth/registro-paso2";
  }

  /**
   * Procesa el Paso 2: valida nombre, apellido, email y contraseña del administrador. La
   * confirmación de contraseña se compara manualmente.
   *
   * @param form DTO con los datos del paso 2
   * @param bindingResult resultado de las validaciones
   * @param model modelo Thymeleaf
   * @return redirección a {@code /registro/paso-3} o de vuelta al paso 2 si hay errores
   */
  @Operation(
      summary = "Procesar paso 2 — Datos del administrador",
      description =
          "Valida nombre, apellido, email y contraseña. Verifica que la confirmación de contraseña"
              + " coincida.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "302",
        description = "Validación exitosa — redirige a /registro/paso-3"),
    @ApiResponse(
        responseCode = "200",
        description = "Errores de validación — devuelve el formulario")
  })
  @PostMapping("/registro/paso-2")
  public String procesarPaso2(
      @ModelAttribute("registroForm") RegistroEmpresaDto form,
      BindingResult bindingResult,
      Model model) {
    if (form.getAdminNombre() == null || form.getAdminNombre().isBlank()) {
      bindingResult.rejectValue("adminNombre", "error.adminNombre", "El nombre es obligatorio");
    }
    if (form.getAdminApellido() == null || form.getAdminApellido().isBlank()) {
      bindingResult.rejectValue(
          "adminApellido", "error.adminApellido", "El apellido es obligatorio");
    }
    if (form.getAdminEmail() == null
        || form.getAdminEmail().isBlank()
        || !form.getAdminEmail().contains("@")) {
      bindingResult.rejectValue(
          "adminEmail", "error.adminEmail", "Correo electrónico válido obligatorio");
    }
    if (form.getAdminPassword() == null || form.getAdminPassword().length() < 6) {
      bindingResult.rejectValue(
          "adminPassword", "error.adminPassword", "La contraseña debe tener al menos 6 caracteres");
    }
    if (form.getAdminPasswordConfirm() != null
        && !form.getAdminPasswordConfirm().equals(form.getAdminPassword())) {
      bindingResult.rejectValue(
          "adminPasswordConfirm", "error.adminPasswordConfirm", "Las contraseñas no coinciden");
    }

    if (bindingResult.hasErrors()) {
      // ¡NUEVO! Extraemos el mensaje de error y lo mandamos a la alerta roja
      model.addAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
      model.addAttribute("pasoActual", 2);
      return "auth/registro-paso2";
    }

    return "redirect:/registro/paso-3";
  }

  // ── Paso 3: Configuración Inicial SLA / CSAT / Health Score ─────────────

  /**
   * Muestra el formulario del Paso 3: configuración inicial de SLA, CSAT y factores del Health
   * Score.
   *
   * @param form DTO de registro mantenido en sesión
   * @param model modelo Thymeleaf
   * @return nombre de la vista {@code auth/registro-paso3}
   */
  @Operation(
      summary = "Paso 3 del registro — Configuración inicial",
      description =
          "Muestra el formulario de configuración inicial: umbrales de SLA, escala CSAT y pesos del"
              + " Health Score.")
  @ApiResponse(responseCode = "200", description = "Formulario del paso 3 renderizado")
  @GetMapping("/registro/paso-3")
  public String registroPaso3(
      @ModelAttribute("registroForm") RegistroEmpresaDto form, Model model) {
    model.addAttribute("pasoActual", 3);
    return "auth/registro-paso3";
  }

  /**
   * Procesa el Paso 3 y finaliza el registro: persiste la empresa, el rol administrador, el usuario
   * administrador y las configuraciones SLA, CSAT y Health Score. Limpia la sesión y redirige al
   * login con un mensaje de éxito.
   *
   * @param form DTO completo con datos de los 3 pasos
   * @param bindingResult resultado de validaciones
   * @param model modelo Thymeleaf
   * @param session sesión HTTP para limpiar el atributo de registro
   * @param redirectAttributes atributos flash para pasar mensajes tras la redirección
   * @return redirección a {@code /login} si el registro es exitoso, o de vuelta al paso 3
   * @throws IllegalArgumentException si el email ya está registrado en el sistema
   */
  @Operation(
      summary = "Finalizar registro de empresa",
      description =
          "Persiste la empresa, el usuario administrador y las configuraciones SLA/CSAT/Health"
              + " Score. Limpia la sesión y redirige al login.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "302",
        description = "Empresa registrada exitosamente — redirige a /login?registered"),
    @ApiResponse(
        responseCode = "200",
        description =
            "Error de negocio (email duplicado, datos inválidos) — devuelve el formulario"),
    @ApiResponse(responseCode = "500", description = "Error inesperado del servidor")
  })
  @PostMapping("/registro/paso-3")
  public String procesarPaso3(
      @ModelAttribute("registroForm") RegistroEmpresaDto form,
      BindingResult bindingResult,
      Model model,
      HttpSession session,
      RedirectAttributes redirectAttributes) {
    try {
      empresaService.registrarEmpresaCompleta(form);
      session.removeAttribute("registroForm");
      redirectAttributes.addFlashAttribute(
          "successMessage",
          "¡Empresa registrada con éxito! Ya puedes iniciar sesión con tu cuenta de"
              + " administrador.");
      return "redirect:/login";

    } catch (IllegalArgumentException | NullPointerException ex) {
      // ¡AQUÍ ATRAPAMOS LOS ERRORES DE GUAVA PRECONDITIONS!
      model.addAttribute("errorMessage", ex.getMessage());
      model.addAttribute("pasoActual", 3);
      return "auth/registro-paso3";

    } catch (Exception ex) {
      log.error("Error al registrar empresa", ex);
      model.addAttribute(
          "errorMessage", "Ocurrió un error inesperado durante el registro: " + ex.getMessage());
      model.addAttribute("pasoActual", 3);
      return "auth/registro-paso3";
    }
  }
}
