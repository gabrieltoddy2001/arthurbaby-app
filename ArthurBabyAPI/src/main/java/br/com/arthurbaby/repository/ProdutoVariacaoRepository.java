package br.com.arthurbaby.repository;
import br.com.arthurbaby.entity.Enums.VariacaoStatus;
import br.com.arthurbaby.entity.ProdutoVariacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface ProdutoVariacaoRepository extends JpaRepository<ProdutoVariacao, Long> {
    List<ProdutoVariacao> findByProdutoId(Long produtoId);
    List<ProdutoVariacao> findByProdutoIdAndStatus(Long produtoId, VariacaoStatus status);
    boolean existsBySku(String sku);

    /**
     * Baixa atômica: só subtrai se houver saldo suficiente, numa única instrução no banco.
     * Duas vendas simultâneas da última unidade não passam ambas (a segunda afeta 0 linhas).
     * @return 1 se baixou; 0 se o saldo era insuficiente
     */
    @Modifying(flushAutomatically = true)
    @Query("update ProdutoVariacao v set v.estoqueAtual = v.estoqueAtual - :quantidade "
            + "where v.id = :id and v.estoqueAtual >= :quantidade")
    int baixarSeDisponivel(@Param("id") Long id, @Param("quantidade") int quantidade);

    /** Soma atômica (entrada/estorno), sem sobrescrever alterações feitas por outra transação. */
    @Modifying(flushAutomatically = true)
    @Query("update ProdutoVariacao v set v.estoqueAtual = v.estoqueAtual + :quantidade where v.id = :id")
    int somarEstoque(@Param("id") Long id, @Param("quantidade") int quantidade);

    /** Define o saldo (ajuste manual de inventário). */
    @Modifying(flushAutomatically = true)
    @Query("update ProdutoVariacao v set v.estoqueAtual = :quantidade where v.id = :id")
    int definirEstoque(@Param("id") Long id, @Param("quantidade") int quantidade);

    /** Saldo atual lido do banco (não do objeto em memória, que pode estar desatualizado). */
    @Query("select v.estoqueAtual from ProdutoVariacao v where v.id = :id")
    Integer saldoAtual(@Param("id") Long id);

    /** Soma do estoque das variações ativas do produto (usada para marcar/desmarcar ESGOTADO). */
    @Query("select coalesce(sum(v.estoqueAtual), 0) from ProdutoVariacao v "
            + "where v.produto.id = :produtoId and v.status = br.com.arthurbaby.entity.Enums.VariacaoStatus.ATIVA")
    long saldoDoProduto(@Param("produtoId") Long produtoId);
}
