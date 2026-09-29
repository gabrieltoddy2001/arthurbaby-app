-- =====================================================================
-- ArthurBaby API — Script de criacao do banco de dados (MySQL 8+)
-- =====================================================================
-- Banco:   arthurbaby
-- Usuario: root
-- Senha:   Admin321
--
-- Este script e um espelho manual do modelo mapeado pelas entidades
-- JPA (br.com.arthurbaby.entity). Em desenvolvimento a aplicacao usa
-- spring.jpa.hibernate.ddl-auto=update (ver application-mysql.properties)
-- e cria/atualiza as tabelas sozinha ao subir — ou seja, NAO e
-- obrigatorio rodar este script para a API funcionar.
--
-- Use este arquivo quando quiser:
--   - criar o banco manualmente (MySQL Workbench, DBeaver, linha de
--     comando) antes de subir a aplicacao;
--   - conferir/documentar a estrutura esperada das tabelas;
--   - popular dados de referencia (categorias, tamanhos, cores, marca,
--     configuracao da loja) sem depender do primeiro boot da API.
--
-- Os usuarios de teste (admin@arthurbaby.com.br / admin123 e
-- cliente@teste.com / cliente123) NAO estao neste script porque a
-- senha precisa ser gravada com hash BCrypt gerado pelo backend — eles
-- sao criados automaticamente pelo DataInitializer no primeiro boot,
-- caso a tabela `usuario` esteja vazia.
--
-- Como executar (linha de comando):
--   mysql -u root -p < arthurbaby_schema.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS arthurbaby
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE arthurbaby;

SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------------
-- usuario (clientes, vendedores e administradores)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    id                                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome_completo                       VARCHAR(150)  NOT NULL,
    email                                VARCHAR(150)  NOT NULL,
    senha                                VARCHAR(255)  NOT NULL,
    telefone                             VARCHAR(20)   NULL,
    cpf                                  VARCHAR(14)   NULL,
    perfil                               VARCHAR(20)   NOT NULL DEFAULT 'CLIENTE',
    status                               VARCHAR(20)   NOT NULL DEFAULT 'ATIVO',
    aceite_termo_uso                     TINYINT(1)    NOT NULL DEFAULT 0,
    data_aceite_termo_uso                DATETIME      NULL,
    aceite_lgpd                          TINYINT(1)    NOT NULL DEFAULT 0,
    data_aceite_lgpd                     DATETIME      NULL,
    token_recuperacao_senha              VARCHAR(100)  NULL,
    token_recuperacao_senha_expira_em    DATETIME      NULL,
    criado_em                            DATETIME      NULL,
    atualizado_em                        DATETIME      NULL,
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT uk_usuario_cpf UNIQUE (cpf)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- endereco (enderecos de entrega/cobranca do cliente)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS endereco (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id     BIGINT        NOT NULL,
    cep            VARCHAR(9)    NULL,
    logradouro     VARCHAR(200)  NULL,
    numero         VARCHAR(20)   NULL,
    complemento    VARCHAR(100)  NULL,
    bairro         VARCHAR(100)  NULL,
    cidade         VARCHAR(100)  NULL,
    uf             VARCHAR(2)    NULL,
    referencia     VARCHAR(200)  NULL,
    principal      TINYINT(1)    NOT NULL DEFAULT 0,
    CONSTRAINT fk_endereco_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    INDEX idx_endereco_usuario (usuario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- categoria (com subcategoria via auto-relacionamento)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categoria (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    categoria_pai_id    BIGINT        NULL,
    nome                VARCHAR(100)  NOT NULL,
    descricao           VARCHAR(500)  NULL,
    imagem_url          VARCHAR(500)  NULL,
    icone               VARCHAR(50)   NULL,
    ordem_exibicao      INT           NOT NULL DEFAULT 0,
    status              VARCHAR(20)   NOT NULL DEFAULT 'ATIVA',
    criado_em           DATETIME      NULL,
    atualizado_em       DATETIME      NULL,
    CONSTRAINT uk_categoria_nome UNIQUE (nome),
    CONSTRAINT fk_categoria_pai FOREIGN KEY (categoria_pai_id) REFERENCES categoria (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- marca / tamanho / cor / modelo (atributos de produto)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS marca (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome     VARCHAR(255) NOT NULL,
    status   VARCHAR(20)  NOT NULL DEFAULT 'ATIVA',
    CONSTRAINT uk_marca_nome UNIQUE (nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tamanho (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome              VARCHAR(30) NOT NULL,
    ordem_exibicao    INT         NOT NULL DEFAULT 0,
    status            VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    CONSTRAINT uk_tamanho_nome UNIQUE (nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cor (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome              VARCHAR(50) NOT NULL,
    codigo_hex        VARCHAR(7)  NULL,
    ordem_exibicao    INT         NOT NULL DEFAULT 0,
    status            VARCHAR(20) NOT NULL DEFAULT 'ATIVA',
    CONSTRAINT uk_cor_nome UNIQUE (nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS modelo (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome     VARCHAR(80) NOT NULL,
    status   VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    CONSTRAINT uk_modelo_nome UNIQUE (nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- produto / produto_imagem / produto_variacao
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS produto (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    categoria_id          BIGINT         NOT NULL,
    marca_id              BIGINT         NULL,
    codigo                VARCHAR(50)    NOT NULL,
    sku                   VARCHAR(80)    NOT NULL,
    nome                  VARCHAR(180)   NOT NULL,
    descricao             TEXT           NULL,
    preco                 DECIMAL(12,2)  NOT NULL,
    preco_promocional     DECIMAL(12,2)  NULL,
    custo                 DECIMAL(12,2)  NULL,
    peso                  DECIMAL(10,3)  NULL,
    altura                DECIMAL(10,3)  NULL,
    largura               DECIMAL(10,3)  NULL,
    comprimento           DECIMAL(10,3)  NULL,
    estoque_minimo        INT            NOT NULL DEFAULT 0,
    status                VARCHAR(20)    NOT NULL DEFAULT 'ATIVO',
    destaque              TINYINT(1)     NOT NULL DEFAULT 0,
    promocao              TINYINT(1)     NOT NULL DEFAULT 0,
    avaliacao             DECIMAL(3,1)   NULL,
    criado_em             DATETIME       NULL,
    atualizado_em         DATETIME       NULL,
    CONSTRAINT uk_produto_codigo UNIQUE (codigo),
    CONSTRAINT uk_produto_sku UNIQUE (sku),
    CONSTRAINT fk_produto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id),
    CONSTRAINT fk_produto_marca FOREIGN KEY (marca_id) REFERENCES marca (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS produto_imagem (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    produto_id        BIGINT        NOT NULL,
    url               VARCHAR(500)  NOT NULL,
    descricao         VARCHAR(255)  NULL,
    ordem_exibicao    INT           NOT NULL DEFAULT 0,
    principal         TINYINT(1)    NOT NULL DEFAULT 0,
    CONSTRAINT fk_produto_imagem_produto FOREIGN KEY (produto_id) REFERENCES produto (id) ON DELETE CASCADE,
    INDEX idx_produto_imagem_produto (produto_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS produto_variacao (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    produto_id        BIGINT         NOT NULL,
    tamanho_id        BIGINT         NULL,
    cor_id            BIGINT         NULL,
    modelo_id         BIGINT         NULL,
    sku               VARCHAR(100)   NOT NULL,
    preco             DECIMAL(12,2)  NULL,
    estoque_atual     INT            NOT NULL DEFAULT 0,
    estoque_minimo    INT            NOT NULL DEFAULT 0,
    status            VARCHAR(20)    NOT NULL DEFAULT 'ATIVA',
    CONSTRAINT uk_produto_variacao_sku UNIQUE (sku),
    CONSTRAINT fk_variacao_produto FOREIGN KEY (produto_id) REFERENCES produto (id) ON DELETE CASCADE,
    CONSTRAINT fk_variacao_tamanho FOREIGN KEY (tamanho_id) REFERENCES tamanho (id),
    CONSTRAINT fk_variacao_cor FOREIGN KEY (cor_id) REFERENCES cor (id),
    CONSTRAINT fk_variacao_modelo FOREIGN KEY (modelo_id) REFERENCES modelo (id),
    INDEX idx_variacao_produto (produto_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- favorito (produtos favoritados por cliente)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS favorito (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id    BIGINT    NOT NULL,
    produto_id    BIGINT    NOT NULL,
    criado_em     DATETIME  NULL,
    CONSTRAINT uk_favorito_cliente_produto UNIQUE (cliente_id, produto_id),
    CONSTRAINT fk_favorito_cliente FOREIGN KEY (cliente_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT fk_favorito_produto FOREIGN KEY (produto_id) REFERENCES produto (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- movimentacao_estoque
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS movimentacao_estoque (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    produto_id            BIGINT        NOT NULL,
    variacao_id           BIGINT        NULL,
    usuario_id            BIGINT        NULL,
    tipo                  VARCHAR(20)   NOT NULL,
    quantidade            INT           NOT NULL DEFAULT 0,
    estoque_anterior      INT           NULL,
    estoque_posterior     INT           NULL,
    observacao            VARCHAR(500)  NULL,
    criado_em             DATETIME      NULL,
    CONSTRAINT fk_mov_estoque_produto FOREIGN KEY (produto_id) REFERENCES produto (id),
    CONSTRAINT fk_mov_estoque_variacao FOREIGN KEY (variacao_id) REFERENCES produto_variacao (id),
    CONSTRAINT fk_mov_estoque_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    INDEX idx_mov_estoque_produto (produto_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- cupom (desconto percentual aplicado sobre o subtotal do pedido)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cupom (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo                VARCHAR(50)   NOT NULL,
    percentual_desconto   DECIMAL(5,2)  NOT NULL,
    ativo                 TINYINT(1)    NOT NULL DEFAULT 1,
    valido_ate            DATETIME      NULL,
    CONSTRAINT uk_cupom_codigo UNIQUE (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- pedido / pedido_item / pedido_status_historico
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pedido (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_pedido           VARCHAR(30)    NOT NULL,
    cliente_id              BIGINT         NOT NULL,
    vendedor_id             BIGINT         NULL,
    status                  VARCHAR(30)    NOT NULL DEFAULT 'PEDIDO_GERADO',
    forma_recebimento       VARCHAR(20)    NOT NULL DEFAULT 'RETIRADA_LOJA',
    subtotal                DECIMAL(12,2)  NOT NULL DEFAULT 0,
    desconto                DECIMAL(12,2)  NOT NULL DEFAULT 0,
    frete                   DECIMAL(12,2)  NOT NULL DEFAULT 0,
    total                   DECIMAL(12,2)  NOT NULL DEFAULT 0,
    cupom                   VARCHAR(50)    NULL,
    observacao              TEXT           NULL,
    motivo_cancelamento     VARCHAR(255)   NULL,
    criado_em               DATETIME       NULL,
    atualizado_em            DATETIME       NULL,
    confirmado_em            DATETIME       NULL,
    cancelado_em             DATETIME       NULL,
    CONSTRAINT uk_pedido_numero UNIQUE (numero_pedido),
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES usuario (id),
    CONSTRAINT fk_pedido_vendedor FOREIGN KEY (vendedor_id) REFERENCES usuario (id),
    INDEX idx_pedido_cliente (cliente_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pedido_item (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id               BIGINT         NOT NULL,
    produto_id               BIGINT         NOT NULL,
    variacao_id              BIGINT         NULL,
    codigo_produto           VARCHAR(50)    NOT NULL,
    nome_produto             VARCHAR(180)   NOT NULL,
    variacao_descricao       VARCHAR(255)   NULL,
    quantidade                INT            NOT NULL DEFAULT 0,
    valor_unitario            DECIMAL(12,2)  NOT NULL,
    desconto                  DECIMAL(12,2)  NOT NULL DEFAULT 0,
    valor_total               DECIMAL(12,2)  NOT NULL,
    CONSTRAINT fk_pedido_item_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id) ON DELETE CASCADE,
    CONSTRAINT fk_pedido_item_produto FOREIGN KEY (produto_id) REFERENCES produto (id),
    CONSTRAINT fk_pedido_item_variacao FOREIGN KEY (variacao_id) REFERENCES produto_variacao (id),
    INDEX idx_pedido_item_pedido (pedido_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pedido_status_historico (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id          BIGINT        NOT NULL,
    usuario_id         BIGINT        NULL,
    status_anterior    VARCHAR(30)   NULL,
    status_novo        VARCHAR(30)   NOT NULL,
    observacao         VARCHAR(500)  NULL,
    criado_em          DATETIME      NULL,
    CONSTRAINT fk_status_hist_pedido FOREIGN KEY (pedido_id) REFERENCES pedido (id) ON DELETE CASCADE,
    CONSTRAINT fk_status_hist_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    INDEX idx_status_hist_pedido (pedido_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- configuracao_loja
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS configuracao_loja (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome_fantasia            VARCHAR(255)  NULL,
    razao_social             VARCHAR(255)  NULL,
    cnpj                     VARCHAR(20)   NULL,
    telefone                 VARCHAR(20)   NULL,
    whatsapp                 VARCHAR(20)   NULL,
    email                    VARCHAR(150)  NULL,
    email_pedidos            VARCHAR(150)  NULL,
    instagram                VARCHAR(100)  NULL,
    shopee_url               VARCHAR(255)  NULL,
    facebook_url             VARCHAR(255)  NULL,
    logradouro               VARCHAR(200)  NULL,
    numero                   VARCHAR(20)   NULL,
    complemento              VARCHAR(100)  NULL,
    bairro                   VARCHAR(100)  NULL,
    cidade                   VARCHAR(100)  NULL,
    uf                       VARCHAR(2)    NULL,
    cep                      VARCHAR(9)    NULL,
    logo_url                 VARCHAR(500)  NULL,
    cor_primaria             VARCHAR(7)    NULL,
    cor_destaque             VARCHAR(7)    NULL,
    cor_acento               VARCHAR(7)    NULL,
    cor_fundo                VARCHAR(7)    NULL,
    cor_superficie           VARCHAR(7)    NULL,
    cor_texto_primario       VARCHAR(7)    NULL,
    cor_texto_secundario     VARCHAR(7)    NULL,
    atualizado_em            DATETIME      NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- Dados de referencia (opcional)
-- Idempotente: pode rodar mais de uma vez sem duplicar registros.
-- Espelha o conteudo de br.com.arthurbaby.config.DataInitializer.
-- =====================================================================

INSERT INTO configuracao_loja (nome_fantasia, razao_social, cnpj, telefone, whatsapp, email, email_pedidos,
                                instagram, shopee_url, logradouro, numero, complemento, bairro, cidade, uf, cep,
                                cor_primaria, cor_destaque, cor_fundo, cor_superficie)
SELECT 'ARTHURBABY', 'ARTHUR BABY ENXOVAIS LTDA - ME', '35.671.222/0001-10', '(71) 99339-9816', '(71) 99131-1944',
       'arthuradorno13@gmail.com', 'pedidoababy@gmail.com', '@lojao_arthur_baby',
       'https://shopee.com.br/arthurbabylojao', 'Avenida Sete de Setembro', '548', 'Edf. Fatima Loja Lj',
       'Centro - Dois de Julho', 'Salvador', 'BA', '40020-455', '#37B6B0', '#ED83A4', '#FFFFFF', '#F0F2F5'
WHERE NOT EXISTS (SELECT 1 FROM configuracao_loja);

INSERT IGNORE INTO categoria (nome, descricao, ordem_exibicao, icone) VALUES
    ('Enxoval', 'Categoria Enxoval', 1, 'enxoval'),
    ('Roupas para Bebes', 'Categoria Roupas para Bebes', 2, 'roupas_para_bebes'),
    ('Roupas Infantis', 'Categoria Roupas Infantis', 3, 'roupas_infantis'),
    ('Acessorios', 'Categoria Acessorios', 4, 'acessorios'),
    ('Higiene e Cuidados', 'Categoria Higiene e Cuidados', 5, 'higiene_e_cuidados'),
    ('Alimentacao', 'Categoria Alimentacao', 6, 'alimentacao'),
    ('Quarto do Bebe', 'Categoria Quarto do Bebe', 7, 'quarto_do_bebe'),
    ('Presentes', 'Categoria Presentes', 8, 'presentes'),
    ('Kits', 'Categoria Kits', 9, 'kits'),
    ('Promocoes', 'Categoria Promocoes', 10, 'promocoes');

INSERT IGNORE INTO tamanho (nome, ordem_exibicao) VALUES
    ('RN', 1), ('P', 2), ('M', 3), ('G', 4), ('GG', 5),
    ('2', 6), ('4', 7), ('6', 8), ('8', 9), ('10', 10), ('12', 11), ('14', 12);

INSERT IGNORE INTO cor (nome, codigo_hex, ordem_exibicao) VALUES
    ('Branco', '#FFFFFF', 1),
    ('Azul', '#3B82F6', 2),
    ('Rosa', '#EC4899', 3),
    ('Amarelo', '#FFC000', 4),
    ('Verde', '#4CB896', 5);

INSERT IGNORE INTO marca (nome) VALUES ('ArthurBaby');

INSERT INTO produto (categoria_id, marca_id, codigo, sku, nome, descricao, preco, destaque, promocao, avaliacao)
SELECT (SELECT id FROM categoria ORDER BY id LIMIT 1),
       (SELECT id FROM marca WHERE nome = 'ArthurBaby' LIMIT 1),
       'AB-001', 'AB-001', 'Kit Enxoval Bebe', 'Produto inicial para testes da API', 129.90, 1, 0, 5.0
WHERE NOT EXISTS (SELECT 1 FROM produto WHERE codigo = 'AB-001');

INSERT INTO produto_variacao (produto_id, sku, tamanho_id, cor_id, estoque_atual)
SELECT (SELECT id FROM produto WHERE codigo = 'AB-001' LIMIT 1),
       'AB-001-RN-BR',
       (SELECT id FROM tamanho ORDER BY id LIMIT 1),
       (SELECT id FROM cor ORDER BY id LIMIT 1),
       10
WHERE NOT EXISTS (SELECT 1 FROM produto_variacao WHERE sku = 'AB-001-RN-BR');

INSERT IGNORE INTO cupom (codigo, percentual_desconto, ativo) VALUES ('ARTHUR10', 10.00, 1);

-- Subcategorias de exemplo (categoria "Enxoval")
INSERT IGNORE INTO categoria (categoria_pai_id, nome, descricao, ordem_exibicao)
SELECT (SELECT id FROM categoria WHERE nome = 'Enxoval' LIMIT 1), 'Kit Berco', 'Subcategoria de Enxoval', 1
WHERE EXISTS (SELECT 1 FROM categoria WHERE nome = 'Enxoval');
INSERT IGNORE INTO categoria (categoria_pai_id, nome, descricao, ordem_exibicao)
SELECT (SELECT id FROM categoria WHERE nome = 'Enxoval' LIMIT 1), 'Mantas e Cobertores', 'Subcategoria de Enxoval', 2
WHERE EXISTS (SELECT 1 FROM categoria WHERE nome = 'Enxoval');
INSERT IGNORE INTO categoria (categoria_pai_id, nome, descricao, ordem_exibicao)
SELECT (SELECT id FROM categoria WHERE nome = 'Enxoval' LIMIT 1), 'Toalhas', 'Subcategoria de Enxoval', 3
WHERE EXISTS (SELECT 1 FROM categoria WHERE nome = 'Enxoval');
