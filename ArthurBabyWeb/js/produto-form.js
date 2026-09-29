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
       const [cats, marcs, tams, cors, mods, produtos] = await Promise.all([
         adminApi.listar('categorias'),
         adminApi.listar('marcas'),
         adminApi.listar('tamanhos'),
         adminApi.listar('cores'),
         adminApi.listar('modelos'),
         idUrl ? adminApi.listar('produtos') : Promise.resolve([]),
       ]);
   
       categorias = cats;
       marcas = marcs;
       tamanhos = tams;
       cores = cors;
       modelos = mods;
   
       if (idUrl) {
         editando = produtos.find(p => p.id === idUrl) || null;
         if (editando) {
           imagens = [...(editando.imagens || [])];
           variacoes = [...(editando.variacoes || [])];
         }
       }
   
       renderForm();
     } catch (err) {
       document.getElementById('conteudo').innerHTML =
         `<div class="alert alert-erro">Erro ao carregar: ${err.message}</div>`;
     }
   }
   
   function renderForm() {
     const conteudo = document.getElementById('conteudo');
     const titulo = editando ? 'Editar produto' : 'Novo produto';
     const p = editando || {};
   
     const optCat = categorias.map(c =>
       `<option value="${c.id}" ${p.categoriaId === c.id ? 'selected' : ''}>${escapeHtml(c.nome)}</option>`
     ).join('');
     const optMarc = marcas.map(m =>
       `<option value="${m.id}" ${p.marcaId === m.id ? 'selected' : ''}>${escapeHtml(m.nome)}</option>`
     ).join('');
   
     conteudo.innerHTML = `
       <div class="form-produto">
         <div class="form-header">
           <h1>${titulo}</h1>
           <div>
             <button class="btn btn-ghost" onclick="voltar()">Cancelar</button>
             <button class="btn btn-primary" onclick="salvar()" id="btn-salvar">Salvar</button>
           </div>
         </div>
   
         <div id="alerta-form" class="alert alert-erro hidden"></div>
   
         <div class="card">
           <h3 class="secao-titulo">Dados básicos</h3>
           <div class="grid-2">
             <div class="field">
               <label>Nome *</label>
               <input type="text" id="f-nome" value="${escapeAttr(p.nome || '')}">
             </div>
             <div class="field">
               <label>Código *</label>
               <input type="text" id="f-codigo" value="${escapeAttr(p.codigo || '')}">
             </div>
             <div class="field">
               <label>SKU principal *</label>
               <input type="text" id="f-sku" value="${escapeAttr(p.sku || '')}">
             </div>
             <div class="field">
               <label>Status</label>
               <select id="f-status">
                 <option value="ATIVO" ${p.status === 'ATIVO' || !p.status ? 'selected' : ''}>ATIVO</option>
                 <option value="INATIVO" ${p.status === 'INATIVO' ? 'selected' : ''}>INATIVO</option>
                 <option value="ESGOTADO" ${p.status === 'ESGOTADO' ? 'selected' : ''}>ESGOTADO</option>
               </select>
             </div>
           </div>
           <div class="field">
             <label>Descrição</label>
             <textarea id="f-descricao" rows="3">${escapeHtml(p.descricao || '')}</textarea>
           </div>
         </div>
   
         <div class="card">
           <h3 class="secao-titulo">Classificação</h3>
           <div class="grid-2">
             <div class="field">
               <label>Categoria *</label>
               <select id="f-categoria">
                 <option value="">Selecione...</option>
                 ${optCat}
               </select>
             </div>
             <div class="field">
               <label>Marca</label>
               <select id="f-marca">
                 <option value="">Nenhuma</option>
                 ${optMarc}
               </select>
             </div>
           </div>
         </div>
   
         <div class="card">
           <h3 class="secao-titulo">Preços</h3>
           <div class="grid-3">
             <div class="field">
               <label>Preço * (R$)</label>
               <input type="number" step="0.01" min="0" id="f-preco" value="${p.preco ?? ''}">
             </div>
             <div class="field">
               <label>Preço promocional (R$)</label>
               <input type="number" step="0.01" min="0" id="f-promo" value="${p.precoPromocional ?? ''}">
             </div>
             <div class="field">
               <label>Custo (R$)</label>
               <input type="number" step="0.01" min="0" id="f-custo" value="${p.custo ?? ''}">
             </div>
           </div>
           <div class="checkboxes">
             <label><input type="checkbox" id="f-destaque" ${p.destaque ? 'checked' : ''}> Destaque na home</label>
             <label><input type="checkbox" id="f-promocao" ${p.promocao ? 'checked' : ''}> Em promoção</label>
           </div>
         </div>
   
         <div class="card">
           <h3 class="secao-titulo">Dimensões e estoque mínimo</h3>
           <div class="grid-5">
             <div class="field">
               <label>Peso (kg)</label>
               <input type="number" step="0.001" min="0" id="f-peso" value="${p.peso ?? ''}">
             </div>
             <div class="field">
               <label>Altura (cm)</label>
               <input type="number" step="0.01" min="0" id="f-altura" value="${p.altura ?? ''}">
             </div>
             <div class="field">
               <label>Largura (cm)</label>
               <input type="number" step="0.01" min="0" id="f-largura" value="${p.largura ?? ''}">
             </div>
             <div class="field">
               <label>Comprimento (cm)</label>
               <input type="number" step="0.01" min="0" id="f-comprimento" value="${p.comprimento ?? ''}">
             </div>
             <div class="field">
               <label>Estoque mínimo</label>
               <input type="number" min="0" id="f-est-min" value="${p.estoqueMinimo ?? 0}">
             </div>
           </div>
         </div>
   
         <div class="card">
           <div class="secao-header">
             <h3 class="secao-titulo">Imagens</h3>
             <button type="button" class="btn btn-accent btn-sm" onclick="adicionarImagem()">+ Adicionar imagem</button>
           </div>
           <div id="lista-imagens"></div>
         </div>
   
         <div class="card">
           <div class="secao-header">
             <h3 class="secao-titulo">Variações (tamanho + cor + modelo)</h3>
             <button type="button" class="btn btn-accent btn-sm" onclick="adicionarVariacao()">+ Adicionar variação</button>
           </div>
           <div id="lista-variacoes"></div>
         </div>
       </div>
     `;
   
     renderImagens();
     renderVariacoes();
   }
   
   /* ============================================
      IMAGENS
      ============================================ */
   
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
         Nenhuma variação adicionada. O produto será cadastrado como item único.
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
                   <input type="number" step="0.01" min="0" value="${v.preco ?? ''}"
                          oninput="variacoes[${i}].preco = this.value ? Number(this.value) : null" style="width:90px;">
                 </td>
                 <td>
                   <input type="number" min="0" value="${v.estoqueAtual ?? 0}"
                          oninput="variacoes[${i}].estoqueAtual = Number(this.value) || 0" style="width:80px;">
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
     `;
   }
   
   function adicionarVariacao() {
     variacoes.push({
       tamanhoId: null, corId: null, modeloId: null,
       sku: '', preco: null, estoqueAtual: 0, estoqueMinimo: 0,
     });
     renderVariacoes();
   }
   
   function removerVariacao(i) {
     variacoes.splice(i, 1);
     renderVariacoes();
   }
   
   /* ============================================
      SALVAR
      ============================================ */
   
   async function salvar() {
     const alerta = document.getElementById('alerta-form');
     const btn = document.getElementById('btn-salvar');
   
     function mostrarErro(msg) {
       alerta.textContent = msg;
       alerta.classList.remove('hidden');
       btn.disabled = false;
       btn.textContent = 'Salvar';
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
       imagens: imagens.filter(i => i.url.trim()),
       variacoes: variacoes.filter(v => v.sku.trim()),
     };
   
     if (!body.nome) return mostrarErro('O nome é obrigatório.');
     if (!body.codigo) return mostrarErro('O código é obrigatório.');
     if (!body.sku) return mostrarErro('O SKU é obrigatório.');
     if (!body.categoriaId) return mostrarErro('Selecione uma categoria.');
     if (body.preco <= 0) return mostrarErro('O preço deve ser maior que zero.');
   
     btn.disabled = true;
     btn.textContent = 'Salvando...';
   
     try {
       if (editando) {
         await adminApi.atualizar('produtos', editando.id, body);
       } else {
         await adminApi.criar('produtos', body);
       }
       window.location.href = 'produtos.html';
     } catch (err) {
       console.error('Erro ao salvar produto:', err);
       mostrarErro(err.message || 'Erro ao salvar. Veja o console (F12).');
     }
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