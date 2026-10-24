package com.serviclient.crm.dto;

import com.serviclient.crm.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CredencialTemporalDto {

  private Usuario usuario;
  private String passwordTemporal;
}
