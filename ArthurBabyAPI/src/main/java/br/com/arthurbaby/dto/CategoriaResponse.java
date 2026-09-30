package br.com.arthurbaby.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Categoria de primeiro nível do catálogo com as suas subcategorias aninhadas (árvore de 2 níveis). */
@Schema(description = "Categoria do catálogo com subcategorias")
public record CategoriaResponse(
        @Schema(description = "Id da categoria", example = "1") Long id,
        @Schema(description = "Nome da categoria", example = "Enxoval") String nome,
        @Schema(description = "Identificador do ícone usado pelo app (nome sem acentos, minúsculo, com _)", example = "enxoval") String icone,
        @Schema(description = "Subcategorias (vazia se não houver)") List<SubcategoriaResponse> subcategorias) {

    /** Subcategoria (filha de uma categoria de primeiro nível). */
    @Schema(description = "Subcategoria")
    public record SubcategoriaResponse(
            @Schema(description = "Id da subcategoria", example = "11") Long id,
            @Schema(description = "Nome da subcategoria", example = "Kit Berco") String nome) {}
}
