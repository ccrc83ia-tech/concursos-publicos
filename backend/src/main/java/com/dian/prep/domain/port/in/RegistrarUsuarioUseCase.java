package com.dian.prep.domain.port.in;

import com.dian.prep.domain.model.Usuario;

public interface RegistrarUsuarioUseCase {

    record RegistrarUsuarioCommand(String nombre, String email, String password) {}

    Usuario registrar(RegistrarUsuarioCommand command);
}
