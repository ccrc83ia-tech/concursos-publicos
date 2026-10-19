package com.dian.prep.infrastructure.adapter.in.web;

import com.dian.prep.domain.model.Usuario;
import com.dian.prep.domain.port.in.RegistrarUsuarioUseCase;
import com.dian.prep.domain.port.out.UsuarioRepository;
import com.dian.prep.infrastructure.adapter.in.web.dto.AuthRequest;
import com.dian.prep.infrastructure.adapter.in.web.dto.AuthResponse;
import com.dian.prep.infrastructure.config.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro e inicio de sesión")
public class AuthController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final UsuarioRepository       usuarioRepository;
    private final PasswordEncoder         passwordEncoder;
    private final JwtUtil                 jwtUtil;

    public AuthController(RegistrarUsuarioUseCase registrarUsuarioUseCase,
                          UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.usuarioRepository       = usuarioRepository;
        this.passwordEncoder         = passwordEncoder;
        this.jwtUtil                 = jwtUtil;
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo usuario")
    public ResponseEntity<AuthResponse> registro(@Valid @RequestBody AuthRequest.Registro request) {
        Usuario usuario = registrarUsuarioUseCase.registrar(
                new RegistrarUsuarioUseCase.RegistrarUsuarioCommand(
                        request.nombre(), request.email(), request.password()));

        String token = jwtUtil.generarToken(usuario.getId(), usuario.getEmail(), usuario.getRol().name());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(usuario.getId(), usuario.getNombre(),
                        usuario.getEmail(), usuario.getRol().name(), token));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest.Login request) {
        Usuario usuario = usuarioRepository.buscarPorEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!usuario.isActivo()) {
            throw new IllegalArgumentException("Usuario inactivo");
        }

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String token = jwtUtil.generarToken(usuario.getId(), usuario.getEmail(), usuario.getRol().name());
        return ResponseEntity.ok(new AuthResponse(usuario.getId(), usuario.getNombre(),
                usuario.getEmail(), usuario.getRol().name(), token));
    }
}
