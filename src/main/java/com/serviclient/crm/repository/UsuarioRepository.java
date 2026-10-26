package com.serviclient.crm.repository;

import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link Usuario}.
 * <p>
 * Gestiona consultas de autenticación por email, unicidad y filtrado de usuarios por empresa tenant y rol.
 * </p>
 *
 * @author ServiClient Dev Team
 * @version 1.0.0
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por id restringido a su empresa asociada.
     *
     * @param id identificador del usuario
     * @param empresaId identificador de la empresa
     * @return {@link Optional} con el usuario encontrado
     */
    Optional<Usuario> findByIdAndEmpresaId(Long id, Long empresaId);

    /**
     * Busca un usuario por su correo electrónico único en el sistema.
     *
     * @param email correo electrónico de acceso
     * @return {@link Optional} con el usuario encontrado
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica la existencia de un usuario por su correo electrónico.
     *
     * @param email correo a verificar
     * @return true si ya existe, false en caso contrario
     */
    boolean existsByEmail(String email);

    /**
     * Lista todos los usuarios pertenecientes a una empresa.
     *
     * @param empresaId identificador de la empresa
     * @return lista de usuarios
     */
    List<Usuario> findByEmpresaId(Long empresaId);

    /**
     * Consulta usuarios de una empresa que posean un rol específico.
     *
     * @param empresaId identificador de la empresa
     * @param rol rol funcional a filtrar
     * @return lista de usuarios coincidentes
     */
    @Query("SELECT u FROM Usuario u WHERE u.empresa.id = :empresaId AND LOWER(u.rolEntity.nombre) LIKE LOWER(CONCAT('%', :#{#rol != null ? #rol.name() : ''}, '%'))")
    List<Usuario> findByEmpresaIdAndRol(@Param("empresaId") Long empresaId, @Param("rol") Rol rol);

    /**
     * Consulta usuarios de forma global según su rol.
     *
     * @param rol rol funcional
     * @return lista de usuarios
     */
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.rolEntity.nombre) LIKE LOWER(CONCAT('%', :#{#rol != null ? #rol.name() : ''}, '%'))")
    List<Usuario> findByRol(@Param("rol") Rol rol);
}
