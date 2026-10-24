package com.serviclient.crm.dao.impl;

import com.serviclient.crm.dao.UsuarioDAO;
import com.serviclient.crm.entity.Usuario;
import com.serviclient.crm.entity.enums.Rol;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioDAOImpl implements UsuarioDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Usuario> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(entityManager.find(Usuario.class, id));
    }

    @Override
    public Optional<Usuario> findByIdAndEmpresaId(Long id, Long empresaId) {
    if (id == null || empresaId == null) {
        return Optional.empty();
    }

    return entityManager
        .createQuery(
            "SELECT u FROM Usuario u "
                + "WHERE u.id = :id AND u.empresa.id = :empresaId",
            Usuario.class)
        .setParameter("id", id)
        .setParameter("empresaId", empresaId)
        .getResultStream()
        .findFirst();
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        TypedQuery<Usuario> query = entityManager.createQuery(
                "SELECT u FROM Usuario u WHERE u.email = :email", Usuario.class);
        query.setParameter("email", email);
        return query.getResultStream().findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(u) FROM Usuario u WHERE u.email = :email", Long.class);
        query.setParameter("email", email);
        Long count = query.getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public List<Usuario> findByEmpresaId(Long empresaId) {
        TypedQuery<Usuario> query = entityManager.createQuery(
                "SELECT u FROM Usuario u WHERE u.empresa.id = :empresaId", Usuario.class);
        query.setParameter("empresaId", empresaId);
        return query.getResultList();
    }

    @Override
    public List<Usuario> findByEmpresaIdAndRol(Long empresaId, Rol rol) {
        TypedQuery<Usuario> query = entityManager.createQuery(
                "SELECT u FROM Usuario u WHERE u.empresa.id = :empresaId AND LOWER(u.rolEntity.nombre) LIKE LOWER(CONCAT('%', :rol, '%'))", Usuario.class);
        query.setParameter("empresaId", empresaId);
        query.setParameter("rol", rol != null ? rol.name() : "");
        return query.getResultList();
    }

    @Override
    public List<Usuario> findByRol(Rol rol) {
        TypedQuery<Usuario> query = entityManager.createQuery(
                "SELECT u FROM Usuario u WHERE LOWER(u.rolEntity.nombre) LIKE LOWER(CONCAT('%', :rol, '%'))", Usuario.class);
        query.setParameter("rol", rol != null ? rol.name() : "");
        return query.getResultList();
    }

    @Override
    @Transactional
    public Usuario save(Usuario usuario) {
        if (usuario.getId() == null) {
            entityManager.persist(usuario);
            return usuario;
        } else {
            return entityManager.merge(usuario);
        }
    }

    @Override
    public List<Usuario> findAll() {
        return entityManager.createQuery("SELECT u FROM Usuario u", Usuario.class).getResultList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    @Transactional
    public void delete(Usuario usuario) {
        if (usuario != null) {
            Usuario toRemove = entityManager.contains(usuario) ? usuario : entityManager.merge(usuario);
            entityManager.remove(toRemove);
        }
    }
}
