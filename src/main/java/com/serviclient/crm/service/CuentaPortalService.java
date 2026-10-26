package com.serviclient.crm.service;

import com.serviclient.crm.repository.CuentaPortalRepository;
import com.serviclient.crm.entity.Contacto;
import com.serviclient.crm.entity.CuentaPortal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio de gestión de cuentas y credenciales para el portal de clientes.
 * <p>
 * Administra el ciclo de vida del acceso de los contactos externos al portal de autoservicio:
 * activación y aprovisionamiento con claves temporales de 12 caracteres de alta entropía,
 * desactivación de accesos y sincronización ante modificaciones de correo electrónico.
 * </p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 * @see ICuentaPortalService
 */
@Service
@RequiredArgsConstructor
public class CuentaPortalService implements ICuentaPortalService {

  private final CuentaPortalRepository cuentaPortalRepository;
  private final PasswordEncoder passwordEncoder;

  private static final SecureRandom RANDOM = new SecureRandom();

  /**
   * Registro inmutable que encapsula las credenciales provisionales generadas para el usuario del portal.
   *
   * @param email correo electrónico de acceso al portal
   * @param passwordTemporal contraseña provisional generada en texto plano
   */
  public record CredencialesTemporales(String email, String passwordTemporal) {}

  /**
   * Activa o aprovisiona una cuenta de portal para un contacto dado, generando una contraseña temporal.
   * <p>
   * Si la cuenta ya existía, actualiza su hash con la nueva contraseña temporal y marca la obligación
   * de cambio de clave en el primer inicio de sesión.
   * </p>
   *
   * @param contacto contacto cliente a quien se habilita el acceso
   * @return {@link CredencialesTemporales} con el email y la contraseña provisional generada
   * @throws IllegalArgumentException si el contacto no es válido, no tiene correo o el correo ya pertenece a otro contacto
   */
  @Transactional
  public CredencialesTemporales activarOCrearCuenta(Contacto contacto) {

    if (contacto == null || contacto.getId() == null) {
      throw new IllegalArgumentException("El contacto debe estar registrado.");
    }

    if (contacto.getEmail() == null || contacto.getEmail().isBlank()) {
      throw new IllegalArgumentException("El contacto principal debe tener correo electrónico.");
    }

    // Evitar que otro contacto utilice el mismo usuario del portal.
    cuentaPortalRepository
        .findByEmail(contacto.getEmail())
        .filter(cuenta -> !cuenta.getContacto().getId().equals(contacto.getId()))
        .ifPresent(
            cuenta -> {
              throw new IllegalArgumentException(
                  "El correo ya está asociado a otra cuenta del portal.");
            });

    String passwordTemporal = generarPasswordTemporal();

    CuentaPortal cuenta =
        cuentaPortalRepository
            .findByContactoId(contacto.getId())
            .orElseGet(() -> CuentaPortal.builder().contacto(contacto).build());

    cuenta.setEmail(contacto.getEmail());
    cuenta.setPasswordHash(passwordEncoder.encode(passwordTemporal));
    cuenta.setEstado("ACTIVO");
    cuenta.setRequiereCambioPassword(true);

    cuentaPortalRepository.save(cuenta);

    return new CredencialesTemporales(contacto.getEmail(), passwordTemporal);
  }

  /**
   * Desactiva la cuenta de portal asociada a un contacto estableciendo su estado en "INACTIVO".
   *
   * @param contactoId identificador del contacto
   */
  @Transactional
  public void desactivarCuenta(Long contactoId) {

    cuentaPortalRepository
        .findByContactoId(contactoId)
        .ifPresent(
            cuenta -> {
              cuenta.setEstado("INACTIVO");
              cuentaPortalRepository.save(cuenta);
            });
  }

  /**
   * Actualiza el correo electrónico de acceso al portal del cliente si el contacto ya posee una cuenta activa.
   *
   * @param contacto contacto con los datos actualizados
   * @throws IllegalArgumentException si el contacto no es válido, no tiene correo o el correo ya pertenece a otra cuenta
   */
  @Transactional
  public void actualizarEmailSiExiste(Contacto contacto) {

    if (contacto == null || contacto.getId() == null) {
      throw new IllegalArgumentException("El contacto no es válido.");
    }

    if (contacto.getEmail() == null || contacto.getEmail().isBlank()) {
      throw new IllegalArgumentException("El correo del contacto no puede estar vacío.");
    }

    cuentaPortalRepository
        .findByContactoId(contacto.getId())
        .ifPresent(
            cuenta -> {

              // Verificar que el nuevo correo no pertenezca
              // a otra cuenta del portal.
              cuentaPortalRepository
                  .findByEmail(contacto.getEmail())
                  .filter(otraCuenta -> !otraCuenta.getId().equals(cuenta.getId()))
                  .ifPresent(
                      otraCuenta -> {
                        throw new IllegalArgumentException(
                            "El correo ya está asociado a otra cuenta del Portal Cliente.");
                      });

              cuenta.setEmail(contacto.getEmail());

              /*
               * Importante:
               * cambiar el correo NO cambia la contraseña
               * ni obliga a generar una nueva.
               */
              cuentaPortalRepository.save(cuenta);
            });
  }

  /**
   * Genera una contraseña segura aleatoria de 12 caracteres que combina mayúsculas, minúsculas, dígitos y símbolos.
   *
   * @return contraseña temporal de 12 caracteres
   */
  private String generarPasswordTemporal() {

    String mayusculas = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    String minusculas = "abcdefghijkmnopqrstuvwxyz";
    String numeros = "23456789";
    String especiales = "@#$%";

    List<Character> caracteres = new ArrayList<>();

    caracteres.add(mayusculas.charAt(RANDOM.nextInt(mayusculas.length())));

    caracteres.add(minusculas.charAt(RANDOM.nextInt(minusculas.length())));

    caracteres.add(numeros.charAt(RANDOM.nextInt(numeros.length())));

    caracteres.add(especiales.charAt(RANDOM.nextInt(especiales.length())));

    String todos = mayusculas + minusculas + numeros + especiales;

    while (caracteres.size() < 12) {
      caracteres.add(todos.charAt(RANDOM.nextInt(todos.length())));
    }

    Collections.shuffle(caracteres, RANDOM);

    StringBuilder password = new StringBuilder();

    for (Character caracter : caracteres) {
      password.append(caracter);
    }

    return password.toString();
  }
}
