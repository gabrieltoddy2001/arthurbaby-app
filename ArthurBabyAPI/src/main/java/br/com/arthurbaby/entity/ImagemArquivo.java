package br.com.arthurbaby.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Conteúdo das fotos enviadas pelo painel, guardado no próprio banco.
 * No Render o disco é apagado a cada deploy/reinício, então as fotos não podem ficar só em uploads/.
 * O nome (UUID + extensão) é o mesmo que aparece na URL /api/produtos/imagens/{nome}.
 */
@Getter @Setter @Entity
@Table(name = "imagem_arquivo")
public class ImagemArquivo {
    @Id @Column(length = 64) private String nome;
    @Column(nullable = false, length = 30) private String tipo;
    @Column(nullable = false) private long tamanho;
    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB") private byte[] dados;
    @Column(nullable = false) private LocalDateTime criadoEm = LocalDateTime.now();
}
