package br.com.arthurbaby.config;

import br.com.arthurbaby.entity.Enums.ProdutoStatus;
import br.com.arthurbaby.entity.Produto;
import br.com.arthurbaby.repository.ProdutoRepository;
import br.com.arthurbaby.service.EstoqueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Na subida da API, alinha o status dos produtos ao estoque (depois das cargas de dados, @Order 2 e 3):
 * ATIVO sem saldo em nenhuma variação ativa vira ESGOTADO; ESGOTADO com saldo volta a ATIVO.
 * Produtos INATIVOS não são alterados. A partir daí, cada movimentação de estoque mantém o status em dia.
 */
@Component
@Order(10)
public class SituacaoEstoqueInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(SituacaoEstoqueInitializer.class);

    private final ProdutoRepository produtos;
    private final EstoqueService estoque;

    public SituacaoEstoqueInitializer(ProdutoRepository produtos, EstoqueService estoque) {
        this.produtos = produtos;
        this.estoque = estoque;
    }

    @Override
    @Transactional
    public void run(String... args) {
        int alterados = 0;
        for (Produto produto : produtos.findAll()) {
            ProdutoStatus antes = produto.getStatus();
            estoque.atualizarSituacaoProduto(produto);
            if (produto.getStatus() != antes) {
                alterados++;
                log.info("Produto {} ({}): status {} -> {} conforme o estoque", produto.getId(), produto.getNome(), antes, produto.getStatus());
            }
        }
        if (alterados > 0) log.info("{} produto(s) com status ajustado ao estoque", alterados);
    }
}
