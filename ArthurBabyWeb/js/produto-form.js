
/* ============================================
   produto-form.js — criar/editar produto
   Com imagens e variações (Nível 3 completo)
   ============================================ */

   let categorias = [];
   let marcas = [];
   let tamanhos = [];
   let cores = [];
   let modelos = [];
   let editando = null;
   let imagens = [];
   let variacoes = [];

   function getIdUrl() {
     const params = new URLSearchParams(window.location.search);
     const id = params.get('id');
     return id ? Number(id) : null;
   }

   async function init() {
     const usuario = exigirLogin();
     if (!usuario) return;

     const idUrl = getIdUrl();

     try {
       const [cats, marcs, tams, cors, mods, produto] = await Promise.all([
         adminApi.listar('categorias'),
         adminApi.listar('marcas'),
         adminApi.listar('tamanhos'),
         adminApi.listar('cores'),
         adminApi.listar('modelos'),
         idUrl ? adminApi.buscar('produtos', idUrl) : Promise.resolve(null),
       ]);

       categorias = cats;
       marcas = marcs;
       tamanhos = tams.sort((a, b) => (a.ordemExibicao ?? 0) - (b.ordemExibicao ?? 0));
       cores = cors.sort((a, b) => (a.ordemExibicao ?? 0) - (b.ordemExibicao ?? 0));
       modelos = mods;

       if (idUrl) {
         editando = produto || null;
         if (!editando) throw new Error('Produto não encontrado');

         editando.categoriaId = editando.categoria?.id ?? editando.categoriaId ?? null;
         editando.marcaId = editando.marca?.id ?? editando.marcaId ?? null;

         imagens = prepararImagensIniciais(editando.imagens);

         variacoes = (editando.variacoes || []).map(v => ({
           ...v,
           tamanhoId: v.tamanho?.id ?? v.tamanhoId ?? null,
           corId: v.cor?.id ?? v.corId ?? null,
           modeloId: v.modelo?.id ?? v.modeloId ?? null,
         }));
       }

       renderForm();

       if (new URLSearchParams(window.location.search).get('salvo')) {
         const a = document.getElementById('alerta-form');
         a.className = 'alert alert-sucesso';
         a.textContent = 'Produto salvo. Preencha os dados do próximo.';
         setTimeout(() => a.classList.add('hidden'), 4000);
       }
     } catch (err) {
       document.getElementById('conteudo').innerHTML =
         `<div class="alert alert-erro">Erro ao carregar: ${escapeHtml(err.message)}</div>`;
     }
   }

   function renderForm() {
     const conteudo = document.getElementById('conteudo');
     const titulo = editando ? 'Editar produto' : 'Novo produto';
     const p = editando || {};

     // Subcategorias aparecem como "Pai > Filha".
     const nomeCategoria = (c) => {
       const pai = c.categoriaPaiId
         ? categorias.find(x => x.id === c.categoriaPaiId)
         : null;

       return pai ? `${pai.nome} › ${c.nome}` : c.nome;
     };

     const optCat = categorias
       .filter(c => c.status === 'ATIVA' || p.categoriaId === c.id)
       .sort((a, b) => nomeCategoria(a).localeCompare(nomeCategoria(b), 'pt-BR'))
       .map(c => `
         <option value="${c.id}" ${p.categoriaId === c.id ? 'selected' : ''}>
           ${escapeHtml(nomeCategoria(c))}${c.status !== 'ATIVA' ? ' (inativa)' : ''}
         </option>
       `)
       .join('');

     // Marca atual: mantém visível mesmo se estiver inativa.
     const marcaAtual = marcas.find(m => m.id === p.marcaId);
     const nomeMarcaAtual = p.marcaNome || marcaAtual?.nome || p.marca?.nome || '';

     // Marcas ativas disponíveis para seleção.
     const opcoesMarca = marcas
       .filter(m => m.status === 'ATIVA' || p.marcaId === m.id)
       .map(m => `
         <option value="${escapeAttr(m.nome)}">
           ${escapeHtml(m.nome)}${m.status !== 'ATIVA' ? ' (inativa)' : ''}
         </option>
       `)
       .join('');

     conteudo.innerHTML = `
       <div class="form-produto form-compacto">
         <div class="form-header">
           <div>
             <h1>${titulo}</h1>
             ${editando
               ? `<p class="texto-sec">Código ${escapeHtml(p.codigo)} · cadastrado em ${p.criadoEm ? new Date(p.criadoEm).toLocaleDateString('pt-BR') : '—'}</p>`
               : '<p class="texto-sec">Campos com * são obrigatórios.</p>'}
           </div>

           <div>
             <button class="btn btn-ghost" onclick="voltar()">Cancelar</button>
             ${!editando ? `<button class="btn btn-accent" onclick="salvar(true)" id="btn-salvar-outro">Salvar e cadastrar outro</button>` : ''}
             <button class="btn btn-primary" onclick="salvar()" id="btn-salvar">Salvar</button>
           </div>
         </div>

         <div id="alerta-form" class="alert alert-erro hidden"></div>

         <div class="card">
           <h3 class="secao-titulo">Dados básicos e classificação</h3>

           <div class="grid-3">
             <div class="field">
               <label for="f-nome">Nome *</label>
               <input type="text" id="f-nome" maxlength="180"
                      value="${escapeAttr(p.nome || '')}"
                      placeholder="Ex.: Body Manga Curta Ursinho">
             </div>

             <div class="field">
               <label for="f-codigo">Código * <span class="texto-mini">(único, gera o SKU)</span></label>
               <input type="text" id="f-codigo" maxlength="50"
                      value="${escapeAttr(p.codigo || '')}"
                      placeholder="Ex.: AB-001"
                      oninput="sugerirSku()">
             </div>

             <div class="field">
               <label for="f-sku">SKU principal *</label>
               <input type="text" id="f-sku" maxlength="80"
                      value="${escapeAttr(p.sku || '')}"
                      oninput="this.dataset.manual = this.value ? '1' : ''"
                      data-manual="${p.sku ? '1' : ''}">
             </div>

             <div class="field">
               <label for="f-categoria">Categoria *</label>
               <select id="f-categoria">
                 <option value="">Selecione...</option>
                 ${optCat}
               </select>
             </div>

             <div class="field">
               <label for="f-marca">Marca</label>
               <input
                 type="text"
                 id="f-marca"
                 list="lista-marcas"
                 maxlength="100"
                 value="${escapeAttr(nomeMarcaAtual)}"
                 placeholder="Selecione ou digite uma nova marca"
               >
               <datalist id="lista-marcas">
                 ${opcoesMarca}
               </datalist>
               <small class="texto-mini">
                 Selecione uma marca existente ou digite uma nova.
               </small>
             </div>

             <div class="field">
               <label for="f-status" title="ATIVO sem estoque vira ESGOTADO automaticamente, e volta a ATIVO quando o estoque é reposto. INATIVO nunca muda sozinho.">
                 Status <span class="texto-mini">(Esgotado é automático)</span>
               </label>
               <select id="f-status">
                 <option value="ATIVO" ${p.status === 'ATIVO' || !p.status ? 'selected' : ''}>ATIVO</option>
                 <option value="INATIVO" ${p.status === 'INATIVO' ? 'selected' : ''}>INATIVO</option>
                 <option value="ESGOTADO" ${p.status === 'ESGOTADO' ? 'selected' : ''}>ESGOTADO</option>
               </select>
             </div>
           </div>

           <div class="field" style="margin-bottom:0;">
             <label for="f-descricao">Descrição <span class="texto-mini" id="cont-desc"></span></label>
             <textarea id="f-descricao" rows="3" maxlength="2000"
                       placeholder="Material, cuidados, indicação de idade... (aparece no app)"
                       oninput="contarDescricao()">${escapeHtml(p.descricao || '')}</textarea>
           </div>
         </div>

         <div class="linha-cards">
           <div class="card">
             <h3 class="secao-titulo">Preços</h3>

             <div class="grid-3">
               <div class="field">
                 <label for="f-preco">Venda * (R$)</label>
                 <input type="number" step="0.01" min="0" id="f-preco"
                        value="${valorDinheiro(p.preco)}" oninput="atualizarMargem()">
               </div>

               <div class="field">
                 <label for="f-promo" title="Quando preenchido, é o preço cobrado no pedido">Promocional (R$)</label>
                 <input type="number" step="0.01" min="0" id="f-promo"
                        value="${valorDinheiro(p.precoPromocional)}"
                        oninput="aoDigitarPromo(); atualizarMargem()">
               </div>

               <div class="field">
                 <label for="f-custo">Custo (R$)</label>
                 <input type="number" step="0.01" min="0" id="f-custo"
                        value="${valorDinheiro(p.custo)}" oninput="atualizarMargem()">
               </div>
             </div>

             <div id="margem" class="margem-box"></div>

             <div class="checkboxes">
               <label><input type="checkbox" id="f-destaque" ${p.destaque ? 'checked' : ''}> Destaque na home</label>
               <label><input type="checkbox" id="f-promocao" ${p.promocao ? 'checked' : ''}> Em promoção</label>
             </div>
           </div>

           <div class="card">
             <h3 class="secao-titulo">Dimensões e estoque mínimo</h3>

             <div class="grid-5">
               <div class="field">
                 <label for="f-peso">Peso (kg)</label>
                 <input type="number" step="0.001" min="0" id="f-peso" value="${p.peso ?? ''}">
               </div>

               <div class="field">
                 <label for="f-altura">Altura (cm)</label>
                 <input type="number" step="0.01" min="0" id="f-altura" value="${p.altura ?? ''}">
               </div>

               <div class="field">
                 <label for="f-largura">Largura (cm)</label>
                 <input type="number" step="0.01" min="0" id="f-largura" value="${p.largura ?? ''}">
               </div>

               <div class="field">
                 <label for="f-comprimento">Comprim. (cm)</label>
                 <input type="number" step="0.01" min="0" id="f-comprimento" value="${p.comprimento ?? ''}">
               </div>

               <div class="field">
                 <label for="f-est-min" title="Alerta de estoque baixo no painel">Estoque mín.</label>
                 <input type="number" min="0" id="f-est-min" value="${p.estoqueMinimo ?? 0}">
               </div>
             </div>
           </div>
         </div>

         <div class="card" id="card-imagens">
           <div class="secao-header">
             <h3 class="secao-titulo">Imagens</h3>
           </div>

           <p class="texto-sec" style="margin:.5rem 0">
             Informe um link ou o caminho do arquivo, ou deixe em branco e clique em "Escolher fotos".
             Também pode arrastar fotos para cá ou colar com Ctrl+V. JPG, PNG, WEBP ou GIF; fotos grandes são reduzidas automaticamente.
           </p>

           <div class="img-link-linha">
             <input type="text" id="img-link" autocomplete="off" spellcheck="false"
                    placeholder="Link (https://...) ou caminho do arquivo (C:\\Fotos\\produto.jpg)"
                    oninput="atualizarBotaoImagem()"
                    onkeydown="if (event.key === 'Enter') { event.preventDefault(); acaoBotaoImagem(); }">
             <button type="button" class="btn btn-accent btn-sm" id="btn-imagem-acao" onclick="acaoBotaoImagem()">Escolher fotos</button>
             <input id="arquivos-imagem" type="file"
                    accept="image/jpeg,image/png,image/webp,image/gif"
                    multiple hidden onchange="enviarImagens(this)">
           </div>

           <div id="status-upload-imagem" class="img-status" role="status" aria-live="polite"></div>
           <div id="lista-imagens"></div>
         </div>

         <div class="card">
           <div class="secao-header">
             <h3 class="secao-titulo">Variações (tamanho + cor + modelo)</h3>

             <div style="display:flex;gap:.5rem;flex-wrap:wrap;">
               <button type="button" class="btn btn-ghost btn-sm" onclick="alternarGerador()">Gerar combinações</button>
               <button type="button" class="btn btn-accent btn-sm" onclick="adicionarVariacao()">+ Adicionar variação</button>
             </div>
           </div>

           <div id="gerador" class="gerador-variacoes hidden">
             <strong style="font-size:.9rem;">Gerar variações a partir de tamanhos × cores</strong>

             <div class="texto-mini" style="margin-top:.35rem;">Tamanhos</div>
             <div class="chips">
               ${tamanhos.filter(t => t.status === 'ATIVO').map(t => `
                 <label class="chip">
                   <input type="checkbox" class="g-tam" value="${t.id}"
                          onchange="this.parentElement.classList.toggle('marcado', this.checked)">
                   ${escapeHtml(t.nome)}
                 </label>
               `).join('') || '<span class="texto-mini">Nenhum tamanho ativo</span>'}
             </div>

             <div class="texto-mini">Cores</div>
             <div class="chips">
               ${cores.filter(c => c.status === 'ATIVA').map(c => `
                 <label class="chip">
                   <input type="checkbox" class="g-cor" value="${c.id}"
                          onchange="this.parentElement.classList.toggle('marcado', this.checked)">
                   ${c.codigoHex ? `<span class="cor-amostra" style="background:${escapeAttr(c.codigoHex)}"></span>` : ''}
                   ${escapeHtml(c.nome)}
                 </label>
               `).join('') || '<span class="texto-mini">Nenhuma cor ativa</span>'}
             </div>

             <div class="grid-3" style="align-items:end;">
               <div class="field" style="margin-bottom:0;">
                 <label for="g-modelo">Modelo (opcional)</label>
                 <select id="g-modelo">
                   <option value="">—</option>
                   ${modelos.filter(m => m.status === 'ATIVO').map(m => `<option value="${m.id}">${escapeHtml(m.nome)}</option>`).join('')}
                 </select>
               </div>

               <div class="field" style="margin-bottom:0;">
                 <label for="g-estoque">Estoque inicial de cada</label>
                 <input type="number" id="g-estoque" min="0" value="0">
               </div>

               <button type="button" class="btn btn-primary btn-sm" onclick="gerarVariacoes()">Gerar</button>
             </div>

             <div class="dica">
               O SKU é montado com o código do produto + tamanho + cor.
               Combinações já existentes são ignoradas.
             </div>
           </div>

           <div id="lista-variacoes"></div>
         </div>
       </div>
     `;

     renderImagens();
     configurarZonaImagens();
     renderVariacoes();
     atualizarMargem();
     contarDescricao();
     marcarSujo(false);

     document.querySelector('.form-produto').addEventListener('input', () => marcarSujo(true));
     document.querySelector('.form-produto').addEventListener('change', () => marcarSujo(true));
   }

   /* ============================================
      AJUDAS DO FORMULÁRIO
      ============================================ */

   let formSujo = false;

   function marcarSujo(v) {
     formSujo = v;
   }

   window.addEventListener('beforeunload', (e) => {
     if (formSujo) {
       e.preventDefault();
       e.returnValue = '';
     }
   });

   function contarDescricao() {
     const t = document.getElementById('f-descricao');
     const c = document.getElementById('cont-desc');

     if (t && c) c.textContent = `(${t.value.length}/2000)`;
   }

   function slug(s) {
     return String(s || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '')
       .toUpperCase().replace(/[^A-Z0-9]+/g, '-').replace(/^-|-$/g, '');
   }

   function sugerirSku() {
     const sku = document.getElementById('f-sku');
     if (!sku.dataset.manual) {
       sku.value = slug(document.getElementById('f-codigo').value);
     }
   }

   function aoDigitarPromo() {
     const promo = document.getElementById('f-promo').value;
     const chk = document.getElementById('f-promocao');

     if (chk && promo && Number(promo) > 0) chk.checked = true;
   }

   function atualizarMargem() {
     const box = document.getElementById('margem');
     if (!box) return;

     const preco = Number(document.getElementById('f-preco').value) || 0;
     const promo = Number(document.getElementById('f-promo').value) || 0;
     const custo = Number(document.getElementById('f-custo').value) || 0;
     const venda = promo > 0 ? promo : preco;

     box.className = 'margem-box';

     if (promo > 0 && preco > 0 && promo >= preco) {
       box.classList.add('ruim');
       box.textContent = 'O preço promocional deve ser menor que o preço de venda.';
       return;
     }

     if (!venda || !custo) {
       box.textContent = 'Informe preço e custo para ver a margem de lucro.';
       return;
     }

     const lucro = venda - custo;
     const margem = (lucro / venda) * 100;
     const markup = venda / custo;

     box.classList.add(lucro <= 0 ? 'ruim' : (margem >= 30 ? 'boa' : ''));

     box.innerHTML = `
       Vendendo por <strong>R$ ${venda.toFixed(2).replace('.', ',')}</strong>${promo > 0 ? ' (promocional)' : ''}:
       lucro de <strong>R$ ${lucro.toFixed(2).replace('.', ',')}</strong> por unidade ·
       margem <strong>${margem.toFixed(1).replace('.', ',')}%</strong> ·
       markup <strong>${markup.toFixed(2).replace('.', ',')}x</strong>
       ${promo > 0 ? ` · desconto de ${(((preco - promo) / preco) * 100).toFixed(0)}%` : ''}
       ${lucro <= 0 ? ' — atenção: venda com prejuízo!' : ''}
     `;
   }

   function alternarGerador() {
     document.getElementById('gerador').classList.toggle('hidden');
   }

   function gerarVariacoes() {
     const tamIds = [...document.querySelectorAll('.g-tam:checked')].map(i => Number(i.value));
     const corIds = [...document.querySelectorAll('.g-cor:checked')].map(i => Number(i.value));
     const modeloId = Number(document.getElementById('g-modelo').value) || null;
     const estoque = Math.max(0, parseInt(document.getElementById('g-estoque').value, 10) || 0);

     if (!tamIds.length && !corIds.length) {
       alert('Selecione ao menos um tamanho ou uma cor.');
       return;
     }

     const codigo = slug(document.getElementById('f-codigo').value) || 'PROD';
     const ts = tamIds.length ? tamIds : [null];
     const cs = corIds.length ? corIds : [null];

     let criadas = 0;

     ts.forEach(tId => cs.forEach(cId => {
       const existe = variacoes.some(v =>
         (v.tamanhoId ?? null) === tId &&
         (v.corId ?? null) === cId &&
         (v.modeloId ?? null) === modeloId
       );

       if (existe) return;

       const tNome = tamanhos.find(t => t.id === tId)?.nome;
       const cNome = cores.find(c => c.id === cId)?.nome;

       let sku = [codigo, slug(tNome), slug(cNome)].filter(Boolean).join('-');
       let n = 2;

       while (variacoes.some(v => (v.sku || '').toUpperCase() === sku)) {
         sku = `${sku}-${n++}`;
       }

       variacoes.push({
         tamanhoId: tId,
         corId: cId,
         modeloId,
         sku,
         preco: null,
         estoqueAtual: estoque,
         estoqueMinimo: 0,
       });

       criadas++;
     }));

     renderVariacoes();
     marcarSujo(true);

     alert(criadas ? `${criadas} variação(ões) gerada(s).` : 'Todas as combinações já existiam.');
   }

   /* ============================================
      MARCAS
      ============================================ */

   async function resolverMarcaId() {
     const campo = document.getElementById('f-marca');
     const nome = campo.value.trim().replace(/\s+/g, ' ');

     if (!nome) return null;

     // Procura uma marca existente, ignorando maiúsculas e minúsculas.
     const existente = marcas.find(
       m => m.nome.trim().toLowerCase() === nome.toLowerCase()
     );

     if (existente) {
       const marcaAtualId = editando?.marcaId ?? null;

       // Permite manter a marca inativa já vinculada ao produto.
       if (existente.status !== 'ATIVA' && existente.id !== marcaAtualId) {
         throw new Error('Essa marca está inativa. Escolha uma marca ativa.');
       }

       return existente.id;
     }

     // Solicita à API o cadastro ou recuperação da marca.
     const resultado = await apiJson('/admin/marcas/obter-ou-criar', {
       method: 'POST',
       body: JSON.stringify({ nome }),
     }, 'Não foi possível cadastrar a marca.');

     if (!resultado?.id) {
       throw new Error('A API não retornou um identificador válido para a marca.');
     }

     // Atualiza a lista local.
     const novaMarca = {
       id: resultado.id,
       nome: resultado.nome || nome,
       status: resultado.status || 'ATIVA',
     };

     marcas.push(novaMarca);

     return novaMarca.id;
   }

   /* ============================================
      IMAGENS
      Cada item de `imagens` é { id?, url, descricao, principal } + campos de tela (começam com _):
      _uid (chave estável), _preview (blob local enquanto envia), _status ('enviando' | 'erro'),
      _progresso (0-100), _erro (mensagem), _arquivo (para tentar de novo).
      A ordem do array é a ordem de exibição salva (ordemExibicao = posição + 1).
      ============================================ */

   const IMG_TIPOS = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
   const IMG_MAX_BYTES = 5 * 1024 * 1024;
   const IMG_MAX_LADO = 1600;          // fotos maiores são reduzidas antes do envio
   const IMG_ENVIOS_SIMULTANEOS = 3;
   let imgSeq = 0;
   let imgArrastando = null;
   let lightboxIndice = -1;

   function novoUid() { return 'img-' + (++imgSeq); }

   function prepararImagensIniciais(lista) {
     const ordenadas = [...(lista || [])]
       .sort((a, b) => (a.ordemExibicao ?? 0) - (b.ordemExibicao ?? 0))
       .map(img => ({ ...img, _uid: novoUid() }));
     if (ordenadas.length && !ordenadas.some(i => i.principal)) ordenadas[0].principal = true;
     return ordenadas;
   }

   function imagensEnviando() {
     return imagens.some(i => i._status === 'enviando');
   }

   function formatarBytes(b) {
     if (b < 1024) return b + ' B';
     if (b < 1024 * 1024) return Math.round(b / 1024) + ' KB';
     return (b / (1024 * 1024)).toFixed(1).replace('.', ',') + ' MB';
   }

   function statusImagem(msg, tipo = '') {
     const el = document.getElementById('status-upload-imagem');
     if (!el) return;
     el.className = 'img-status' + (tipo ? ' img-status-' + tipo : '');
     el.textContent = msg || '';
   }

   /** Reduz fotos grandes (lado maior > 1600px ou arquivo > 1,5 MB) para enviar e carregar mais rápido. GIF fica como está. */
   async function otimizarImagem(arquivo) {
     if (arquivo.type === 'image/gif') return arquivo;
     let bitmap;
     try {
       bitmap = await createImageBitmap(arquivo);
     } catch (_) {
       return arquivo; // o navegador não decodificou: o servidor valida o formato
     }
     const maior = Math.max(bitmap.width, bitmap.height);
     if (maior <= IMG_MAX_LADO && arquivo.size <= 1.5 * 1024 * 1024) { bitmap.close?.(); return arquivo; }

     const escala = Math.min(1, IMG_MAX_LADO / maior);
     const canvas = document.createElement('canvas');
     canvas.width = Math.round(bitmap.width * escala);
     canvas.height = Math.round(bitmap.height * escala);
     canvas.getContext('2d').drawImage(bitmap, 0, 0, canvas.width, canvas.height);
     bitmap.close?.();

     // PNG vira WEBP (mantém transparência e fica bem menor); JPG e WEBP mantêm o formato.
     const tipoSaida = arquivo.type === 'image/jpeg' ? 'image/jpeg' : 'image/webp';
     const blob = await new Promise(r => canvas.toBlob(r, tipoSaida, 0.85));
     if (!blob || blob.size >= arquivo.size) return arquivo;
     const ext = tipoSaida === 'image/jpeg' ? '.jpg' : '.webp';
     return new File([blob], arquivo.name.replace(/\.[^.]+$/, '') + ext, { type: tipoSaida });
   }

   /** Envia um arquivo com progresso real (XMLHttpRequest expõe o upload.onprogress, o fetch não). */
   function enviarArquivo(arquivo, aoProgredir) {
     return new Promise((resolve, reject) => {
       const xhr = new XMLHttpRequest();
       xhr.open('POST', `${API_BASE}/admin/imagens/upload`);
       const token = getToken();
       if (token) xhr.setRequestHeader('Authorization', `Bearer ${token}`);
       xhr.upload.onprogress = e => { if (e.lengthComputable) aoProgredir(Math.round(e.loaded / e.total * 100)); };
       xhr.onerror = () => reject(new Error('Sem conexão com o servidor'));
       xhr.onload = () => {
         let json = null;
         try { json = JSON.parse(xhr.responseText); } catch (_) {}
         if (xhr.status === 401) { limparSessao(); window.location.href = 'login.html'; return reject(new Error('Sessão expirada')); }
         if (xhr.status >= 200 && xhr.status < 300 && json?.url) return resolve(json.url);
         reject(new Error(json?.erro || json?.message || `Falha no envio (${xhr.status})`));
       };
       const dados = new FormData();
       dados.append('arquivo', arquivo);
       xhr.send(dados);
     });
   }

   async function processarEnvio(img) {
     img._status = 'enviando';
     img._progresso = 0;
     img._erro = null;
     atualizarCartaoImagem(img);
     try {
       let url;
       if (img._caminho) {
         url = await importarPorCaminho(img._caminho);
       } else {
         const arquivo = await otimizarImagem(img._arquivo);
         img._tamanhoEnviado = arquivo.size;
         url = await enviarArquivo(arquivo, p => { img._progresso = p; atualizarProgresso(img); });
       }
       img.url = url;
       img._status = null;
       img._arquivo = null;
       if (img._caminho) img._origem = img._caminho; // mostra o caminho do arquivo ao lado de "Imagem principal"
       img._caminho = null;
       // Mantém a prévia local até a imagem do servidor terminar de carregar (sem "piscar")
       const preload = new Image();
       preload.onload = preload.onerror = () => {
         if (img._preview) { URL.revokeObjectURL(img._preview); img._preview = null; }
         atualizarCartaoImagem(img);
       };
       preload.src = urlImagem(url);
       marcarSujo(true);
     } catch (erro) {
       img._status = 'erro';
       img._erro = erro.message;
     }
     atualizarCartaoImagem(img);
   }

   /** Adiciona arquivos (do seletor, arrastar-e-soltar ou colar). Arquivos inválidos não impedem os demais. */
   async function adicionarArquivos(lista) {
     const arquivos = Array.from(lista || []);
     if (!arquivos.length) return;

     const recusados = [];
     const novos = [];
     for (const arquivo of arquivos) {
       if (!IMG_TIPOS.includes(arquivo.type)) { recusados.push(`${arquivo.name} (formato não aceito)`); continue; }
       if (!arquivo.size) { recusados.push(`${arquivo.name} (arquivo vazio)`); continue; }
       // Acima de 5 MB só passa se não for GIF (as fotos grandes são reduzidas antes do envio)
       if (arquivo.size > IMG_MAX_BYTES && arquivo.type === 'image/gif') { recusados.push(`${arquivo.name} (GIF acima de 5 MB)`); continue; }
       if (arquivo.size > 25 * 1024 * 1024) { recusados.push(`${arquivo.name} (acima de 25 MB)`); continue; }
       const img = {
         _uid: novoUid(),
         url: '',
         descricao: arquivo.name.replace(/\.[^.]+$/, '').replace(/[-_]+/g, ' ').trim(),
         principal: imagens.length === 0 && novos.length === 0,
         _preview: URL.createObjectURL(arquivo),
         _arquivo: arquivo,
         _status: 'enviando',
         _progresso: 0,
       };
       novos.push(img);
     }
     imagens.push(...novos);
     garantirPrincipal();
     renderImagens();

     if (recusados.length) statusImagem(`Não adicionadas: ${recusados.join('; ')}. Use JPG, PNG, WEBP ou GIF.`, 'erro');
     else if (novos.length) statusImagem(`Enviando ${novos.length} imagem(ns)...`);

     // Fila com no máximo 3 envios ao mesmo tempo
     const fila = [...novos];
     const trabalhadores = Array.from({ length: Math.min(IMG_ENVIOS_SIMULTANEOS, fila.length) }, async () => {
       while (fila.length) await processarEnvio(fila.shift());
     });
     await Promise.all(trabalhadores);

     if (!novos.length) return;
     const falhas = novos.filter(i => i._status === 'erro').length;
     if (falhas) statusImagem(`${novos.length - falhas} enviada(s), ${falhas} com erro. Use "Tentar de novo" na linha da imagem.`, 'erro');
     else if (!recusados.length) statusImagem(`${novos.length} imagem(ns) enviada(s). Clique em Salvar para gravar no produto.`, 'ok');
   }

   function enviarImagens(input) {
     const arquivos = input.files;
     adicionarArquivos(arquivos).finally(() => { input.value = ''; });
   }

   function tentarEnviarDeNovo(uid) {
     const img = imagens.find(i => i._uid === uid);
     if (img?._arquivo || img?._caminho) processarEnvio(img);
   }



   /** A API lê o arquivo no computador onde ela roda (o navegador não pode abrir um caminho digitado). */
   async function importarPorCaminho(caminho) {
     const resp = await apiFetch('/admin/imagens/importar-caminho', {
       method: 'POST',
       body: JSON.stringify({ caminho }),
     });
     let json = null;
     try { json = await resp.json(); } catch (_) {}
     if (!resp.ok || !json?.url) {
       throw new Error(json?.erro || json?.message || `Falha ao importar (${resp.status})`);
     }
     return json.url;
   }

   /** Separa o que foi digitado/colado: aceita vários itens entre aspas (como o "Copiar como caminho" do Windows) ou separados por ";". */
   function separarEntradas(texto) {
     const t = (texto || '').trim();
     if (!t) return [];
     const entreAspas = t.match(/"[^"]+"/g);
     if (entreAspas) return entreAspas.map(x => x.slice(1, -1).trim()).filter(Boolean);
     return t.split(/[;\n]+/).map(x => x.trim()).filter(Boolean);
   }

   function ehLink(v) { return /^https?:\/\/\S+$/i.test(v); }
   function ehCaminho(v) { return /^(file:|[a-z]:[\\/]|\\\\|\/)/i.test(v); }

   /** Confere no navegador se o link abre como imagem antes de adicioná-lo (evita salvar link quebrado). */
   function linkCarregaImagem(url) {
     return new Promise(resolve => {
       const teste = new Image();
       const limite = setTimeout(() => { teste.src = ''; resolve(false); }, 15000);
       teste.onload = () => { clearTimeout(limite); resolve(teste.naturalWidth > 0); };
       teste.onerror = () => { clearTimeout(limite); resolve(false); };
       teste.src = url;
     });
   }

   async function adicionarPorLinkOuCaminho() {
     const campo = document.getElementById('img-link');
     const entradas = separarEntradas(campo?.value);
     if (!entradas.length) { campo?.focus(); return; }

     const recusados = [];
     const caminhos = [];
     const links = [];
     for (const e of entradas) {
       if (imagens.some(i => i.url === e)) recusados.push(`${e} (já está na lista)`);
       else if (ehLink(e)) links.push(e);
       else if (ehCaminho(e)) caminhos.push(e);
       else recusados.push(`${e} (use um link https://... ou o caminho completo, ex.: C:\\Fotos\\produto.jpg)`);
     }
     campo.value = '';
     atualizarBotaoImagem();

     // Caminhos: entram na lista na hora e são importados pela API (com "Tentar de novo" em caso de erro)
     const novos = caminhos.map(c => ({
       _uid: novoUid(),
       url: '',
       descricao: c.replace(/^"|"$/g, '').split(/[\\/]/).pop().replace(/\.[^.]+$/, '').replace(/[-_]+/g, ' ').trim(),
       principal: false,
       _caminho: c,
       _status: 'enviando',
       _progresso: 0,
     }));
     imagens.push(...novos);

     // Links: verifica se abrem como imagem
     if (links.length) statusImagem(`Verificando ${links.length} link(s)...`);
     const verificados = await Promise.all(links.map(async url => ({ url, ok: await linkCarregaImagem(url) })));
     for (const { url, ok } of verificados) {
       if (!ok) { recusados.push(`${url} (não abriu como imagem)`); continue; }
       imagens.push({ _uid: novoUid(), url, descricao: '', principal: false });
     }
     garantirPrincipal();
     renderImagens();
     if (novos.length || verificados.some(v => v.ok)) marcarSujo(true);

     await Promise.all(novos.map(processarEnvio));

     const falhas = novos.filter(i => i._status === 'erro').length;
     const adicionadas = verificados.filter(v => v.ok).length + novos.length - falhas;
     const partes = [];
     if (adicionadas) partes.push(`${adicionadas} imagem(ns) adicionada(s). Clique em Salvar para gravar no produto.`);
     if (falhas) partes.push(`${falhas} caminho(s) com erro (veja na lista).`);
     if (recusados.length) partes.push(`Não adicionadas: ${recusados.join('; ')}.`);
     statusImagem(partes.join(' '), falhas || recusados.length ? 'erro' : 'ok');
   }

   function garantirPrincipal() {
     if (imagens.length && !imagens.some(i => i.principal)) imagens[0].principal = true;
   }

   function removerImagem(uid) {
     const i = imagens.findIndex(x => x._uid === uid);
     if (i < 0) return;
     const [img] = imagens.splice(i, 1);
     if (img._preview) URL.revokeObjectURL(img._preview);
     garantirPrincipal();
     renderImagens();
     marcarSujo(true);
   }

   function marcarImagemPrincipal(uid) {
     imagens.forEach(img => { img.principal = img._uid === uid; });
     renderImagens();
     marcarSujo(true);
   }

   function moverImagem(uid, delta) {
     const i = imagens.findIndex(x => x._uid === uid);
     const j = i + delta;
     if (i < 0 || j < 0 || j >= imagens.length) return;
     [imagens[i], imagens[j]] = [imagens[j], imagens[i]];
     renderImagens();
     marcarSujo(true);
     document.querySelector(`[data-uid="${uid}"] .img-${delta < 0 ? 'subir' : 'descer'}`)?.focus();
   }

   function alterarDescricaoImagem(uid, valor) {
     const img = imagens.find(x => x._uid === uid);
     if (img) img.descricao = valor;
   }

   /* ----- Renderização ----- */

   function srcDaImagem(img) {
     return img._preview || urlImagem(img.url);
   }

   function htmlCartaoImagem(img, i) {
     const total = imagens.length;
     const enviando = img._status === 'enviando';
     const erro = img._status === 'erro';
     const src = srcDaImagem(img);
     return `
       <div class="linha-imagem ${enviando ? 'enviando' : ''} ${erro ? 'com-erro' : ''}"
            data-uid="${img._uid}" draggable="${enviando ? 'false' : 'true'}"
            ondragstart="iniciarArrasteImagem(event, '${img._uid}')" ondragend="terminarArrasteImagem()"
            ondragover="sobreCartaoImagem(event, '${img._uid}')" ondrop="soltarNoCartaoImagem(event, '${img._uid}')">
         <div class="preview ${src ? 'carregando' : ''}" ${src && !erro ? `onclick="abrirLightbox('${img._uid}')" title="Clique para ampliar" style="cursor:zoom-in"` : ''}>
           ${src
             ? `<img src="${escapeAttr(src)}" alt="" loading="lazy" decoding="async"
                     onload="this.parentElement.classList.remove('carregando')"
                     onerror="falhaAoCarregarImagem(this, '${img._uid}')">`
             : '<span style="font-size:.7rem;color:var(--texto-secundario);text-align:center;">sem<br>imagem</span>'}
         </div>

         <div class="campos-imagem">
           <input type="text" placeholder="Descrição (opcional)" maxlength="255"
                  value="${escapeAttr(img.descricao || '')}"
                  oninput="alterarDescricaoImagem('${img._uid}', this.value)">

           ${enviando ? `
             <div class="img-envio">
               <div class="img-progresso ${img._caminho ? 'indeterminado' : ''}"><div class="img-progresso-barra" style="width:${img._progresso || 0}%"></div></div>
               <span class="img-progresso-texto">${img._caminho ? 'Importando do computador...' : img._progresso >= 100 ? 'Processando...' : `Enviando ${img._progresso || 0}%`}</span>
             </div>` : erro ? `
             <div class="img-envio img-envio-erro">
               <span>Falha no envio: ${escapeHtml(img._erro || '')}</span>
               ${img._arquivo || img._caminho ? `<button type="button" class="btn-mini" onclick="tentarEnviarDeNovo('${img._uid}')">Tentar de novo</button>` : ''}
             </div>` : `
             <div class="img-principal-linha">
               <label style="display:flex; align-items:center; gap:.4rem; font-size:.85rem; flex-shrink:0;">
                 <input type="radio" name="img-principal" ${img.principal ? 'checked' : ''}
                        onchange="marcarImagemPrincipal('${img._uid}')">
                 Imagem principal
               </label>
               ${htmlOrigemImagem(img)}
             </div>`}
         </div>

         <div class="img-acoes-linha">
           <button type="button" class="img-ordem-btn img-subir" onclick="moverImagem('${img._uid}', -1)" ${i === 0 ? 'disabled' : ''}
                   title="Subir (ordem de exibição)" aria-label="Subir imagem ${i + 1}">▲</button>
           <button type="button" class="img-ordem-btn img-descer" onclick="moverImagem('${img._uid}', 1)" ${i === total - 1 ? 'disabled' : ''}
                   title="Descer (ordem de exibição)" aria-label="Descer imagem ${i + 1}">▼</button>
           <button type="button" class="btn-mini inativar" onclick="removerImagem('${img._uid}')">Remover</button>
         </div>
       </div>`;
   }

   /**
    * Caminho ou endereço da imagem, ao lado de "Imagem principal". Imagens importadas por caminho nesta sessão
    * mostram o caminho do arquivo; as demais, o endereço gravado no produto (link abre a imagem em nova aba).
    */
   /** "C:\Users\User\...\Fotos\body.jpg" -> "C:\Users\…\Fotos\body.jpg": mantém o início e o nome do arquivo visíveis. */
   function abreviarCaminho(caminho) {
     const limpo = caminho.replace(/^"|"$/g, '');
     if (limpo.length <= 60) return limpo;
     const sep = limpo.includes('\\') ? '\\' : '/';
     const partes = limpo.split(/[\\/]/);
     if (partes.length <= 4) return limpo;
     return [partes[0], partes[1], '…', partes[partes.length - 2], partes[partes.length - 1]].join(sep);
   }

   function htmlOrigemImagem(img) {
     const endereco = (img.url || '').trim();
     if (!endereco && !img._origem) return '';
     const texto = img._origem ? abreviarCaminho(img._origem) : endereco;
     const titulo = img._origem ? `Arquivo: ${img._origem}\nEndereço gravado: ${endereco}` : endereco;
     const tipo = img._origem ? 'img-origem-arquivo' : 'img-origem-link';
     return endereco
       ? `<a class="img-origem ${tipo}" href="${escapeAttr(urlImagem(endereco))}" target="_blank" rel="noopener noreferrer"
             title="${escapeAttr(titulo)}">${escapeHtml(texto)}</a>`
       : `<span class="img-origem ${tipo}" title="${escapeAttr(titulo)}">${escapeHtml(texto)}</span>`;
   }

   function renderImagens() {
     const div = document.getElementById('lista-imagens');
     if (!div) return;

     if (imagens.length === 0) {
       div.innerHTML = `
         <p style="color:var(--texto-secundario);font-size:.9rem;padding:.5rem 0;">
           Nenhuma imagem adicionada. Clique em "Escolher fotos" ou informe um link/caminho acima.
         </p>
       `;
       return;
     }

     div.innerHTML = imagens.map(htmlCartaoImagem).join('');
   }

   /** Atualiza só o cartão da imagem (evita redesenhar a grade inteira e perder o foco da descrição). */
   function atualizarCartaoImagem(img) {
     const atual = document.querySelector(`[data-uid="${img._uid}"]`);
     const i = imagens.indexOf(img);
     if (!atual || i < 0) return;
     const tmp = document.createElement('div');
     tmp.innerHTML = htmlCartaoImagem(img, i);
     atual.replaceWith(tmp.firstElementChild);
   }

   function atualizarProgresso(img) {
     const cartao = document.querySelector(`[data-uid="${img._uid}"]`);
     if (!cartao) return;
     const barra = cartao.querySelector('.img-progresso-barra');
     const texto = cartao.querySelector('.img-progresso-texto');
     if (barra) barra.style.width = img._progresso + '%';
     if (texto) texto.textContent = img._progresso >= 100 ? 'Processando...' : `Enviando ${img._progresso}%`;
   }

   function falhaAoCarregarImagem(el, uid) {
     const thumb = el.parentElement;
     thumb.classList.remove('carregando');
     thumb.removeAttribute('onclick');
     thumb.style.cursor = '';
     el.remove();
     if (!thumb.querySelector('.img-falha')) {
       thumb.insertAdjacentHTML('beforeend', `
         <button type="button" class="img-falha" title="Não carregou. Clique para tentar de novo"
                 onclick="event.stopPropagation(); recarregarImagem('${uid}')">não<br>carregou<br>↻</button>`);
     }
   }

   function recarregarImagem(uid) {
     const img = imagens.find(x => x._uid === uid);
     const thumb = document.querySelector(`[data-uid="${uid}"] .preview`);
     if (!img || !thumb) return;
     thumb.querySelector('.img-falha')?.remove();
     thumb.classList.add('carregando');
     const src = srcDaImagem(img);
     const novo = document.createElement('img');
     novo.alt = '';
     novo.onload = () => {
       thumb.classList.remove('carregando');
       thumb.setAttribute('onclick', `abrirLightbox('${uid}')`);
       thumb.style.cursor = 'zoom-in';
     };
     novo.onerror = () => falhaAoCarregarImagem(novo, uid);
     novo.src = src + (src.startsWith('blob:') ? '' : (src.includes('?') ? '&' : '?') + 'r=' + Date.now());
     thumb.prepend(novo);
   }

   /* ----- Arrastar para reordenar ----- */

   function iniciarArrasteImagem(e, uid) {
     imgArrastando = uid;
     e.dataTransfer.effectAllowed = 'move';
     e.dataTransfer.setData('text/plain', uid);
     requestAnimationFrame(() => document.querySelector(`[data-uid="${uid}"]`)?.classList.add('arrastando'));
   }

   function terminarArrasteImagem() {
     imgArrastando = null;
     document.querySelectorAll('.linha-imagem.arrastando, .linha-imagem.alvo').forEach(el => el.classList.remove('arrastando', 'alvo'));
   }

   function sobreCartaoImagem(e, uid) {
     if (!imgArrastando || imgArrastando === uid) return;
     e.preventDefault();
     document.querySelectorAll('.linha-imagem.alvo').forEach(el => el.classList.remove('alvo'));
     document.querySelector(`[data-uid="${uid}"]`)?.classList.add('alvo');
   }

   function soltarNoCartaoImagem(e, uid) {
     if (!imgArrastando) return; // arquivos soltos sobre a grade: tratados pela zona de envio
     e.preventDefault();
     e.stopPropagation();
     const de = imagens.findIndex(x => x._uid === imgArrastando);
     const para = imagens.findIndex(x => x._uid === uid);
     terminarArrasteImagem();
     if (de < 0 || para < 0 || de === para) return;
     const [img] = imagens.splice(de, 1);
     imagens.splice(para, 0, img);
     renderImagens();
     marcarSujo(true);
   }

   /* ----- Zona de envio: clicar, arrastar arquivos ou colar (Ctrl+V) ----- */

   /** Botão único: com link/caminho digitado, adiciona; com o campo vazio, abre o explorador para escolher as fotos. */
   function acaoBotaoImagem() {
     const valor = (document.getElementById('img-link')?.value || '').trim();
     if (valor) adicionarPorLinkOuCaminho();
     else document.getElementById('arquivos-imagem').click();
   }

   function atualizarBotaoImagem() {
     const btn = document.getElementById('btn-imagem-acao');
     if (!btn) return;
     const temTexto = !!(document.getElementById('img-link')?.value || '').trim();
     btn.textContent = temTexto ? 'Adicionar' : 'Escolher fotos';
     btn.title = temTexto ? 'Adicionar a imagem do link/caminho informado' : 'Abrir o explorador para escolher fotos';
   }

   /** Arrastar arquivos para o card de Imagens ou colar (Ctrl+V) faz o mesmo que "Escolher fotos". */
   function configurarZonaImagens() {
     const card = document.getElementById('card-imagens');
     if (!card) return;
     let profundidade = 0;
     const temArquivos = e => Array.from(e.dataTransfer?.types || []).includes('Files');

     card.addEventListener('dragenter', e => { if (!temArquivos(e)) return; e.preventDefault(); profundidade++; card.classList.add('img-soltar'); });
     card.addEventListener('dragover', e => { if (!temArquivos(e)) return; e.preventDefault(); e.dataTransfer.dropEffect = 'copy'; });
     card.addEventListener('dragleave', e => { if (!temArquivos(e)) return; if (--profundidade <= 0) { profundidade = 0; card.classList.remove('img-soltar'); } });
     card.addEventListener('drop', e => {
       if (!temArquivos(e)) return;
       e.preventDefault();
       profundidade = 0;
       card.classList.remove('img-soltar');
       adicionarArquivos(e.dataTransfer.files);
     });
     document.addEventListener('paste', e => {
       const alvo = e.target;
       if (alvo && (alvo.tagName === 'INPUT' || alvo.tagName === 'TEXTAREA')) return;
       const arquivos = Array.from(e.clipboardData?.files || []).filter(f => f.type.startsWith('image/'));
       if (arquivos.length) { e.preventDefault(); adicionarArquivos(arquivos); }
     });
   }

   /* ----- Visualização ampliada ----- */

   function imagensVisiveis() {
     return imagens.filter(i => srcDaImagem(i) && i._status !== 'erro');
   }

   function abrirLightbox(uid) {
     const lista = imagensVisiveis();
     lightboxIndice = lista.findIndex(i => i._uid === uid);
     if (lightboxIndice < 0) return;
     let lb = document.getElementById('img-lightbox');
     if (!lb) {
       lb = document.createElement('div');
       lb.id = 'img-lightbox';
       lb.className = 'img-lightbox';
       lb.setAttribute('role', 'dialog');
       lb.setAttribute('aria-modal', 'true');
       lb.innerHTML = `
         <button type="button" class="lb-fechar" onclick="fecharLightbox()" aria-label="Fechar">✕</button>
         <button type="button" class="lb-nav lb-ant" onclick="navegarLightbox(-1)" aria-label="Imagem anterior">‹</button>
         <figure>
           <div class="lb-palco carregando"><img id="lb-img" alt=""></div>
           <figcaption id="lb-legenda"></figcaption>
         </figure>
         <button type="button" class="lb-nav lb-prox" onclick="navegarLightbox(1)" aria-label="Próxima imagem">›</button>`;
       lb.addEventListener('click', e => { if (e.target === lb) fecharLightbox(); });
       document.body.appendChild(lb);
       document.addEventListener('keydown', e => {
         if (!document.getElementById('img-lightbox')?.classList.contains('aberto')) return;
         if (e.key === 'Escape') fecharLightbox();
         if (e.key === 'ArrowLeft') navegarLightbox(-1);
         if (e.key === 'ArrowRight') navegarLightbox(1);
       });
     }
     lb.classList.add('aberto');
     mostrarNoLightbox();
     lb.querySelector('.lb-fechar').focus();
   }

   function mostrarNoLightbox() {
     const lista = imagensVisiveis();
     const img = lista[lightboxIndice];
     if (!img) return fecharLightbox();
     const palco = document.querySelector('#img-lightbox .lb-palco');
     const el = document.getElementById('lb-img');
     palco.classList.add('carregando');
     el.onload = () => palco.classList.remove('carregando');
     el.onerror = () => palco.classList.remove('carregando');
     el.src = srcDaImagem(img);
     el.alt = img.descricao || 'Imagem do produto';
     const pos = imagens.indexOf(img) + 1;
     document.getElementById('lb-legenda').textContent =
       `${pos} de ${imagens.length}${img.principal ? ' · Principal' : ''}${img.descricao ? ' · ' + img.descricao : ''}`;
     const varias = lista.length > 1;
     document.querySelectorAll('#img-lightbox .lb-nav').forEach(b => { b.hidden = !varias; });
   }

   function navegarLightbox(delta) {
     const total = imagensVisiveis().length;
     if (!total) return;
     lightboxIndice = (lightboxIndice + delta + total) % total;
     mostrarNoLightbox();
   }

   function fecharLightbox() {
     document.getElementById('img-lightbox')?.classList.remove('aberto');
   }

   /* ============================================
      VARIAÇÕES
      ============================================ */

   function renderVariacoes() {
     const div = document.getElementById('lista-variacoes');
     if (!div) return;

     if (variacoes.length === 0) {
       div.innerHTML = `
         <p style="color:var(--texto-secundario);font-size:.9rem;padding:.5rem 0;">
           Nenhuma variação adicionada. <strong>Obrigatório:</strong>
           cadastre pelo menos uma (use "Gerar combinações" ou "+ Adicionar variação"),
           pois o estoque é controlado por variação.
         </p>
       `;
       return;
     }

     div.innerHTML = `
       <div class="tabela-rolavel">
         <table class="tabela tabela-mini">
           <thead>
             <tr>
               <th>Tamanho</th>
               <th>Cor</th>
               <th>Modelo</th>
               <th>SKU *</th>
               <th>Preço</th>
               <th>Estoque</th>
               <th>Estoque mín.</th>
               <th></th>
             </tr>
           </thead>

           <tbody>
             ${variacoes.map((v, i) => `
               <tr>
                 <td>
                   <select onchange="variacoes[${i}].tamanhoId = this.value ? Number(this.value) : null">
                     <option value="">—</option>
                     ${tamanhos.map(t => `
                       <option value="${t.id}" ${v.tamanhoId === t.id ? 'selected' : ''}>
                         ${escapeHtml(t.nome)}
                       </option>
                     `).join('')}
                   </select>
                 </td>

                 <td>
                   <select onchange="variacoes[${i}].corId = this.value ? Number(this.value) : null">
                     <option value="">—</option>
                     ${cores.map(c => `
                       <option value="${c.id}" ${v.corId === c.id ? 'selected' : ''}>
                         ${escapeHtml(c.nome)}
                       </option>
                     `).join('')}
                   </select>
                 </td>

                 <td>
                   <select onchange="variacoes[${i}].modeloId = this.value ? Number(this.value) : null">
                     <option value="">—</option>
                     ${modelos.map(m => `
                       <option value="${m.id}" ${v.modeloId === m.id ? 'selected' : ''}>
                         ${escapeHtml(m.nome)}
                       </option>
                     `).join('')}
                   </select>
                 </td>

                 <td>
                   <input type="text" value="${escapeAttr(v.sku || '')}"
                          oninput="variacoes[${i}].sku = this.value" style="min-width:140px;">
                 </td>

                 <td>
                   <input type="number" step="0.01" min="0" value="${valorDinheiro(v.preco)}"
                          placeholder="padrão"
                          title="Vazio = usa o preço do produto"
                          oninput="variacoes[${i}].preco = this.value ? Number(this.value) : null"
                          style="width:90px;">
                 </td>

                 <td>
                   ${v.id
                     ? `<strong title="Para alterar, use a tela Estoque (gera histórico de movimentação)">${v.estoqueAtual ?? 0}</strong>
                        <a href="estoque.html" class="texto-mini" style="display:block;">movimentar</a>`
                     : `<input type="number" min="0" value="${v.estoqueAtual ?? 0}"
                               title="Estoque inicial"
                               oninput="variacoes[${i}].estoqueAtual = Math.max(0, parseInt(this.value, 10) || 0); atualizarTotalEstoque()"
                               style="width:80px;">`}
                 </td>

                 <td>
                   <input type="number" min="0" value="${v.estoqueMinimo ?? 0}"
                          oninput="variacoes[${i}].estoqueMinimo = Number(this.value) || 0"
                          style="width:80px;">
                 </td>

                 <td>
                   <button type="button" class="btn-mini inativar"
                           onclick="removerVariacao(${i})">×</button>
                 </td>
               </tr>
             `).join('')}
           </tbody>
         </table>
       </div>

       <p class="dica" id="total-estoque"></p>
     `;

     atualizarTotalEstoque();
   }

   function atualizarTotalEstoque() {
     const el = document.getElementById('total-estoque');
     if (!el) return;

     const total = variacoes.reduce((s, v) => s + (Number(v.estoqueAtual) || 0), 0);

     el.innerHTML = `
       ${variacoes.length} variação(ões) · estoque total: <strong>${total}</strong> un.
       ${editando ? ' · O estoque das variações já salvas é alterado na tela <a href="estoque.html">Estoque</a>.' : ''}
     `;
   }

   function adicionarVariacao() {
     variacoes.push({
       tamanhoId: null,
       corId: null,
       modeloId: null,
       sku: '',
       preco: null,
       estoqueAtual: 0,
       estoqueMinimo: 0,
     });

     renderVariacoes();
     marcarSujo(true);
   }

   function removerVariacao(i) {
     const v = variacoes[i];

     if (v.id && !confirm(`Remover a variação ${v.sku || ''}? Se ela já tiver pedidos ou movimentações, o servidor pode recusar.\nAlternativa: deixe o estoque zerado.`)) {
       return;
     }

     variacoes.splice(i, 1);
     renderVariacoes();
     marcarSujo(true);
   }

   /* ============================================
      SALVAR
      ============================================ */

   async function salvar(cadastrarOutro = false) {
     const alerta = document.getElementById('alerta-form');
     const btn = document.getElementById(cadastrarOutro ? 'btn-salvar-outro' : 'btn-salvar');
     const textoBtn = btn.textContent;

     function mostrarErro(msg) {
       alerta.className = 'alert alert-erro';
       alerta.textContent = msg;
       alerta.classList.remove('hidden');

       btn.disabled = false;
       btn.textContent = textoBtn;

       window.scrollTo({ top: 0, behavior: 'smooth' });
     }

     alerta.classList.add('hidden');

     const getVal = (id) => document.getElementById(id)?.value ?? '';

     const body = {
       nome: getVal('f-nome').trim(),
       codigo: getVal('f-codigo').trim(),
       sku: getVal('f-sku').trim(),
       descricao: getVal('f-descricao').trim() || null,
       status: getVal('f-status') || 'ATIVO',
       categoriaId: Number(getVal('f-categoria')) || null,
       marcaId: null,
       preco: Number(getVal('f-preco')) || 0,
       precoPromocional: getVal('f-promo') ? Number(getVal('f-promo')) : null,
       custo: getVal('f-custo') ? Number(getVal('f-custo')) : null,
       peso: getVal('f-peso') ? Number(getVal('f-peso')) : null,
       altura: getVal('f-altura') ? Number(getVal('f-altura')) : null,
       largura: getVal('f-largura') ? Number(getVal('f-largura')) : null,
       comprimento: getVal('f-comprimento') ? Number(getVal('f-comprimento')) : null,
       estoqueMinimo: Number(getVal('f-est-min')) || 0,
       destaque: document.getElementById('f-destaque')?.checked ?? false,
       promocao: document.getElementById('f-promocao')?.checked ?? false,
       imagens: imagens.filter(i => (i.url || '').trim()),
       variacoes: variacoes,
     };

     if (imagensEnviando()) return mostrarErro('Aguarde o envio das imagens terminar antes de salvar.');
     if (imagens.some(i => i._status === 'erro')) {
       return mostrarErro('Há imagens que não foram enviadas: use "Tentar de novo" ou remova-as antes de salvar.');
     }
     if (!body.nome) return mostrarErro('O nome é obrigatório.');
     if (!body.codigo) return mostrarErro('O código é obrigatório.');
     if (!body.sku) return mostrarErro('O SKU é obrigatório.');
     if (!body.categoriaId) return mostrarErro('Selecione uma categoria.');
     if (body.preco <= 0) return mostrarErro('O preço deve ser maior que zero.');

     if (body.precoPromocional != null && body.precoPromocional > 0 && body.precoPromocional >= body.preco) {
       return mostrarErro('O preço promocional deve ser menor que o preço de venda.');
     }

     if (body.promocao && !(body.precoPromocional > 0)) {
       return mostrarErro('Para marcar "Em promoção", informe o preço promocional.');
     }

     for (const img of body.imagens) {
       if (!/^https?:\/\//i.test(img.url.trim())) {
         return mostrarErro(`URL de imagem inválida: ${img.url}`);
       }
     }

     // O estoque é controlado por variação.
     if (variacoes.length === 0) {
       return mostrarErro(MensagensLoja.produtoPrecisaVariacao);
     }

     // SKU obrigatório e único nas variações.
     const skus = new Set();

     for (const [i, v] of variacoes.entries()) {
       const sku = (v.sku || '').trim().toUpperCase();

       if (!sku) {
         return mostrarErro(`Informe o SKU da variação da linha ${i + 1} (ou remova a linha).`);
       }

       if (skus.has(sku)) {
         return mostrarErro(`SKU repetido nas variações: ${sku}.`);
       }

       skus.add(sku);

       if (v.preco != null && v.preco <= 0) {
         return mostrarErro(`Preço inválido na variação ${sku}.`);
       }
     }

     const combos = new Set();

     for (const v of variacoes) {
       const k = `${v.tamanhoId ?? ''}|${v.corId ?? ''}|${v.modeloId ?? ''}`;

       if (combos.has(k)) {
         return mostrarErro(`Há variações com a mesma combinação de tamanho/cor/modelo (SKU ${v.sku}).`);
       }

       combos.add(k);
     }

     btn.disabled = true;
     btn.textContent = 'Salvando...';

     try {
       // Resolve a marca antes de enviar o produto.
       body.marcaId = await resolverMarcaId();

       const payload = paraEntidade(body);

       if (editando) {
         await adminApi.atualizar('produtos', editando.id, payload);
       } else {
         await adminApi.criar('produtos', payload);
       }

       marcarSujo(false);

       window.location.href = cadastrarOutro
         ? 'produto-form.html?salvo=1'
         : 'produtos.html';

     } catch (err) {
       console.error('Erro ao salvar produto:', err);
       mostrarErro(err.message || 'Erro ao salvar. Veja o console (F12).');
     }
   }

   /* O CRUD genérico da API recebe a entidade Produto:
      relacionamentos vão como { id }. */

   function paraEntidade(body) {
     const ref = (id) => (id ? { id } : null);
     const { categoriaId, marcaId, ...resto } = body;

     return {
       ...resto,

       categoria: ref(categoriaId),
       marca: ref(marcaId),

       imagens: body.imagens.map((img, i) => ({
         id: img.id ?? null,
         url: img.url.trim(),
         descricao: img.descricao || null,
         ordemExibicao: i + 1,
         principal: !!img.principal,
       })),

       variacoes: body.variacoes.map(v => ({
         id: v.id ?? null,
         sku: v.sku.trim(),
         tamanho: ref(v.tamanhoId),
         cor: ref(v.corId),
         modelo: ref(v.modeloId),
         preco: v.preco ?? null,
         estoqueAtual: v.estoqueAtual || 0,
         estoqueMinimo: v.estoqueMinimo || 0,
         ...(v.status ? { status: v.status } : {}),
       })),
     };
   }

   function voltar() {
     window.location.href = 'produtos.html';
   }

   /* ----- Helpers ----- */

   function escapeHtml(s) {
     return String(s ?? '').replace(/[&<>"']/g, m => ({
       '&': '&amp;',
       '<': '&lt;',
       '>': '&gt;',
       '"': '&quot;',
       "'": '&#39;',
     }[m]));
   }

   function escapeAttr(s) {
     return escapeHtml(s);
   }

   /* ----- Init ----- */

   document.addEventListener('DOMContentLoaded', () => {
     if (montarLayout('Produto', 'produtos')) init();
   });
