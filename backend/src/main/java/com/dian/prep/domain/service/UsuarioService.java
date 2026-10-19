package com.dian.prep.domain.service;

import com.dian.prep.domain.model.Usuario;
import com.dian.prep.domain.port.in.RegistrarUsuarioUseCase;
import com.dian.prep.domain.port.out.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService implements RegistrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder   passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder   = passwordEncoder;
    }

    @Override
    public Usuario registrar(RegistrarUsuarioCommand command) {
        if (usuarioRepository.existePorEmail(command.email())) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con el email: " + command.email());
        }
        String passwordEncoded = passwordEncoder.encode(command.password());
        Usuario usuario = Usuario.crear(command.nombre(), command.email(), passwordEncoded);
        return usuarioRepository.guardar(usuario);
    }
}
