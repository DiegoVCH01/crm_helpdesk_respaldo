package com.serviclient.crm.repository;

import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByIdAndEmpresaId(Long id, Long empresaId);

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByEmpresaId(Long empresaId);

    @Query("SELECT u FROM Usuario u WHERE u.empresa.id = :empresaId AND LOWER(u.rolEntity.nombre) LIKE LOWER(CONCAT('%', :#{#rol != null ? #rol.name() : ''}, '%'))")
    List<Usuario> findByEmpresaIdAndRol(@Param("empresaId") Long empresaId, @Param("rol") Rol rol);

    @Query("SELECT u FROM Usuario u WHERE LOWER(u.rolEntity.nombre) LIKE LOWER(CONCAT('%', :#{#rol != null ? #rol.name() : ''}, '%'))")
    List<Usuario> findByRol(@Param("rol") Rol rol);
}
