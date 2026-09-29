-- =====================================================================
-- ArthurBaby - dump do banco MySQL `arthurbaby` (28/09/2026)
-- Estrutura completa de todas as tabelas, igual ao banco usado pela API
-- (inclui cupom dinamico e password_reset_token).
-- Dados: somente tabelas de referencia/catalogo. Usuarios, enderecos,
-- pedidos, favoritos, movimentacoes e tokens NAO sao exportados (dados
-- pessoais e hashes de senha). Os usuarios de teste sao criados pelo
-- DataInitializer no primeiro boot (senha 123456).
--
-- Restaurar:  mysql -u root -p < arthurbaby_dump.sql
-- =====================================================================
CREATE DATABASE IF NOT EXISTS arthurbaby DEFAULT CHARACTER SET utf8mb4;
USE arthurbaby;


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `categoria` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `categoria_pai_id` bigint unsigned DEFAULT NULL,
  `nome` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descricao` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `imagem_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ordem_exibicao` int NOT NULL DEFAULT '0',
  `status` enum('ATIVA','INATIVA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ATIVA',
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `atualizado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `icone` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nome` (`nome`),
  KEY `categoria_pai_id` (`categoria_pai_id`),
  CONSTRAINT `categoria_ibfk_1` FOREIGN KEY (`categoria_pai_id`) REFERENCES `categoria` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `configuracao_loja` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `nome_fantasia` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `razao_social` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cnpj` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `telefone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `whatsapp` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email_pedidos` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `instagram` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shopee_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `facebook_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `logradouro` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `numero` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `complemento` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bairro` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cidade` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `uf` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cep` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `logo_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cor_primaria` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cor_destaque` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cor_acento` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cor_fundo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cor_superficie` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cor_texto_primario` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cor_texto_secundario` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `atualizado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `cor` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `nome` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `codigo_hex` varchar(7) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ordem_exibicao` int NOT NULL DEFAULT '0',
  `status` enum('ATIVA','INATIVA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ATIVA',
  PRIMARY KEY (`id`),
  UNIQUE KEY `nome` (`nome`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `cupom` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ativo` bit(1) NOT NULL,
  `codigo` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `valido_ate` datetime(6) DEFAULT NULL,
  `criado_em` datetime(6) DEFAULT NULL,
  `descricao` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tipo` enum('FRETE_GRATIS','PERCENTUAL','VALOR_FIXO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `valido_de` datetime(6) DEFAULT NULL,
  `valor` decimal(12,2) DEFAULT NULL,
  `valor_maximo_desconto` decimal(12,2) DEFAULT NULL,
  `valor_minimo` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK3kcmt1d0xxfdloitavaeescse` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `endereco` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint unsigned NOT NULL,
  `cep` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `logradouro` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `numero` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `complemento` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bairro` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cidade` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `uf` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `referencia` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `principal` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `endereco_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `favorito` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `cliente_id` bigint unsigned NOT NULL,
  `produto_id` bigint unsigned NOT NULL,
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_favorito` (`cliente_id`,`produto_id`),
  UNIQUE KEY `UK5o2gnv7er5yas4fga3wmbxvl1` (`cliente_id`,`produto_id`),
  KEY `produto_id` (`produto_id`),
  CONSTRAINT `favorito_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `usuario` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `favorito_ibfk_2` FOREIGN KEY (`produto_id`) REFERENCES `produto` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `marca` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `nome` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ATIVA','INATIVA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ATIVA',
  PRIMARY KEY (`id`),
  UNIQUE KEY `nome` (`nome`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `modelo` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `nome` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ATIVO','INATIVO') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ATIVO',
  PRIMARY KEY (`id`),
  UNIQUE KEY `nome` (`nome`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `movimentacao_estoque` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `produto_id` bigint unsigned NOT NULL,
  `variacao_id` bigint unsigned DEFAULT NULL,
  `usuario_id` bigint unsigned DEFAULT NULL,
  `tipo` enum('ENTRADA','SAIDA','AJUSTE','RESERVA','ESTORNO') COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantidade` int NOT NULL,
  `estoque_anterior` int DEFAULT NULL,
  `estoque_posterior` int DEFAULT NULL,
  `observacao` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `variacao_id` (`variacao_id`),
  KEY `usuario_id` (`usuario_id`),
  KEY `idx_mov_estoque_produto` (`produto_id`),
  CONSTRAINT `movimentacao_estoque_ibfk_1` FOREIGN KEY (`produto_id`) REFERENCES `produto` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `movimentacao_estoque_ibfk_2` FOREIGN KEY (`variacao_id`) REFERENCES `produto_variacao` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `movimentacao_estoque_ibfk_3` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `password_reset_token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `criado_em` datetime(6) DEFAULT NULL,
  `expira_em` datetime(6) NOT NULL,
  `token` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `usado` bit(1) NOT NULL,
  `usuario_id` bigint unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKg0guo4k8krgpwuagos61oc06j` (`token`),
  KEY `idx_token_reset` (`token`),
  KEY `fk_password_reset_token_usuario` (`usuario_id`),
  CONSTRAINT `fk_password_reset_token_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pedido` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `numero_pedido` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cliente_id` bigint unsigned NOT NULL,
  `vendedor_id` bigint unsigned DEFAULT NULL,
  `status` enum('RASCUNHO','PEDIDO_GERADO','EM_ANALISE','AGUARDANDO_CONFIRMACAO','CONFIRMADO','SEPARANDO_PRODUTOS','PRONTO_PARA_RETIRADA','EM_TRANSPORTE','ENTREGUE','CANCELADO') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PEDIDO_GERADO',
  `forma_recebimento` enum('RETIRADA_LOJA','ENTREGA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'RETIRADA_LOJA',
  `subtotal` decimal(12,2) NOT NULL DEFAULT '0.00',
  `desconto` decimal(12,2) NOT NULL DEFAULT '0.00',
  `frete` decimal(12,2) NOT NULL DEFAULT '0.00',
  `total` decimal(12,2) NOT NULL DEFAULT '0.00',
  `observacao` text COLLATE utf8mb4_unicode_ci,
  `motivo_cancelamento` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `atualizado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `confirmado_em` datetime DEFAULT NULL,
  `cancelado_em` datetime DEFAULT NULL,
  `cupom` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `numero_pedido` (`numero_pedido`),
  KEY `vendedor_id` (`vendedor_id`),
  KEY `idx_pedido_cliente` (`cliente_id`),
  KEY `idx_pedido_status` (`status`),
  KEY `idx_pedido_data` (`criado_em`),
  CONSTRAINT `pedido_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `pedido_ibfk_2` FOREIGN KEY (`vendedor_id`) REFERENCES `usuario` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pedido_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `pedido_id` bigint unsigned NOT NULL,
  `produto_id` bigint unsigned NOT NULL,
  `variacao_id` bigint unsigned DEFAULT NULL,
  `codigo_produto` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nome_produto` varchar(180) COLLATE utf8mb4_unicode_ci NOT NULL,
  `variacao_descricao` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `quantidade` int NOT NULL,
  `valor_unitario` decimal(12,2) NOT NULL,
  `desconto` decimal(12,2) NOT NULL DEFAULT '0.00',
  `valor_total` decimal(12,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `produto_id` (`produto_id`),
  KEY `variacao_id` (`variacao_id`),
  KEY `idx_item_pedido` (`pedido_id`),
  CONSTRAINT `pedido_item_ibfk_1` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `pedido_item_ibfk_2` FOREIGN KEY (`produto_id`) REFERENCES `produto` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `pedido_item_ibfk_3` FOREIGN KEY (`variacao_id`) REFERENCES `produto_variacao` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pedido_status_historico` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `pedido_id` bigint unsigned NOT NULL,
  `usuario_id` bigint unsigned DEFAULT NULL,
  `status_anterior` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status_novo` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `observacao` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `pedido_id` (`pedido_id`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `pedido_status_historico_ibfk_1` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `pedido_status_historico_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `produto` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `categoria_id` bigint unsigned NOT NULL,
  `marca_id` bigint unsigned DEFAULT NULL,
  `codigo` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sku` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nome` varchar(180) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descricao` text COLLATE utf8mb4_unicode_ci,
  `preco` decimal(12,2) NOT NULL,
  `preco_promocional` decimal(12,2) DEFAULT NULL,
  `custo` decimal(12,2) DEFAULT NULL,
  `peso` decimal(38,2) DEFAULT NULL,
  `altura` decimal(38,2) DEFAULT NULL,
  `largura` decimal(38,2) DEFAULT NULL,
  `comprimento` decimal(38,2) DEFAULT NULL,
  `estoque_minimo` int NOT NULL DEFAULT '0',
  `status` enum('ATIVO','INATIVO','ESGOTADO') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ATIVO',
  `destaque` tinyint(1) NOT NULL DEFAULT '0',
  `promocao` tinyint(1) NOT NULL DEFAULT '0',
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `atualizado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `avaliacao` decimal(3,1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `codigo` (`codigo`),
  UNIQUE KEY `sku` (`sku`),
  KEY `marca_id` (`marca_id`),
  KEY `idx_produto_categoria` (`categoria_id`),
  KEY `idx_produto_status` (`status`),
  KEY `idx_produto_nome` (`nome`),
  CONSTRAINT `produto_ibfk_1` FOREIGN KEY (`categoria_id`) REFERENCES `categoria` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `produto_ibfk_2` FOREIGN KEY (`marca_id`) REFERENCES `marca` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `produto_imagem` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `produto_id` bigint unsigned NOT NULL,
  `url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descricao` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ordem_exibicao` int NOT NULL DEFAULT '0',
  `principal` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `produto_id` (`produto_id`),
  CONSTRAINT `produto_imagem_ibfk_1` FOREIGN KEY (`produto_id`) REFERENCES `produto` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `produto_variacao` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `produto_id` bigint unsigned NOT NULL,
  `tamanho_id` bigint unsigned DEFAULT NULL,
  `cor_id` bigint unsigned DEFAULT NULL,
  `modelo_id` bigint unsigned DEFAULT NULL,
  `sku` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `preco` decimal(12,2) DEFAULT NULL,
  `estoque_atual` int NOT NULL DEFAULT '0',
  `estoque_minimo` int NOT NULL DEFAULT '0',
  `status` enum('ATIVA','INATIVA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ATIVA',
  PRIMARY KEY (`id`),
  UNIQUE KEY `sku` (`sku`),
  KEY `tamanho_id` (`tamanho_id`),
  KEY `cor_id` (`cor_id`),
  KEY `modelo_id` (`modelo_id`),
  KEY `idx_variacao_produto` (`produto_id`),
  CONSTRAINT `produto_variacao_ibfk_1` FOREIGN KEY (`produto_id`) REFERENCES `produto` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `produto_variacao_ibfk_2` FOREIGN KEY (`tamanho_id`) REFERENCES `tamanho` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `produto_variacao_ibfk_3` FOREIGN KEY (`cor_id`) REFERENCES `cor` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `produto_variacao_ibfk_4` FOREIGN KEY (`modelo_id`) REFERENCES `modelo` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `tamanho` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `nome` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ordem_exibicao` int NOT NULL DEFAULT '0',
  `status` enum('ATIVO','INATIVO') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ATIVO',
  PRIMARY KEY (`id`),
  UNIQUE KEY `nome` (`nome`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `usuario` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `nome_completo` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `senha` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `telefone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cpf` varchar(14) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `perfil` enum('ADMINISTRADOR','VENDEDOR','CLIENTE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CLIENTE',
  `status` enum('ATIVO','INATIVO','BLOQUEADO') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ATIVO',
  `aceite_termo_uso` tinyint(1) NOT NULL DEFAULT '0',
  `data_aceite_termo_uso` datetime DEFAULT NULL,
  `aceite_lgpd` tinyint(1) NOT NULL DEFAULT '0',
  `data_aceite_lgpd` datetime DEFAULT NULL,
  `criado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `atualizado_em` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `token_recuperacao_senha` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `token_recuperacao_senha_expira_em` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `cpf` (`cpf`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


-- ---------------- Dados de referencia ----------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

LOCK TABLES `configuracao_loja` WRITE;
/*!40000 ALTER TABLE `configuracao_loja` DISABLE KEYS */;
INSERT  IGNORE INTO `configuracao_loja` (`id`, `nome_fantasia`, `razao_social`, `cnpj`, `telefone`, `whatsapp`, `email`, `email_pedidos`, `instagram`, `shopee_url`, `facebook_url`, `logradouro`, `numero`, `complemento`, `bairro`, `cidade`, `uf`, `cep`, `logo_url`, `cor_primaria`, `cor_destaque`, `cor_acento`, `cor_fundo`, `cor_superficie`, `cor_texto_primario`, `cor_texto_secundario`, `atualizado_em`) VALUES (1,'ARTHURBABY','ARTHUR BABY ENXOVAIS LTDA - ME','35.671.222/0001-10','(71) 99339-9816','(71) 99131-1944','arthuradorno13@gmail.com','pedidoababy@gmail.com','@lojao_arthur_baby','https://shopee.com.br/arthurbabylojao',NULL,'Avenida Sete de Setembro','548','Edf. Fatima Loja Lj','Centro - Dois de Julho','Salvador','BA','40020-455',NULL,'#37B6B0','#ED83A4',NULL,'#FFFFFF','#F0F2F5',NULL,NULL,'2026-09-19 13:35:12');
/*!40000 ALTER TABLE `configuracao_loja` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `categoria` WRITE;
/*!40000 ALTER TABLE `categoria` DISABLE KEYS */;
INSERT  IGNORE INTO `categoria` (`id`, `categoria_pai_id`, `nome`, `descricao`, `imagem_url`, `ordem_exibicao`, `status`, `criado_em`, `atualizado_em`, `icone`) VALUES (1,NULL,'Enxoval','Produtos para composição do enxoval do bebê.',NULL,1,'ATIVA','2026-09-13 21:26:44','2026-09-13 21:26:44',NULL),(2,NULL,'Roupas para Bebês','Roupas para recém-nascidos e bebês.',NULL,2,'ATIVA','2026-09-13 21:26:44','2026-09-13 21:26:44',NULL),(3,NULL,'Roupas Infantis','Roupas para crianças.',NULL,3,'ATIVA','2026-09-13 21:26:44','2026-09-13 21:26:44',NULL),(4,NULL,'Acessórios','Acessórios para bebês e crianças.',NULL,4,'ATIVA','2026-09-13 21:26:44','2026-09-13 21:26:44',NULL),(5,NULL,'Higiene e Cuidados','Produtos de higiene e cuidados infantis.',NULL,5,'ATIVA','2026-09-13 21:26:44','2026-09-13 21:26:44',NULL),(6,NULL,'Alimentação','Produtos e acessórios para alimentação infantil.',NULL,6,'ATIVA','2026-09-13 21:26:44','2026-09-13 21:26:44',NULL),(7,NULL,'Quarto do Bebê','Produtos e acessórios para o quarto do bebê.',NULL,7,'ATIVA','2026-09-13 21:26:44','2026-09-13 21:26:44',NULL),(8,NULL,'Presentes','Kits e produtos para presentes.',NULL,8,'ATIVA','2026-09-13 21:26:44','2026-09-13 21:26:44',NULL),(9,NULL,'Kits','Kits e conjuntos de produtos.',NULL,9,'ATIVA','2026-09-13 21:26:44','2026-09-13 21:26:44',NULL),(10,NULL,'Promoções','Produtos em promoção.',NULL,10,'ATIVA','2026-09-13 21:26:44','2026-09-13 21:26:44',NULL);
/*!40000 ALTER TABLE `categoria` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `marca` WRITE;
/*!40000 ALTER TABLE `marca` DISABLE KEYS */;
INSERT  IGNORE INTO `marca` (`id`, `nome`, `status`) VALUES (1,'ArthurBaby','ATIVA'),(2,'Baby Confort','ATIVA'),(3,'Kids Fashion','ATIVA');
/*!40000 ALTER TABLE `marca` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `tamanho` WRITE;
/*!40000 ALTER TABLE `tamanho` DISABLE KEYS */;
INSERT  IGNORE INTO `tamanho` (`id`, `nome`, `ordem_exibicao`, `status`) VALUES (1,'RN',1,'ATIVO'),(2,'P',2,'ATIVO'),(3,'M',3,'ATIVO'),(4,'G',4,'ATIVO'),(5,'GG',5,'ATIVO'),(6,'2',6,'ATIVO'),(7,'4',7,'ATIVO'),(8,'6',8,'ATIVO'),(9,'8',9,'ATIVO'),(10,'10',10,'ATIVO'),(11,'12',11,'ATIVO'),(12,'14',12,'ATIVO');
/*!40000 ALTER TABLE `tamanho` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `cor` WRITE;
/*!40000 ALTER TABLE `cor` DISABLE KEYS */;
INSERT  IGNORE INTO `cor` (`id`, `nome`, `codigo_hex`, `ordem_exibicao`, `status`) VALUES (1,'Branco','#FFFFFF',1,'ATIVA'),(2,'Azul','#3B82F6',2,'ATIVA'),(3,'Rosa','#EC4899',3,'ATIVA'),(4,'Amarelo','#FFC000',4,'ATIVA'),(5,'Verde','#4CB896',5,'ATIVA'),(6,'Bege','#E8D8C3',6,'ATIVA'),(7,'Cinza','#64748B',7,'ATIVA'),(8,'Preto','#000000',8,'ATIVA'),(9,'Vermelho','#EF4444',9,'ATIVA'),(10,'Azul Bebê','#89CFF0',2,'ATIVA'),(11,'Rosa Bebê','#F4A6C1',3,'ATIVA');
/*!40000 ALTER TABLE `cor` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `modelo` WRITE;
/*!40000 ALTER TABLE `modelo` DISABLE KEYS */;
INSERT  IGNORE INTO `modelo` (`id`, `nome`, `status`) VALUES (1,'Tradicional','ATIVO'),(2,'Ursinho','ATIVO'),(3,'Floral','ATIVO'),(4,'Padrão','ATIVO'),(5,'Premium','ATIVO'),(6,'Deluxe','ATIVO');
/*!40000 ALTER TABLE `modelo` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `cupom` WRITE;
/*!40000 ALTER TABLE `cupom` DISABLE KEYS */;
INSERT  IGNORE INTO `cupom` (`id`, `ativo`, `codigo`, `valido_ate`, `criado_em`, `descricao`, `tipo`, `valido_de`, `valor`, `valor_maximo_desconto`, `valor_minimo`) VALUES (1,_binary '','ARTHUR10',NULL,NULL,'10% de desconto','PERCENTUAL',NULL,10.00,NULL,NULL),(2,_binary '','FRETEGRATIS',NULL,'2026-09-28 22:15:17.696943','Frete gratis','FRETE_GRATIS',NULL,NULL,NULL,NULL),(3,_binary '','BEMVINDO',NULL,'2026-09-28 22:15:17.745552','R$ 15 de desconto','VALOR_FIXO',NULL,15.00,NULL,NULL);
/*!40000 ALTER TABLE `cupom` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `produto` WRITE;
/*!40000 ALTER TABLE `produto` DISABLE KEYS */;
INSERT  IGNORE INTO `produto` (`id`, `categoria_id`, `marca_id`, `codigo`, `sku`, `nome`, `descricao`, `preco`, `preco_promocional`, `custo`, `peso`, `altura`, `largura`, `comprimento`, `estoque_minimo`, `status`, `destaque`, `promocao`, `criado_em`, `atualizado_em`, `avaliacao`) VALUES (1,3,3,'KIDS-001','KIDS-001-2A-RS','Conjunto Infantil Feminino','Conjunto infantil composto por blusa e short.',89.90,79.90,45.00,0.25,5.00,25.00,30.00,5,'ATIVO',0,1,'2026-09-14 01:50:51','2026-09-28 22:15:18',5.0),(2,1,1,'AB-001','AB-001','Kit Enxoval Bebe','Produto inicial para testes da API',129.90,NULL,NULL,NULL,NULL,NULL,NULL,0,'ATIVO',1,0,'2026-09-28 22:15:18','2026-09-28 22:15:18',5.0);
/*!40000 ALTER TABLE `produto` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `produto_imagem` WRITE;
/*!40000 ALTER TABLE `produto_imagem` DISABLE KEYS */;
INSERT  IGNORE INTO `produto_imagem` (`id`, `produto_id`, `url`, `descricao`, `ordem_exibicao`, `principal`) VALUES (1,1,'https://placehold.co/400x400/ED83A4/FFFFFF?text=Produto','Imagem de exemplo',1,1),(2,2,'https://placehold.co/400x400/ED83A4/FFFFFF?text=Produto','Imagem de exemplo',1,1);
/*!40000 ALTER TABLE `produto_imagem` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `produto_variacao` WRITE;
/*!40000 ALTER TABLE `produto_variacao` DISABLE KEYS */;
INSERT  IGNORE INTO `produto_variacao` (`id`, `produto_id`, `tamanho_id`, `cor_id`, `modelo_id`, `sku`, `preco`, `estoque_atual`, `estoque_minimo`, `status`) VALUES (1,2,1,1,4,'AB-001-RN-BR',NULL,10,0,'ATIVA');
/*!40000 ALTER TABLE `produto_variacao` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

