package com.serviclient.crm.dao;

import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.Rol;

import java.util.List;
import java.util.Optional;

public interface UsuarioDAO {
    Optional<Usuario> findById(Long id);
    Optional<Usuario> findByIdAndEmpresaId(Long id, Long empresaId);
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Usuario> findByEmpresaId(Long empresaId);
    List<Usuario> findByEmpresaIdAndRol(Long empresaId, Rol rol);
    List<Usuario> findByRol(Rol rol);
    Usuario save(Usuario usuario);
    List<Usuario> findAll();
    void deleteById(Long id);
    void delete(Usuario usuario);
}
