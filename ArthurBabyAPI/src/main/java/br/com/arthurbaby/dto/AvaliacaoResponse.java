package br.com.arthurbaby.dto;

import br.com.arthurbaby.entity.Avaliacao;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Avaliação exibida no detalhe do produto. Mostra só o nome do cliente (nada de e-mail/CPF),
 * já que a listagem é pública. {@code criadoEm} sai sem milissegundos, como combinado com o app.
 */
@Schema(description = "Avaliação de um produto")
public record AvaliacaoResponse(
        @Schema(description = "Id da avaliação", example = "1") Long id,
        @Schema(description = "Nota de 1 a 5", example = "5") Integer nota,
        @Schema(description = "Comentário", nullable = true, example = "Tecido muito macio, amei!") String comentario,
        @Schema(description = "Nome de quem avaliou", example = "Maria Silva") String clienteNome,
        @Schema(description = "Data da avaliação", example = "2026-10-19T10:00:00")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime criadoEm) {

    public static AvaliacaoResponse de(Avaliacao a) {
        return new AvaliacaoResponse(a.getId(), a.getNota(), a.getComentario(),
                a.getCliente() != null ? a.getCliente().getNomeCompleto() : null, a.getCriadoEm());
    }
}
