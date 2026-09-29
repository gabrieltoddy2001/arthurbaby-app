/* ============================================
   mock-data.js — banco de dados fictício
   Com persistência em localStorage:
   os dados SOBREVIVEM ao recarregar a página.
   ============================================ */

   const MOCK_STORAGE_KEY = 'arthurbaby_mock_db';

   // Dados iniciais (usados só na primeira vez)
   const MOCK_DB_INICIAL = {
     marcas: [
       { id: 1, nome: 'ArthurBaby', status: 'ATIVO' },
       { id: 2, nome: 'Baby Confort', status: 'ATIVO' },
       { id: 3, nome: 'Kids Fashion', status: 'ATIVO' },
       { id: 4, nome: 'Enxoval Premium', status: 'INATIVO' },
     ],
   
     categorias: [
       { id: 1, nome: 'Enxoval', descricao: 'Produtos para enxoval do bebê', categoriaPaiId: null, status: 'ATIVA' },
       { id: 2, nome: 'Roupas para Bebês', descricao: 'Roupas RN e bebês', categoriaPaiId: null, status: 'ATIVA' },
       { id: 3, nome: 'Roupas Infantis', descricao: 'Roupas para crianças', categoriaPaiId: null, status: 'ATIVA' },
       { id: 4, nome: 'Acessórios', descricao: 'Acessórios diversos', categoriaPaiId: null, status: 'ATIVA' },
       { id: 5, nome: 'Body', descricao: 'Bodies em geral', categoriaPaiId: 2, status: 'ATIVA' },
       { id: 6, nome: 'Macacão', descricao: 'Macacões e conjuntos', categoriaPaiId: 2, status: 'ATIVA' },
       { id: 7, nome: 'Mantas', descricao: 'Mantas e cobertores', categoriaPaiId: 1, status: 'ATIVA' },
     ],
   
     tamanhos: [
       { id: 1, nome: 'RN', ordem: 1, status: 'ATIVO' },
       { id: 2, nome: 'P', ordem: 2, status: 'ATIVO' },
       { id: 3, nome: 'M', ordem: 3, status: 'ATIVO' },
       { id: 4, nome: 'G', ordem: 4, status: 'ATIVO' },
       { id: 5, nome: 'GG', ordem: 5, status: 'ATIVO' },
     ],
   
     cores: [
       { id: 1, nome: 'Branco', codigoHex: '#FFFFFF', status: 'ATIVA' },
       { id: 2, nome: 'Azul Bebê', codigoHex: '#89CFF0', status: 'ATIVA' },
       { id: 3, nome: 'Rosa Bebê', codigoHex: '#F4A6C1', status: 'ATIVA' },
       { id: 4, nome: 'Amarelo', codigoHex: '#FFC000', status: 'ATIVA' },
       { id: 5, nome: 'Verde', codigoHex: '#4CB896', status: 'ATIVA' },
     ],
   
     modelos: [
       { id: 1, nome: 'Tradicional', status: 'ATIVO' },
       { id: 2, nome: 'Ursinho', status: 'ATIVO' },
       { id: 3, nome: 'Floral', status: 'ATIVO' },
       { id: 4, nome: 'Poá', status: 'ATIVO' },
     ],
   
     produtos: [
       {
         id: 1, codigo: 'BABY-001', sku: 'BABY-001-RN-BR',
         nome: 'Body Bebê Manga Curta',
         descricao: 'Body infantil de algodão, confortável e macio para o bebê.',
         preco: 39.90, precoPromocional: 34.90, custo: 20.00,
         peso: 0.100, altura: 2.00, largura: 20.00, comprimento: 25.00,
         estoqueMinimo: 5, status: 'ATIVO', destaque: true, promocao: true,
         categoriaId: 5, marcaId: 1,
         imagens: [], variacoes: [],
       },
       {
         id: 2, codigo: 'BABY-002', sku: 'BABY-002-M-AZ',
         nome: 'Macacão Bebê Ursinho',
         descricao: 'Macacão infantil em algodão com estampa de ursinho.',
         preco: 79.90, precoPromocional: 69.90, custo: 42.00,
         peso: 0.180, altura: 4.00, largura: 22.00, comprimento: 30.00,
         estoqueMinimo: 5, status: 'ATIVO', destaque: true, promocao: true,
         categoriaId: 6, marcaId: 2,
         imagens: [], variacoes: [],
       },
       {
         id: 3, codigo: 'BABY-003', sku: 'BABY-003-UN',
         nome: 'Kit Higiene Bebê',
         descricao: 'Kit com escova, pente e acessórios para cuidados do bebê.',
         preco: 59.90, precoPromocional: null, custo: 30.00,
         peso: 0.300, altura: 8.00, largura: 20.00, comprimento: 25.00,
         estoqueMinimo: 3, status: 'ATIVO', destaque: true, promocao: false,
         categoriaId: 4, marcaId: 1,
         imagens: [], variacoes: [],
       },
       {
         id: 4, codigo: 'KIDS-001', sku: 'KIDS-001-2A-RS',
         nome: 'Conjunto Infantil Feminino',
         descricao: 'Conjunto infantil composto por blusa e short.',
         preco: 89.90, precoPromocional: 79.90, custo: 45.00,
         peso: 0.250, altura: 5.00, largura: 25.00, comprimento: 30.00,
         estoqueMinimo: 5, status: 'ATIVO', destaque: false, promocao: true,
         categoriaId: 3, marcaId: 3,
         imagens: [], variacoes: [],
       },
       {
         id: 5, codigo: 'BABY-005', sku: 'BABY-005-UN',
         nome: 'Manta Bebê Cobertor',
         descricao: 'Manta macia e aconchegante para o bebê.',
         preco: 49.90, precoPromocional: null, custo: 25.00,
         peso: 0.350, altura: 3.00, largura: 30.00, comprimento: 40.00,
         estoqueMinimo: 5, status: 'INATIVO', destaque: false, promocao: false,
         categoriaId: 7, marcaId: 1,
         imagens: [], variacoes: [],
       },
     ],
   
     pedidos: [
        {
          id: 1,
          numeroPedido: 'PED-2026-0001',
          clienteId: 3,
          clienteNome: 'Ana Souza',
          clienteEmail: 'ana.souza@email.com',
          clienteTelefone: '(71) 99999-1001',
          status: 'PEDIDO_GERADO',
          formaRecebimento: 'RETIRADA_LOJA',
          enderecoEntrega: null,
          subtotal: 139.70,
          desconto: 0,
          frete: 0,
          total: 139.70,
          observacao: 'Pedido realizado pelo catálogo da ArthurBaby.',
          motivoCancelamento: null,
          criadoEm: '2026-09-18T10:00:00',
          itens: [
            { produtoId: 1, produtoNome: 'Body Bebê Manga Curta', variacaoDescricao: 'Tamanho RN - Cor Branco - Modelo Tradicional', quantidade: 2, valorUnitario: 39.90, desconto: 0, valorTotal: 79.80 },
            { produtoId: 3, produtoNome: 'Kit Higiene Bebê', variacaoDescricao: 'Tamanho único', quantidade: 1, valorUnitario: 59.90, desconto: 0, valorTotal: 59.90 },
          ],
          historico: [
            { status: 'PEDIDO_GERADO', data: '2026-09-18T10:00:00', usuario: 'Ana Souza', observacao: 'Pedido criado pelo cliente.' },
          ],
        },
        {
          id: 2,
          numeroPedido: 'PED-2026-0002',
          clienteId: 4,
          clienteNome: 'Mariana Santos',
          clienteEmail: 'mariana.santos@email.com',
          clienteTelefone: '(71) 99999-1002',
          status: 'CONFIRMADO',
          formaRecebimento: 'ENTREGA',
          enderecoEntrega: {
            cep: '41830-000',
            logradouro: 'Avenida Oceânica',
            numero: '500',
            complemento: 'Apto 202',
            bairro: 'Pituba',
            cidade: 'Salvador',
            uf: 'BA',
            referencia: 'Próximo ao shopping',
          },
          subtotal: 139.80,
          desconto: 0,
          frete: 15.00,
          total: 154.80,
          observacao: 'Entrega no endereço cadastrado.',
          motivoCancelamento: null,
          criadoEm: '2026-09-18T11:00:00',
          itens: [
            { produtoId: 2, produtoNome: 'Macacão Bebê Ursinho', variacaoDescricao: 'Tamanho M - Cor Azul Bebê - Modelo Ursinho', quantidade: 1, valorUnitario: 79.90, desconto: 0, valorTotal: 79.90 },
            { produtoId: 3, produtoNome: 'Kit Higiene Bebê', variacaoDescricao: 'Tamanho único', quantidade: 1, valorUnitario: 59.90, desconto: 0, valorTotal: 59.90 },
          ],
          historico: [
            { status: 'PEDIDO_GERADO', data: '2026-09-18T11:00:00', usuario: 'Mariana Santos', observacao: 'Pedido criado pelo cliente.' },
            { status: 'CONFIRMADO', data: '2026-09-18T11:15:00', usuario: 'Carlos Almeida', observacao: 'Pedido confirmado pelo vendedor.' },
          ],
        },
        {
          id: 3,
          numeroPedido: 'PED-2026-0003',
          clienteId: 3,
          clienteNome: 'Ana Souza',
          clienteEmail: 'ana.souza@email.com',
          clienteTelefone: '(71) 99999-1001',
          status: 'ENTREGUE',
          formaRecebimento: 'RETIRADA_LOJA',
          enderecoEntrega: null,
          subtotal: 79.90,
          desconto: 5.00,
          frete: 0,
          total: 74.90,
          observacao: 'Cliente retirou pessoalmente.',
          motivoCancelamento: null,
          criadoEm: '2026-09-15T14:30:00',
          itens: [
            { produtoId: 1, produtoNome: 'Body Bebê Manga Curta', variacaoDescricao: 'Tamanho P - Cor Azul Bebê', quantidade: 2, valorUnitario: 39.90, desconto: 5.00, valorTotal: 74.80 },
          ],
          historico: [
            { status: 'PEDIDO_GERADO', data: '2026-09-15T14:30:00', usuario: 'Ana Souza', observacao: 'Pedido criado.' },
            { status: 'CONFIRMADO', data: '2026-09-15T14:35:00', usuario: 'Carlos Almeida', observacao: 'Confirmado.' },
            { status: 'PRONTO_PARA_RETIRADA', data: '2026-09-15T16:00:00', usuario: 'Carlos Almeida', observacao: 'Separado.' },
            { status: 'ENTREGUE', data: '2026-09-16T09:00:00', usuario: 'Carlos Almeida', observacao: 'Cliente retirou na loja.' },
          ],
        },
        {
          id: 4,
          numeroPedido: 'PED-2026-0004',
          clienteId: 4,
          clienteNome: 'Mariana Santos',
          clienteEmail: 'mariana.santos@email.com',
          clienteTelefone: '(71) 99999-1002',
          status: 'CANCELADO',
          formaRecebimento: 'ENTREGA',
          enderecoEntrega: {
            cep: '41830-000',
            logradouro: 'Avenida Oceânica',
            numero: '500',
            complemento: 'Apto 202',
            bairro: 'Pituba',
            cidade: 'Salvador',
            uf: 'BA',
            referencia: 'Próximo ao shopping',
          },
          subtotal: 89.90,
          desconto: 0,
          frete: 10.00,
          total: 99.90,
          observacao: null,
          motivoCancelamento: 'Cliente desistiu da compra.',
          criadoEm: '2026-09-14T10:00:00',
          itens: [
            { produtoId: 4, produtoNome: 'Conjunto Infantil Feminino', variacaoDescricao: 'Tamanho 2 anos', quantidade: 1, valorUnitario: 89.90, desconto: 0, valorTotal: 89.90 },
          ],
          historico: [
            { status: 'PEDIDO_GERADO', data: '2026-09-14T10:00:00', usuario: 'Mariana Santos', observacao: 'Pedido criado.' },
            { status: 'EM_ANALISE', data: '2026-09-14T10:30:00', usuario: 'Carlos Almeida', observacao: 'Em análise.' },
            { status: 'CANCELADO', data: '2026-09-14T15:00:00', usuario: 'Administrador ArthurBaby', observacao: 'Cancelado: Cliente desistiu da compra.' },
          ],
        },
      ],
   
      usuarios: [
        // ===== ADMINISTRADOR E VENDEDOR =====
        {
          id: 1, nome: 'Administrador ArthurBaby', email: 'admin@arthurbaby.com.br',
          cpf: '111.111.111-11', telefone: '(71) 99999-0001',
          perfil: 'ADMINISTRADOR', status: 'ATIVO',
          criadoEm: '2026-01-01T00:00:00',
          enderecos: [],
        },
        {
          id: 2, nome: 'Carlos Almeida', email: 'vendedor@arthurbaby.com.br',
          cpf: '222.222.222-22', telefone: '(71) 99999-0002',
          perfil: 'VENDEDOR', status: 'ATIVO',
          criadoEm: '2026-01-01T00:00:00',
          enderecos: [],
        },
    
        // ===== CLIENTES =====
        {
          id: 3, nome: 'Ana Souza', email: 'ana.souza@email.com',
          cpf: '333.333.333-33', telefone: '(71) 99999-1001',
          perfil: 'CLIENTE', status: 'ATIVO',
          criadoEm: '2026-03-10T10:00:00',
          enderecos: [
            {
              cep: '40100-000', logradouro: 'Rua das Flores', numero: '100',
              complemento: 'Casa', bairro: 'Centro', cidade: 'Salvador', uf: 'BA',
              referencia: 'Próximo à praça principal', principal: true,
            },
          ],
        },
        {
          id: 4, nome: 'Mariana Santos', email: 'mariana.santos@email.com',
          cpf: '444.444.444-44', telefone: '(71) 99999-1002',
          perfil: 'CLIENTE', status: 'ATIVO',
          criadoEm: '2026-04-22T14:00:00',
          enderecos: [
            {
              cep: '41830-000', logradouro: 'Avenida Oceânica', numero: '500',
              complemento: 'Apto 202', bairro: 'Pituba', cidade: 'Salvador', uf: 'BA',
              referencia: 'Próximo ao shopping', principal: true,
            },
          ],
        },
        {
          id: 5, nome: 'Juliana Costa', email: 'juliana.costa@email.com',
          cpf: '555.555.555-55', telefone: '(71) 99999-1003',
          perfil: 'CLIENTE', status: 'ATIVO',
          criadoEm: '2026-05-15T09:30:00',
          enderecos: [
            {
              cep: '40280-000', logradouro: 'Rua Chile', numero: '25',
              complemento: null, bairro: 'Comércio', cidade: 'Salvador', uf: 'BA',
              referencia: null, principal: true,
            },
          ],
        },
        {
          id: 6, nome: 'Pedro Oliveira', email: 'pedro.oliveira@email.com',
          cpf: '666.666.666-66', telefone: '(71) 99999-1004',
          perfil: 'CLIENTE', status: 'BLOQUEADO',
          criadoEm: '2026-06-08T11:45:00',
          enderecos: [
            {
              cep: '41950-000', logradouro: 'Rua da Paciência', numero: '310',
              complemento: null, bairro: 'Rio Vermelho', cidade: 'Salvador', uf: 'BA',
              referencia: null, principal: true,
            },
          ],
        },
        {
          id: 7, nome: 'Camila Ferreira', email: 'camila.ferreira@email.com',
          cpf: '777.777.777-77', telefone: '(71) 99999-1005',
          perfil: 'CLIENTE', status: 'ATIVO',
          criadoEm: '2026-07-19T16:20:00',
          enderecos: [],
        },
      ],
   
     movimentacoes: [
        { id: 1, produtoId: 1, produtoNome: 'Body Bebê Manga Curta', variacaoId: null, tipo: 'ENTRADA', quantidade: 30, estoqueAnterior: 0, estoquePosterior: 30, usuario: 'Administrador ArthurBaby', observacao: 'Entrada inicial de estoque.', data: '2026-09-15T09:00:00' },
        { id: 2, produtoId: 1, produtoNome: 'Body Bebê Manga Curta', variacaoId: null, tipo: 'SAIDA', quantidade: 2, estoqueAnterior: 30, estoquePosterior: 28, usuario: 'Carlos Almeida', observacao: 'Saída referente ao pedido PED-2026-0001.', data: '2026-09-18T10:05:00' },
        { id: 3, produtoId: 2, produtoNome: 'Macacão Bebê Ursinho', variacaoId: null, tipo: 'ENTRADA', quantidade: 20, estoqueAnterior: 0, estoquePosterior: 20, usuario: 'Administrador ArthurBaby', observacao: 'Entrada inicial de estoque.', data: '2026-09-15T09:10:00' },
        { id: 4, produtoId: 3, produtoNome: 'Kit Higiene Bebê', variacaoId: null, tipo: 'ENTRADA', quantidade: 15, estoqueAnterior: 0, estoquePosterior: 15, usuario: 'Administrador ArthurBaby', observacao: 'Entrada inicial de estoque.', data: '2026-09-15T09:20:00' },
        { id: 5, produtoId: 4, produtoNome: 'Conjunto Infantil Feminino', variacaoId: null, tipo: 'ENTRADA', quantidade: 10, estoqueAnterior: 0, estoquePosterior: 10, usuario: 'Administrador ArthurBaby', observacao: 'Entrada inicial de estoque.', data: '2026-09-15T09:30:00' },
        { id: 6, produtoId: 1, produtoNome: 'Body Bebê Manga Curta', variacaoId: null, tipo: 'AJUSTE', quantidade: -2, estoqueAnterior: 28, estoquePosterior: 26, usuario: 'Administrador ArthurBaby', observacao: 'Ajuste após inventário.', data: '2026-09-17T14:00:00' },
        { id: 7, produtoId: 5, produtoNome: 'Manta Bebê Cobertor', variacaoId: null, tipo: 'ENTRADA', quantidade: 8, estoqueAnterior: 0, estoquePosterior: 8, usuario: 'Administrador ArthurBaby', observacao: 'Entrada inicial de estoque.', data: '2026-09-15T09:40:00' },
      ],
   
     configuracoes: {
       nomeFantasia: 'ARTHURBABY',
       razaoSocial: 'ARTHUR BABY ENXOVAIAS LTDA - ME',
       cnpj: '35.671.222/0001-10',
       telefone: '(71) 99339-9816',
       whatsapp: '(71) 99131-1944',
       email: 'arthuradorno13@gmail.com',
       emailPedidos: 'pedidoababy@gmail.com',
       instagram: '@lojao_arthur_baby',
       shopeeUrl: 'https://shopee.com.br/arthurbabylojao',
       facebookUrl: 'https://www.facebook.com/p/lojão-Arthur-baby',
       logradouro: 'Avenida Sete de Setembro',
       numero: '548',
       complemento: 'Ed. Fatima Loja',
       bairro: 'Centro - Dois de Julho',
       cidade: 'Salvador',
       uf: 'BA',
       cep: '40020-455',
       corPrimaria: '#37B6B0',
       corDestaque: '#ED83A4',
     },
   
     auditoria: [
        { id: 1, usuario: 'Administrador ArthurBaby', acao: 'CRIAR', entidade: 'Produto', entidadeId: 1, descricao: 'Criou o produto Body Bebê Manga Curta', data: '2026-09-15T09:00:00' },
        { id: 2, usuario: 'Administrador ArthurBaby', acao: 'CRIAR', entidade: 'Produto', entidadeId: 2, descricao: 'Criou o produto Macacão Bebê Ursinho', data: '2026-09-15T09:10:00' },
        { id: 3, usuario: 'Administrador ArthurBaby', acao: 'CRIAR', entidade: 'Produto', entidadeId: 3, descricao: 'Criou o produto Kit Higiene Bebê', data: '2026-09-15T09:20:00' },
        { id: 4, usuario: 'Administrador ArthurBaby', acao: 'CRIAR', entidade: 'Categoria', entidadeId: 5, descricao: 'Criou a subcategoria Body', data: '2026-09-15T10:00:00' },
        { id: 5, usuario: 'Carlos Almeida', acao: 'ATUALIZAR', entidade: 'Pedido', entidadeId: 2, descricao: 'Alterou status do pedido PED-2026-0002 para CONFIRMADO', data: '2026-09-18T11:15:00' },
        { id: 6, usuario: 'Administrador ArthurBaby', acao: 'RESETAR_SENHA', entidade: 'Usuario', entidadeId: 5, descricao: 'Resetou a senha do cliente Juliana Costa', data: '2026-09-18T14:00:00' },
        { id: 7, usuario: 'Administrador ArthurBaby', acao: 'BLOQUEAR', entidade: 'Usuario', entidadeId: 6, descricao: 'Bloqueou o cliente Pedro Oliveira', data: '2026-09-18T14:30:00' },
        { id: 8, usuario: 'Carlos Almeida', acao: 'ATUALIZAR', entidade: 'MovimentacaoEstoque', entidadeId: 2, descricao: 'Registrou SAIDA de 2 unidades do Body Bebê Manga Curta', data: '2026-09-18T10:05:00' },
        { id: 9, usuario: 'Administrador ArthurBaby', acao: 'CANCELAR', entidade: 'Pedido', entidadeId: 4, descricao: 'Cancelou o pedido PED-2026-0004. Motivo: Cliente desistiu da compra.', data: '2026-09-14T15:00:00' },
        { id: 10, usuario: 'Administrador ArthurBaby', acao: 'ATUALIZAR', entidade: 'ConfiguracaoLoja', entidadeId: 1, descricao: 'Atualizou as configurações da loja', data: '2026-09-13T16:00:00' },
        { id: 11, usuario: 'Administrador ArthurBaby', acao: 'CRIAR', entidade: 'Marca', entidadeId: 3, descricao: 'Criou a marca Kids Fashion', data: '2026-09-12T11:00:00' },
        { id: 12, usuario: 'Carlos Almeida', acao: 'ATUALIZAR', entidade: 'Pedido', entidadeId: 3, descricao: 'Alterou status do pedido PED-2026-0003 para ENTREGUE', data: '2026-09-16T09:00:00' },
      ],
   };
   
   let MOCK_PROXIMO_ID = 1000;
   
   /* ============================================
      Carrega do localStorage (se existir)
      ============================================ */
   let MOCK_DB;
   (function carregarMock() {
     try {
       const salvo = localStorage.getItem(MOCK_STORAGE_KEY);
       if (salvo) {
         const parsed = JSON.parse(salvo);
         MOCK_DB = parsed.db || JSON.parse(JSON.stringify(MOCK_DB_INICIAL));
         MOCK_PROXIMO_ID = parsed.proximoId || 1000;
         return;
       }
     } catch (e) {
       console.warn('Falha ao carregar mock salvo. Usando dados iniciais.', e);
     }
     MOCK_DB = JSON.parse(JSON.stringify(MOCK_DB_INICIAL));
   })();
   
   /* ============================================
      Salva o estado atual no localStorage
      ============================================ */
   function salvarMock() {
     try {
       localStorage.setItem(MOCK_STORAGE_KEY, JSON.stringify({
         db: MOCK_DB,
         proximoId: MOCK_PROXIMO_ID,
       }));
     } catch (e) {
       console.warn('Falha ao salvar mock.', e);
     }
   }
   
   /* ============================================
      Reseta o mock pros dados iniciais
      Uso: resetarMock() no console do navegador
      ============================================ */
   function resetarMock() {
     if (confirm('Apagar TODAS as alterações e voltar aos dados iniciais?')) {
       localStorage.removeItem(MOCK_STORAGE_KEY);
       location.reload();
     }
   }
   
   /* ============================================
      Gera novo ID
      ============================================ */
   function mockNovoId() {
     return MOCK_PROXIMO_ID++;
   }