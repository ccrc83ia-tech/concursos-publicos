package com.dian.prep.infrastructure.adapter.out.persistence;

import com.dian.prep.domain.model.Usuario;
import com.dian.prep.domain.port.out.UsuarioRepository;
import com.dian.prep.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import com.dian.prep.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UsuarioJpaAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;

    public UsuarioJpaAdapter(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioEntity entity = toEntity(usuario);
        UsuarioEntity saved  = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existePorEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    // --- Mappers ---

    private UsuarioEntity toEntity(Usuario u) {
        UsuarioEntity e = new UsuarioEntity();
        if (u.getId() != null) e.setId(u.getId());
        e.setNombre(u.getNombre());
        e.setEmail(u.getEmail());
        e.setPassword(u.getPassword());
        e.setRol(u.getRol().name());
        e.setActivo(u.isActivo());
        if (u.getCreatedAt() != null) e.setCreatedAt(u.getCreatedAt());
        return e;
    }

    private Usuario toDomain(UsuarioEntity e) {
        Usuario u = new Usuario();
        u.setId(e.getId());
        u.setNombre(e.getNombre());
        u.setEmail(e.getEmail());
        u.setPassword(e.getPassword());
        u.setRol(Usuario.RolUsuario.valueOf(e.getRol()));
        u.setActivo(e.isActivo());
        u.setCreatedAt(e.getCreatedAt());
        return u;
    }
}
