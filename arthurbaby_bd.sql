CREATE DATABASE IF NOT EXISTS arthurbaby CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE arthurbaby;

CREATE TABLE usuario (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
nome_completo VARCHAR(150) NOT NULL,
email VARCHAR(150) NOT NULL UNIQUE,
 senha VARCHAR(255) NOT NULL,
 telefone VARCHAR(20), cpf VARCHAR(14) UNIQUE,
 perfil ENUM('ADMINISTRADOR','VENDEDOR','CLIENTE') NOT NULL DEFAULT 'CLIENTE',
 status ENUM('ATIVO','INATIVO','BLOQUEADO') NOT NULL DEFAULT 'ATIVO',
aceite_termo_uso BOOLEAN NOT NULL DEFAULT FALSE,
data_aceite_termo_uso DATETIME NULL,
aceite_lgpd BOOLEAN NOT NULL DEFAULT FALSE,
data_aceite_lgpd DATETIME NULL,
criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
atualizado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE endereco (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
usuario_id BIGINT UNSIGNED NOT NULL,
 cep VARCHAR(9), logradouro VARCHAR(180), numero VARCHAR(20),
 complemento VARCHAR(100), bairro VARCHAR(100), cidade VARCHAR(100), uf CHAR(2),
 referencia VARCHAR(200), principal BOOLEAN NOT NULL DEFAULT FALSE,
 FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE categoria (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
categoria_pai_id BIGINT UNSIGNED NULL,
 nome VARCHAR(100) NOT NULL UNIQUE, descricao VARCHAR(500),
imagem_url VARCHAR(500), ordem_exibicao INT NOT NULL DEFAULT 0,
 status ENUM('ATIVA','INATIVA') NOT NULL DEFAULT 'ATIVA',
criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
atualizado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 FOREIGN KEY (categoria_pai_id) REFERENCES categoria(id) ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE marca (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
 nome VARCHAR(100) NOT NULL UNIQUE,
 status ENUM('ATIVA','INATIVA') NOT NULL DEFAULT 'ATIVA'
);

CREATE TABLE produto (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
categoria_id BIGINT UNSIGNED NOT NULL, marca_id BIGINT UNSIGNED NULL,
codigo VARCHAR(50) NOT NULL UNIQUE, sku VARCHAR(80) NOT NULL UNIQUE,
 nome VARCHAR(180) NOT NULL, descricao TEXT,
preco DECIMAL(12,2) NOT NULL, preco_promocional DECIMAL(12,2) NULL,
 custo DECIMAL(12,2) NULL, peso DECIMAL(10,3) NULL,
 altura DECIMAL(10,2) NULL, largura DECIMAL(10,2) NULL, comprimento DECIMAL(10,2) NULL,
estoque_minimo INT NOT NULL DEFAULT 0,
 status ENUM('ATIVO','INATIVO','ESGOTADO') NOT NULL DEFAULT 'ATIVO',
 destaque BOOLEAN NOT NULL DEFAULT FALSE, promocao BOOLEAN NOT NULL DEFAULT FALSE,
criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
atualizado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 FOREIGN KEY (categoria_id) REFERENCES categoria(id) ON UPDATE CASCADE ON DELETE RESTRICT,
 FOREIGN KEY (marca_id) REFERENCES marca(id) ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE produto_imagem (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, produto_id BIGINT UNSIGNED NOT NULL,
url VARCHAR(500) NOT NULL, descricao VARCHAR(200), ordem_exibicao INT NOT NULL DEFAULT 0,
 principal BOOLEAN NOT NULL DEFAULT FALSE,
 FOREIGN KEY (produto_id) REFERENCES produto(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE tamanho (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, nome VARCHAR(30) NOT NULL UNIQUE,
ordem_exibicao INT NOT NULL DEFAULT 0, status ENUM('ATIVO','INATIVO') NOT NULL DEFAULT 'ATIVO'
);

CREATE TABLE cor (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, nome VARCHAR(50) NOT NULL UNIQUE,
codigo_hex CHAR(7), ordem_exibicao INT NOT NULL DEFAULT 0,
 status ENUM('ATIVA','INATIVA') NOT NULL DEFAULT 'ATIVA'
);

CREATE TABLE modelo (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, nome VARCHAR(80) NOT NULL UNIQUE,
 status ENUM('ATIVO','INATIVO') NOT NULL DEFAULT 'ATIVO'
);

CREATE TABLE produto_variacao (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, produto_id BIGINT UNSIGNED NOT NULL,
tamanho_id BIGINT UNSIGNED NULL, cor_id BIGINT UNSIGNED NULL, modelo_id BIGINT UNSIGNED NULL,
sku VARCHAR(100) NOT NULL UNIQUE, preco DECIMAL(12,2) NULL,
estoque_atual INT NOT NULL DEFAULT 0, estoque_minimo INT NOT NULL DEFAULT 0,
 status ENUM('ATIVA','INATIVA') NOT NULL DEFAULT 'ATIVA',
 FOREIGN KEY (produto_id) REFERENCES produto(id) ON UPDATE CASCADE ON DELETE CASCADE,
 FOREIGN KEY (tamanho_id) REFERENCES tamanho(id) ON UPDATE CASCADE ON DELETE SET NULL,
 FOREIGN KEY (cor_id) REFERENCES cor(id) ON UPDATE CASCADE ON DELETE SET NULL,
 FOREIGN KEY (modelo_id) REFERENCES modelo(id) ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE movimentacao_estoque (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, produto_id BIGINT UNSIGNED NOT NULL,
variacao_id BIGINT UNSIGNED NULL, usuario_id BIGINT UNSIGNED NULL,
 tipo ENUM('ENTRADA','SAIDA','AJUSTE','RESERVA','ESTORNO') NOT NULL,
 quantidade INT NOT NULL, estoque_anterior INT NULL, estoque_posterior INT NULL,
observacao VARCHAR(500), criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY (produto_id) REFERENCES produto(id) ON UPDATE CASCADE ON DELETE RESTRICT,
 FOREIGN KEY (variacao_id) REFERENCES produto_variacao(id) ON UPDATE CASCADE ON DELETE SET NULL,
 FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE pedido (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, numero_pedido VARCHAR(30) NOT NULL UNIQUE,
cliente_id BIGINT UNSIGNED NOT NULL, vendedor_id BIGINT UNSIGNED NULL,
 status ENUM('RASCUNHO','PEDIDO_GERADO','EM_ANALISE','AGUARDANDO_CONFIRMACAO','CONFIRMADO',
 'SEPARANDO_PRODUTOS','PRONTO_PARA_RETIRADA','EM_TRANSPORTE','ENTREGUE','CANCELADO')
 NOT NULL DEFAULT 'PEDIDO_GERADO',
forma_recebimento ENUM('RETIRADA_LOJA','ENTREGA') NOT NULL DEFAULT 'RETIRADA_LOJA',
 subtotal DECIMAL(12,2) NOT NULL DEFAULT 0, desconto DECIMAL(12,2) NOT NULL DEFAULT 0,
 frete DECIMAL(12,2) NOT NULL DEFAULT 0, total DECIMAL(12,2) NOT NULL DEFAULT 0,
observacao TEXT, motivo_cancelamento VARCHAR(500),
criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
atualizado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
confirmado_em DATETIME NULL, cancelado_em DATETIME NULL,
 FOREIGN KEY (cliente_id) REFERENCES usuario(id) ON UPDATE CASCADE ON DELETE RESTRICT,
 FOREIGN KEY (vendedor_id) REFERENCES usuario(id) ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE pedido_item (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, pedido_id BIGINT UNSIGNED NOT NULL,
produto_id BIGINT UNSIGNED NOT NULL, variacao_id BIGINT UNSIGNED NULL,
codigo_produto VARCHAR(50) NOT NULL, nome_produto VARCHAR(180) NOT NULL,
variacao_descricao VARCHAR(255), quantidade INT NOT NULL,
valor_unitario DECIMAL(12,2) NOT NULL, desconto DECIMAL(12,2) NOT NULL DEFAULT 0,
valor_total DECIMAL(12,2) NOT NULL,
 FOREIGN KEY (pedido_id) REFERENCES pedido(id) ON UPDATE CASCADE ON DELETE CASCADE,
 FOREIGN KEY (produto_id) REFERENCES produto(id) ON UPDATE CASCADE ON DELETE RESTRICT,
 FOREIGN KEY (variacao_id) REFERENCES produto_variacao(id) ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE pedido_status_historico (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, pedido_id BIGINT UNSIGNED NOT NULL,
usuario_id BIGINT UNSIGNED NULL, status_anterior VARCHAR(40),
status_novo VARCHAR(40) NOT NULL, observacao VARCHAR(500),
criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY (pedido_id) REFERENCES pedido(id) ON UPDATE CASCADE ON DELETE CASCADE,
 FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE favorito (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, cliente_id BIGINT UNSIGNED NOT NULL,
produto_id BIGINT UNSIGNED NOT NULL, criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY uk_favorito (cliente_id, produto_id),
 FOREIGN KEY (cliente_id) REFERENCES usuario(id) ON UPDATE CASCADE ON DELETE CASCADE,
 FOREIGN KEY (produto_id) REFERENCES produto(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE configuracao_loja (
 id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
nome_fantasia VARCHAR(150) NOT NULL, razao_social VARCHAR(180), cnpj VARCHAR(18),
 telefone VARCHAR(30), whatsapp VARCHAR(30), email VARCHAR(150), email_pedidos VARCHAR(150),
instagram VARCHAR(150), shopee_url VARCHAR(500), facebook_url VARCHAR(500),
 logradouro VARCHAR(180), numero VARCHAR(20), complemento VARCHAR(100), bairro VARCHAR(100),
 cidade VARCHAR(100), uf CHAR(2), cep VARCHAR(9), logo_url VARCHAR(500),
cor_primaria CHAR(7), cor_destaque CHAR(7), cor_acento CHAR(7), cor_fundo CHAR(7),
cor_superficie CHAR(7), cor_texto_primario CHAR(7), cor_texto_secundario CHAR(7),
atualizado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_produto_categoria ON produto(categoria_id);
CREATE INDEX idx_produto_status ON produto(status);
CREATE INDEX idx_produto_nome ON produto(nome);
CREATE INDEX idx_variacao_produto ON produto_variacao(produto_id);
CREATE INDEX idx_pedido_cliente ON pedido(cliente_id);
CREATE INDEX idx_pedido_status ON pedido(status);
CREATE INDEX idx_pedido_data ON pedido(criado_em);
CREATE INDEX idx_item_pedido ON pedido_item(pedido_id);
CREATE INDEX idx_mov_estoque_produto ON movimentacao_estoque(produto_id);

INSERT INTO configuracao_loja
(nome_fantasia,razao_social,cnpj,telefone,whatsapp,email,email_pedidos,instagram,shopee_url,facebook_url,
logradouro,numero,complemento,bairro,cidade,uf,cep,cor_primaria,cor_destaque,cor_acento,cor_fundo,
cor_superficie,cor_texto_primario,cor_texto_secundario)
VALUES
('ARTHURBABY','ARTHUR BABY ENXOVAIS LTDA - ME','35.671.222/0001-10','(71) 99339-9816','(71) 99131-1944',
'arthuradorno13@gmail.com','pedidoababy@gmail.com','@lojao_arthur_baby',
'https://shopee.com.br/arthurbabylojao',
'https://www.facebook.com/p/loj%C3%A3o-Arthur-baby-61590691140950/',
'Avenida Sete de Setembro','548','Edf. Fatima Loja Lj','Centro - Dois de Julho','Salvador','BA','40020-455',
'#0F3B70','#FFC000','#4CB896','#F4F6F8','#FFFFFF','#1E293B','#64748B');

INSERT INTO categoria (nome,descricao,ordem_exibicao) VALUES
('Enxoval','Produtos para composição do enxoval do bebê.',1),
('Roupas para Bebês','Roupas para recém-nascidos e bebês.',2),
('Roupas Infantis','Roupas para crianças.',3),
('Acessórios','Acessórios para bebês e crianças.',4),
('Higiene e Cuidados','Produtos de higiene e cuidados infantis.',5),
('Alimentação','Produtos e acessórios para alimentação infantil.',6),
('Quarto do Bebê','Produtos e acessórios para o quarto do bebê.',7),
('Presentes','Kits e produtos para presentes.',8),
('Kits','Kits e conjuntos de produtos.',9),
('Promoções','Produtos em promoção.',10);

INSERT INTO tamanho (nome,ordem_exibicao) VALUES
('RN',1),('P',2),('M',3),('G',4),('GG',5),('2',6),('4',7),('6',8),('8',9),('10',10),('12',11),('14',12);

INSERT INTO cor (nome,codigo_hex,ordem_exibicao) VALUES
('Branco','#FFFFFF',1),('Azul','#3B82F6',2),('Rosa','#EC4899',3),
('Amarelo','#FFC000',4),('Verde','#4CB896',5),('Bege','#E8D8C3',6),
('Cinza','#64748B',7),('Preto','#000000',8),('Vermelho','#EF4444',9);

