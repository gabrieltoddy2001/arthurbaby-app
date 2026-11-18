/* ============================================
   produtos.js — listagem de produtos com filtros e ordenação
   ============================================ */

   const TIPO = 'produtos';
   let cache = [];
   let categorias = [];
   let marcas = [];
   
   // Variáveis para os Selects Customizados
   let valorCat = '';
   let valorMarc = '';
   let valorStatus = '';
   
   async function carregar() {
     const conteudo = document.getElementById('conteudo');
     conteudo.innerHTML = '<div class="loading">Carregando...</div>';
   
     try {
       const [lista, cats, marcs] = await Promise.all([
         adminApi.listar(TIPO),
         adminApi.listar('categorias'),
         adminApi.listar('marcas'),
       ]);
       cache = lista;
       categorias = cats;
       marcas = marcs;
       renderTudo();
     } catch (err) {
       conteudo.innerHTML = `<div class="alert alert-erro">Erro: ${err.message}</div>`;
     }
   }
   
   function renderTudo() {
     const conteudo = document.getElementById('conteudo');
   
     if (!document.getElementById('tabela-produtos')) {
       // Prepara as opções dos selects customizados
       const opcoesCat = categorias.map(c =>
         `<div class="custom-option" onclick="selecionarFiltro('cat', '${c.id}', '${escapeHtml(c.nome)}')">${escapeHtml(c.nome)}</div>`
       ).join('');
       
       const opcoesMarc = marcas.map(m =>
         `<div class="custom-option" onclick="selecionarFiltro('marc', '${m.id}', '${escapeHtml(m.nome)}')">${escapeHtml(m.nome)}</div>`
       ).join('');
   
       conteudo.innerHTML = `
         <div class="filtros">
           <input type="text" id="f-busca" placeholder="Buscar por nome, código ou SKU..." oninput="aplicarFiltros()">
           
           <!-- SELECT CATEGORIA -->
           <div class="custom-select" id="custom-cat" style="position:relative; min-width:160px;">
             <div class="custom-select-trigger" onclick="toggleSelect('cat')">
               <span id="cat-label" style="white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">Todas as categorias</span>
               <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
             </div>
             <div class="custom-select-options hidden" id="cat-options">
               <div class="custom-option" onclick="selecionarFiltro('cat', '', 'Todas as categorias')">Todas as categorias</div>
               ${opcoesCat}
             </div>
           </div>
   
           <!-- SELECT MARCA -->
           <div class="custom-select" id="custom-marc" style="position:relative; min-width:160px;">
             <div class="custom-select-trigger" onclick="toggleSelect('marc')">
               <span id="marc-label" style="white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">Todas as marcas</span>
               <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
             </div>
             <div class="custom-select-options hidden" id="marc-options">
               <div class="custom-option" onclick="selecionarFiltro('marc', '', 'Todas as marcas')">Todas as marcas</div>
               ${opcoesMarc}
             </div>
           </div>
   
           <!-- SELECT STATUS -->
           <div class="custom-select" id="custom-status" style="position:relative; min-width:140px;">
             <div class="custom-select-trigger" onclick="toggleSelect('status')">
               <span id="status-label" style="white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">Todos os status</span>
               <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
             </div>
             <div class="custom-select-options hidden" id="status-options">
               <div class="custom-option" onclick="selecionarFiltro('status', '', 'Todos os status')">Todos os status</div>
               <div class="custom-option" onclick="selecionarFiltro('status', 'ATIVO', 'ATIVO')">ATIVO</div>
               <div class="custom-option" onclick="selecionarFiltro('status', 'INATIVO', 'INATIVO')">INATIVO</div>
               <div class="custom-option" onclick="selecionarFiltro('status', 'ESGOTADO', 'ESGOTADO')">ESGOTADO</div>
             </div>
           </div>
   
           <button class="btn btn-primary" onclick="novoProduto()">+ Novo produto</button>
         </div>
         
         <div class="tabela-rolavel">
           <table class="tabela tabela-ordenavel">
             <thead>
               <tr>
                 <th>Foto</th>
                 <th data-ordenavel="nome">Produto</th>
                 <th data-ordenavel="categoria">Categoria</th>
                 <th data-ordenavel="marca">Marca</th>
                 <th data-ordenavel="preco" class="text-right">Preço</th>
                 <th data-ordenavel="status">Status</th>
                 <th>Ações</th>
               </tr>
             </thead>
             <tbody id="tabela-produtos"></tbody>
           </table>
         </div>
       `;
     }
   
     aplicarFiltros();
   }
   
   /* ============================================
      CONTROLE DOS SELECTS CUSTOMIZADOS
      ============================================ */
   function toggleSelect(id) {
     document.getElementById(`${id}-options`).classList.toggle('hidden');
   }
   
   // Fecha os selects se clicar fora
   document.addEventListener('click', (e) => {
     if (!e.target.closest('#custom-cat')) document.getElementById('cat-options')?.classList.add('hidden');
     if (!e.target.closest('#custom-marc')) document.getElementById('marc-options')?.classList.add('hidden');
     if (!e.target.closest('#custom-status')) document.getElementById('status-options')?.classList.add('hidden');
   });
   
   function selecionarFiltro(tipo, valor, label) {
     if (tipo === 'cat') {
       valorCat = valor;
       document.getElementById('cat-label').textContent = label;
       document.getElementById('cat-options').classList.add('hidden');
     } else if (tipo === 'marc') {
       valorMarc = valor;
       document.getElementById('marc-label').textContent = label;
       document.getElementById('marc-options').classList.add('hidden');
     } else if (tipo === 'status') {
       valorStatus = valor;
       document.getElementById('status-label').textContent = label;
       document.getElementById('status-options').classList.add('hidden');
     }
     aplicarFiltros();
   }
   
   function aplicarFiltros() {
     const busca = (document.getElementById('f-busca')?.value || '').toLowerCase().trim();
   
     const filtrados = cache.filter(p => {
       if (busca) {
         const alvo = `${p.nome} ${p.codigo} ${p.sku}`.toLowerCase();
         if (!alvo.includes(busca)) return false;
       }
       if (valorCat && String(p.categoriaId) !== valorCat) return false;
       if (valorMarc && String(p.marcaId) !== valorMarc) return false;
       if (valorStatus && p.status !== valorStatus) return false;
       return true;
     });
   
     renderTabela(filtrados);
   }
   
   function renderTabela(lista) {
     const tbody = document.getElementById('tabela-produtos');
   
     tbody.innerHTML = lista.length === 0
       ? `<tr><td colspan="7" class="text-center" style="padding:2rem;color:var(--texto-secundario)">
            Nenhum produto encontrado.
          </td></tr>`
       : lista.map(p => {
           const cat = categorias.find(c => c.id === p.categoriaId);
           const marc = marcas.find(m => m.id === p.marcaId);
   
           const precoExibir = p.promocao && p.precoPromocional
             ? `<span style="text-decoration:line-through;color:var(--texto-secundario);font-size:.8rem;">
                  R$ ${formatarPreco(p.preco)}
                </span><br>
                <strong style="color:var(--rosa-escuro);">R$ ${formatarPreco(p.precoPromocional)}</strong>`
             : `<strong>R$ ${formatarPreco(p.preco)}</strong>`;
   
           const badgeStatus = {
             'ATIVO': 'badge-ativo',
             'INATIVO': 'badge-inativo',
             'ESGOTADO': 'badge-esgotado',
           }[p.status] || 'badge-inativo';
   
           const imgPrincipal = (p.imagens || []).find(i => i.principal) || (p.imagens || [])[0];
           const imgHtml = imgPrincipal?.url
             ? `<img src="${escapeAttr(urlImagem(imgPrincipal.url))}" loading="lazy" alt=""
                     style="width:50px;height:50px;object-fit:cover;border-radius:6px;border:1px solid var(--borda);"
                     onerror="this.parentElement.innerHTML='<div style=\\'width:50px;height:50px;background:#FEE2E2;border-radius:6px;border:1px solid #FCA5A5;display:grid;place-items:center;font-size:.6rem;color:#991B1B;text-align:center;\\'>não<br>carregou</div>'">`
             : `<div style="width:50px;height:50px;background:var(--superficie);border-radius:6px;
                            border:1px solid var(--borda);display:grid;place-items:center;font-size:.7rem;
                            color:var(--texto-secundario);text-align:center;">sem<br>img</div>`;
   
           return `
             <tr>
               <td>${imgHtml}</td>
               <td data-campo="nome">
                 <strong>${escapeHtml(p.nome)}</strong><br>
                 <code style="font-size:.75rem;color:var(--texto-secundario);">${escapeHtml(p.codigo)}</code>
                 ${p.destaque ? ' ⭐' : ''}
               </td>
               <td data-campo="categoria">${cat ? escapeHtml(cat.nome) : '—'}</td>
               <td data-campo="marca">${marc ? escapeHtml(marc.nome) : '—'}</td>
               <td data-campo="preco" class="text-right">${precoExibir}</td>
               <td data-campo="status"><span class="badge ${badgeStatus}">${p.status}</span></td>
               <td>
                 <div class="acoes-linha">
                   <button class="btn-mini editar" onclick="editarProduto(${p.id})">Editar</button>
                   <button class="btn-mini ${p.status === 'ATIVO' ? 'inativar' : 'ativar'}"
                           onclick="alternar(${p.id})">
                     ${p.status === 'ATIVO' ? 'Inativar' : 'Ativar'}
                   </button>
                 </div>
               </td>
             </tr>`;
         }).join('');
   }
   
   function novoProduto() {
     window.location.href = 'produto-form.html';
   }
   
   function editarProduto(id) {
     window.location.href = `produto-form.html?id=${id}`;
   }
   
   async function alternar(id) {
     const p = cache.find(x => x.id === id);
     if (!p) return;
     try {
       await adminApi.atualizar(TIPO, id, {
         ...p,
         status: p.status === 'ATIVO' ? 'INATIVO' : 'ATIVO',
       });
       carregar();
     } catch (err) {
       alert('Erro: ' + err.message);
     }
   }
   
   /* ----- Helpers ----- */
   function formatarPreco(v) {
     return Number(v || 0).toFixed(2).replace('.', ',');
   }
   function escapeHtml(s) {
     return String(s ?? '').replace(/[&<>"']/g, m => ({
       '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'
     }[m]));
   }
   function escapeAttr(s) { return escapeHtml(s); }
   
   /* ----- Init ----- */
   document.addEventListener('DOMContentLoaded', () => {
     if (montarLayout('Produtos', 'produtos')) carregar();
   });