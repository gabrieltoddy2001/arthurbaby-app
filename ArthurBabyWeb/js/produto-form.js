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

       // Só itens ativos aparecem para seleção (o já vinculado ao produto continua visível)
       categorias = cats;
       marcas = marcs;
       tamanhos = tams.sort((a, b) => (a.ordemExibicao ?? 0) - (b.ordemExibicao ?? 0));
       cores = cors.sort((a, b) => (a.ordemExibicao ?? 0) - (b.ordemExibicao ?? 0));
       modelos = mods;

       if (idUrl) {
         editando = produto || null;
         if (!editando) throw new Error('Produto não encontrado');
         if (editando) {
           // A API devolve relacionamentos como objeto ({ categoria: { id } }); o formulário trabalha com ids.
           editando.categoriaId = editando.categoria?.id ?? editando.categoriaId ?? null;
           editando.marcaId = editando.marca?.id ?? editando.marcaId ?? null;
           imagens = [...(editando.imagens || [])];
           variacoes = (editando.variacoes || []).map(v => ({
             ...v,
             tamanhoId: v.tamanho?.id ?? v.tamanhoId ?? null,
             corId: v.cor?.id ?? v.corId ?? null,
             modeloId: v.modelo?.id ?? v.modeloId ?? null,
           }));
         }
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
         `<div class="alert alert-erro">Erro ao carregar: ${err.message}</div>`;
     }
   }
   
   function renderForm() {
     const conteudo = document.getElementById('conteudo');
     const titulo = editando ? 'Editar produto' : 'Novo produto';
     const p = editando || {};
   
     // Subcategorias aparecem como "Pai › Filha"; inativas só se já estiverem vinculadas
     const nomeCategoria = (c) => {
       const pai = c.categoriaPaiId ? categorias.find(x => x.id === c.categoriaPaiId) : null;
       return pai ? `${pai.nome} › ${c.nome}` : c.nome;
     };
     const optCat = categorias
       .filter(c => c.status === 'ATIVA' || p.categoriaId === c.id)
       .sort((a, b) => nomeCategoria(a).localeCompare(nomeCategoria(b), 'pt-BR'))
       .map(c => `<option value="${c.id}" ${p.categoriaId === c.id ? 'selected' : ''}>${escapeHtml(nomeCategoria(c))}${c.status !== 'ATIVA' ? ' (inativa)' : ''}</option>`)
       .join('');
     const optMarc = marcas
       .filter(m => m.status === 'ATIVA' || p.marcaId === m.id)
       .map(m => `<option value="${m.id}" ${p.marcaId === m.id ? 'selected' : ''}>${escapeHtml(m.nome)}${m.status !== 'ATIVA' ? ' (inativa)' : ''}</option>`)
       .join('');
   
     conteudo.innerHTML = `
       <div class="form-produto form-compacto">
         <div class="form-header">
           <div>
             <h1>${titulo}</h1>
             ${editando ? `<p class="texto-sec">Código ${escapeHtml(p.codigo)} · cadastrado em ${p.criadoEm ? new Date(p.criadoEm).toLocaleDateString('pt-BR') : '—'}</p>` : '<p class="texto-sec">Campos com * são obrigatórios.</p>'}
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
               <input type="text" id="f-nome" maxlength="180" value="${escapeAttr(p.nome || '')}" placeholder="Ex.: Body Manga Curta Ursinho">
             </div>
             <div class="field">
               <label for="f-codigo">Código * <span class="texto-mini">(único, gera o SKU)</span></label>
               <input type="text" id="f-codigo" maxlength="50" value="${escapeAttr(p.codigo || '')}" placeholder="Ex.: AB-001"
                      oninput="sugerirSku()">
             </div>
             <div class="field">
               <label for="f-sku">SKU principal *</label>
               <input type="text" id="f-sku" maxlength="80" value="${escapeAttr(p.sku || '')}"
                      oninput="this.dataset.manual = this.value ? '1' : ''" data-manual="${p.sku ? '1' : ''}">
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
               <select id="f-marca">
                 <option value="">Nenhuma</option>
                 ${optMarc}
               </select>
             </div>
             <div class="field">
               <label for="f-status" title="ATIVO sem estoque vira ESGOTADO automaticamente, e volta a ATIVO quando o estoque é reposto. INATIVO nunca muda sozinho.">Status <span class="texto-mini">(Esgotado é automático)</span></label>
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
                 <input type="number" step="0.01" min="0" id="f-preco" value="${p.preco ?? ''}" oninput="atualizarMargem()">
               </div>
               <div class="field">
                 <label for="f-promo" title="Quando preenchido, é o preço cobrado no pedido">Promocional (R$)</label>
                 <input type="number" step="0.01" min="0" id="f-promo" value="${p.precoPromocional ?? ''}"
                        oninput="aoDigitarPromo(); atualizarMargem()">
               </div>
               <div class="field">
                 <label for="f-custo">Custo (R$)</label>
                 <input type="number" step="0.01" min="0" id="f-custo" value="${p.custo ?? ''}" oninput="atualizarMargem()">
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
   
         <div class="card">
           <div class="secao-header">
             <h3 class="secao-titulo">Imagens</h3>
             <div style="display:flex;gap:.5rem;flex-wrap:wrap;align-items:center;">
               <button type="button" class="btn btn-ghost btn-sm" onclick="adicionarImagem()">+ Adicionar URL</button>
               <button type="button" class="btn btn-accent btn-sm" id="btn-upload-imagem" onclick="document.getElementById('arquivos-imagem').click()">Enviar fotos</button>
               <input id="arquivos-imagem" type="file" accept="image/jpeg,image/png,image/webp,image/gif" multiple hidden onchange="enviarImagens(this)">
             </div>
           </div>
           <p class="texto-sec" style="margin:.5rem 0">Selecione fotos JPG, PNG, WEBP ou GIF (até 5 MB cada). A primeira foto será a principal. Você também pode continuar usando URLs.</p>
           <div id="status-upload-imagem" role="status" aria-live="polite"></div>
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
             <div class="chips">${tamanhos.filter(t => t.status === 'ATIVO').map(t => `
               <label class="chip"><input type="checkbox" class="g-tam" value="${t.id}" onchange="this.parentElement.classList.toggle('marcado', this.checked)"> ${escapeHtml(t.nome)}</label>`).join('') || '<span class="texto-mini">Nenhum tamanho ativo</span>'}</div>
             <div class="texto-mini">Cores</div>
             <div class="chips">${cores.filter(c => c.status === 'ATIVA').map(c => `
               <label class="chip"><input type="checkbox" class="g-cor" value="${c.id}" onchange="this.parentElement.classList.toggle('marcado', this.checked)">
                 ${c.codigoHex ? `<span class="cor-amostra" style="background:${escapeAttr(c.codigoHex)}"></span>` : ''}${escapeHtml(c.nome)}</label>`).join('') || '<span class="texto-mini">Nenhuma cor ativa</span>'}</div>
             <div class="grid-3" style="align-items:end;">
               <div class="field" style="margin-bottom:0;">
                 <label for="g-modelo">Modelo (opcional)</label>
                 <select id="g-modelo"><option value="">—</option>${modelos.filter(m => m.status === 'ATIVO').map(m => `<option value="${m.id}">${escapeHtml(m.nome)}</option>`).join('')}</select>
               </div>
               <div class="field" style="margin-bottom:0;">
                 <label for="g-estoque">Estoque inicial de cada</label>
                 <input type="number" id="g-estoque" min="0" value="0">
               </div>
               <button type="button" class="btn btn-primary btn-sm" onclick="gerarVariacoes()">Gerar</button>
             </div>
             <div class="dica">O SKU é montado com o código do produto + tamanho + cor. Combinações já existentes são ignoradas.</div>
           </div>
           <div id="lista-variacoes"></div>
         </div>
       </div>
     `;

     renderImagens();
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
   function marcarSujo(v) { formSujo = v; }
   window.addEventListener('beforeunload', (e) => {
     if (formSujo) { e.preventDefault(); e.returnValue = ''; }
   });

   function contarDescricao() {
     const t = document.getElementById('f-descricao');
     const c = document.getElementById('cont-desc');
     if (t && c) c.textContent = `(${t.value.length}/2000)`;
   }

   function slug(s) {
     return String(s || '').normalize('NFD').replace(/[̀-ͯ]/g, '')
       .toUpperCase().replace(/[^A-Z0-9]+/g, '-').replace(/^-|-$/g, '');
   }

   function sugerirSku() {
     const sku = document.getElementById('f-sku');
     if (!sku.dataset.manual) sku.value = slug(document.getElementById('f-codigo').value);
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
     const markup = (venda / custo);
     box.classList.add(lucro <= 0 ? 'ruim' : (margem >= 30 ? 'boa' : ''));
     box.innerHTML = `Vendendo por <strong>R$ ${venda.toFixed(2).replace('.', ',')}</strong>${promo > 0 ? ' (promocional)' : ''}:
       lucro de <strong>R$ ${lucro.toFixed(2).replace('.', ',')}</strong> por unidade ·
       margem <strong>${margem.toFixed(1).replace('.', ',')}%</strong> · markup <strong>${markup.toFixed(2).replace('.', ',')}x</strong>
       ${promo > 0 ? ` · desconto de ${(((preco - promo) / preco) * 100).toFixed(0)}%` : ''}
       ${lucro <= 0 ? ' — atenção: venda com prejuízo!' : ''}`;
   }

   function alternarGerador() {
     document.getElementById('gerador').classList.toggle('hidden');
   }

   function gerarVariacoes() {
     const tamIds = [...document.querySelectorAll('.g-tam:checked')].map(i => Number(i.value));
     const corIds = [...document.querySelectorAll('.g-cor:checked')].map(i => Number(i.value));
     const modeloId = Number(document.getElementById('g-modelo').value) || null;
     const estoque = Math.max(0, parseInt(document.getElementById('g-estoque').value, 10) || 0);
     if (!tamIds.length && !corIds.length) { alert('Selecione ao menos um tamanho ou uma cor.'); return; }

     const codigo = slug(document.getElementById('f-codigo').value) || 'PROD';
     const ts = tamIds.length ? tamIds : [null];
     const cs = corIds.length ? corIds : [null];
     let criadas = 0;
     ts.forEach(tId => cs.forEach(cId => {
       const existe = variacoes.some(v => (v.tamanhoId ?? null) === tId && (v.corId ?? null) === cId && (v.modeloId ?? null) === modeloId);
       if (existe) return;
       const tNome = tamanhos.find(t => t.id === tId)?.nome;
       const cNome = cores.find(c => c.id === cId)?.nome;
       let sku = [codigo, slug(tNome), slug(cNome)].filter(Boolean).join('-');
       let n = 2;
       while (variacoes.some(v => (v.sku || '').toUpperCase() === sku)) sku = `${sku}-${n++}`;
       variacoes.push({ tamanhoId: tId, corId: cId, modeloId, sku, preco: null, estoqueAtual: estoque, estoqueMinimo: 0 });
       criadas++;
     }));
     renderVariacoes();
     marcarSujo(true);
     alert(criadas ? `${criadas} variação(ões) gerada(s).` : 'Todas as combinações já existiam.');
   }
   
   /* ============================================
      IMAGENS
      ============================================ */
   
   async function enviarImagens(input) {
     const arquivos = Array.from(input.files || []);
     input.value = '';
     if (!arquivos.length) return;
     const btn = document.getElementById('btn-upload-imagem');
     const status = document.getElementById('status-upload-imagem');
     const tipos = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
     btn.disabled = true;
     try {
       for (let i = 0; i < arquivos.length; i++) {
         const arquivo = arquivos[i];
         if (!tipos.includes(arquivo.type) || arquivo.size > 5 * 1024 * 1024 || !arquivo.size) {
           throw new Error(`Arquivo inválido: ${arquivo.name}. Use JPG, PNG, WEBP ou GIF de até 5 MB.`);
         }
         status.textContent = `Enviando ${i + 1} de ${arquivos.length}: ${arquivo.name}`;
         const dados = new FormData();
         dados.append('arquivo', arquivo);
         const resposta = await fetch(`${API_BASE}/admin/imagens/upload`, {
           method: 'POST',
           headers: { Authorization: `Bearer ${getToken() || ''}` },
           body: dados
         });
         if (!resposta.ok) {
           const erro = await resposta.json().catch(() => ({}));
           throw new Error(erro.erro || `Falha no upload (${resposta.status})`);
         }
         const resultado = await resposta.json();
         imagens.push({ url: resultado.url, descricao: arquivo.name, principal: imagens.length === 0 });
         renderImagens();
         marcarSujo(true);
       }
       status.textContent = `${arquivos.length} imagem(ns) enviada(s). Clique em Salvar para associar ao produto.`;
     } catch (erro) {
       status.textContent = `Erro: ${erro.message}. As imagens já enviadas permanecem na lista.`;
     } finally {
       btn.disabled = false;
     }
   }

   function renderImagens() {
     const div = document.getElementById('lista-imagens');
     if (!div) return;
   
     if (imagens.length === 0) {
       div.innerHTML = `<p style="color:var(--texto-secundario);font-size:.9rem;padding:.5rem 0;">
         Nenhuma imagem adicionada. Clique em "+ Adicionar imagem".
       </p>`;
       return;
     }
   
     div.innerHTML = imagens.map((img, i) => `
       <div class="linha-imagem">
         <div class="preview">
           ${img.url
             ? `<img src="${escapeAttr(img.url)}" alt=""
                     style="width:100%;height:100%;object-fit:cover;"
                     onerror="this.parentElement.innerHTML='<span style=\\'font-size:.65rem;color:#EF4444;text-align:center;padding:.3rem;\\'>não<br>carregou</span>'">`
             : '<span style="font-size:.7rem;color:var(--texto-secundario);text-align:center;">sem<br>imagem</span>'}
         </div>
         <div class="campos-imagem">
           <input type="text" id="img-url-${i}" placeholder="URL da imagem"
                  value="${escapeAttr(img.url || '')}"
                  oninput="imagens[${i}].url = this.value;"
                  onblur="renderImagens();">
           <input type="text" id="img-desc-${i}" placeholder="Descrição (opcional)"
                  value="${escapeAttr(img.descricao || '')}"
                  oninput="imagens[${i}].descricao = this.value;">
           <label style="display:flex; align-items:center; gap:.4rem; font-size:.85rem;">
             <input type="radio" name="img-principal" ${img.principal ? 'checked' : ''}
                    onchange="marcarImagemPrincipal(${i})">
             Imagem principal
           </label>
         </div>
         <button type="button" class="btn-mini inativar" onclick="removerImagem(${i})">Remover</button>
       </div>
     `).join('');
   }
   
   function adicionarImagem() {
     imagens.push({ url: '', descricao: '', ordemExibicao: imagens.length, principal: imagens.length === 0 });
     renderImagens();
   }
   
   function removerImagem(i) {
     const eraPrincipal = imagens[i].principal;
     imagens.splice(i, 1);
     if (eraPrincipal && imagens.length > 0) imagens[0].principal = true;
     renderImagens();
   }
   
   function marcarImagemPrincipal(i) {
     imagens.forEach((img, idx) => img.principal = idx === i);
     renderImagens();
   }
   
   /* ============================================
      VARIAÇÕES
      ============================================ */
   
   function renderVariacoes() {
     const div = document.getElementById('lista-variacoes');
     if (!div) return;
   
     if (variacoes.length === 0) {
       div.innerHTML = `<p style="color:var(--texto-secundario);font-size:.9rem;padding:.5rem 0;">
         Nenhuma variação adicionada. <strong>Obrigatório:</strong> cadastre pelo menos uma (use "Gerar combinações" ou "+ Adicionar variação"), pois o estoque é controlado por variação.
       </p>`;
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
                     ${tamanhos.map(t => `<option value="${t.id}" ${v.tamanhoId === t.id ? 'selected' : ''}>${escapeHtml(t.nome)}</option>`).join('')}
                   </select>
                 </td>
                 <td>
                   <select onchange="variacoes[${i}].corId = this.value ? Number(this.value) : null">
                     <option value="">—</option>
                     ${cores.map(c => `<option value="${c.id}" ${v.corId === c.id ? 'selected' : ''}>${escapeHtml(c.nome)}</option>`).join('')}
                   </select>
                 </td>
                 <td>
                   <select onchange="variacoes[${i}].modeloId = this.value ? Number(this.value) : null">
                     <option value="">—</option>
                     ${modelos.map(m => `<option value="${m.id}" ${v.modeloId === m.id ? 'selected' : ''}>${escapeHtml(m.nome)}</option>`).join('')}
                   </select>
                 </td>
                 <td>
                   <input type="text" value="${escapeAttr(v.sku || '')}"
                          oninput="variacoes[${i}].sku = this.value" style="min-width:140px;">
                 </td>
                 <td>
                   <input type="number" step="0.01" min="0" value="${v.preco ?? ''}" placeholder="padrão"
                          title="Vazio = usa o preço do produto"
                          oninput="variacoes[${i}].preco = this.value ? Number(this.value) : null" style="width:90px;">
                 </td>
                 <td>
                   ${v.id
                     ? `<strong title="Para alterar, use a tela Estoque (gera histórico de movimentação)">${v.estoqueAtual ?? 0}</strong>
                        <a href="estoque.html" class="texto-mini" style="display:block;">movimentar</a>`
                     : `<input type="number" min="0" value="${v.estoqueAtual ?? 0}" title="Estoque inicial"
                          oninput="variacoes[${i}].estoqueAtual = Math.max(0, parseInt(this.value, 10) || 0); atualizarTotalEstoque()" style="width:80px;">`}
                 </td>
                 <td>
                   <input type="number" min="0" value="${v.estoqueMinimo ?? 0}"
                          oninput="variacoes[${i}].estoqueMinimo = Number(this.value) || 0" style="width:80px;">
                 </td>
                 <td>
                   <button type="button" class="btn-mini inativar" onclick="removerVariacao(${i})">×</button>
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
     el.innerHTML = `${variacoes.length} variação(ões) · estoque total: <strong>${total}</strong> un.
       ${editando ? ' · O estoque das variações já salvas é alterado na tela <a href="estoque.html">Estoque</a>.' : ''}`;
   }
   
   function adicionarVariacao() {
     variacoes.push({
       tamanhoId: null, corId: null, modeloId: null,
       sku: '', preco: null, estoqueAtual: 0, estoqueMinimo: 0,
     });
     renderVariacoes();
   }
   
   function removerVariacao(i) {
     const v = variacoes[i];
     if (v.id && !confirm(`Remover a variação ${v.sku || ''}? Se ela já tiver pedidos ou movimentações, o servidor pode recusar.\nAlternativa: deixe o estoque zerado.`)) return;
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
       marcaId: Number(getVal('f-marca')) || null,
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
       if (!/^https?:\/\//i.test(img.url.trim())) return mostrarErro(`URL de imagem inválida: ${img.url}`);
     }
     // Mesma regra do servidor: o estoque é controlado por variação, então todo produto precisa de pelo menos uma
     if (variacoes.length === 0) return mostrarErro(MensagensLoja.produtoPrecisaVariacao);
     // Variações: SKU obrigatório e único (inclusive em relação ao SKU principal de outras linhas)
     const skus = new Set();
     for (const [i, v] of variacoes.entries()) {
       const sku = (v.sku || '').trim().toUpperCase();
       if (!sku) return mostrarErro(`Informe o SKU da variação da linha ${i + 1} (ou remova a linha).`);
       if (skus.has(sku)) return mostrarErro(`SKU repetido nas variações: ${sku}.`);
       skus.add(sku);
       if (v.preco != null && v.preco <= 0) return mostrarErro(`Preço inválido na variação ${sku}.`);
     }
     const combos = new Set();
     for (const v of variacoes) {
       const k = `${v.tamanhoId ?? ''}|${v.corId ?? ''}|${v.modeloId ?? ''}`;
       if (combos.has(k)) return mostrarErro(`Há variações com a mesma combinação de tamanho/cor/modelo (SKU ${v.sku}).`);
       combos.add(k);
     }

     btn.disabled = true;
     btn.textContent = 'Salvando...';

     try {
       const payload = MOCK ? body : paraEntidade(body);
       if (editando) {
         await adminApi.atualizar('produtos', editando.id, payload);
       } else {
         await adminApi.criar('produtos', payload);
       }
       marcarSujo(false);
       window.location.href = cadastrarOutro ? 'produto-form.html?salvo=1' : 'produtos.html';
     } catch (err) {
       console.error('Erro ao salvar produto:', err);
       mostrarErro(err.message || 'Erro ao salvar. Veja o console (F12).');
     }
   }
   
   /* O CRUD genérico da API recebe a entidade Produto: relacionamentos vão como { id }. */
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
       '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'
     }[m]));
   }
   function escapeAttr(s) { return escapeHtml(s); }
   
   /* ----- Init ----- */
   document.addEventListener('DOMContentLoaded', () => {
     if (montarLayout('Produto', 'produtos')) init();
   });