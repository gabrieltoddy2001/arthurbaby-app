package br.com.arthurbaby.dto;
import java.time.LocalDateTime;
public record FavoritoResponse(Long id, ProdutoResumoResponse produto, LocalDateTime criadoEm) {}
