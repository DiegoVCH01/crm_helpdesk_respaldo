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

@Service
@RequiredArgsConstructor
public class CuentaPortalService implements ICuentaPortalService {

  private final CuentaPortalRepository cuentaPortalRepository;
  private final PasswordEncoder passwordEncoder;

  private static final SecureRandom RANDOM = new SecureRandom();

  public record CredencialesTemporales(String email, String passwordTemporal) {}

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
