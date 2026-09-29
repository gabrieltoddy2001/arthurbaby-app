package br.com.arthurbaby.dto;
import java.util.List;
public record CategoriaResponse(Long id, String nome, String icone, List<SubcategoriaResponse> subcategorias) {
    public record SubcategoriaResponse(Long id, String nome) {}
}
