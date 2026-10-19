package com.dian.prep.infrastructure.adapter.in.web.dto;

import java.util.UUID;

public record AuthResponse(
    UUID   id,
    String nombre,
    String email,
    String rol,
    String token
) {}
