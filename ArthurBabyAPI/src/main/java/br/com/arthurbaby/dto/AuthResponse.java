package br.com.arthurbaby.dto;
public record AuthResponse(String token, Long usuarioId, String nome, String perfil) {}
