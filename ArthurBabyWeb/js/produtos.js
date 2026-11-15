/* ============================================
   produtos.js — listagem de produtos com filtros e ordenação
   ============================================ */

   const TIPO = 'produtos';
   let cache = [];
   let categorias = [];
   let marcas = [];
   
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
       const opcoesCat = categorias.map(c =>
         `<option value="${c.id}">${escapeHtml(c.nome)}</option>`).join('');
       const opcoesMarc = marcas.map(m =>
         `<option value="${m.id}">${escapeHtml(m.nome)}</option>`).join('');
   
       conteudo.innerHTML = `
         <div class="filtros">
           <input type="text" id="f-busca" placeholder="Buscar por nome, código ou SKU..." oninput="aplicarFiltros()">
           <select id="f-categoria" onchange="aplicarFiltros()">
             <option value="">Todas as categorias</option>
             ${opcoesCat}
           </select>
           <select id="f-marca" onchange="aplicarFiltros()">
             <option value="">Todas as marcas</option>
             ${opcoesMarc}
           </select>
           <select id="f-status" onchange="aplicarFiltros()">
             <option value="">Todos os status</option>
             <option value="ATIVO">ATIVO</option>
             <option value="INATIVO">INATIVO</option>
             <option value="ESGOTADO">ESGOTADO</option>
           </select>
           <button class="btn btn-primary" onclick="novoProduto()">+ Novo produto</button>
         </div>
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
       `;
     }
   
     aplicarFiltros();
   }
   
   function aplicarFiltros() {
     const busca = (document.getElementById('f-busca')?.value || '').toLowerCase().trim();
     const catId = document.getElementById('f-categoria')?.value || '';
     const marcId = document.getElementById('f-marca')?.value || '';
     const status = document.getElementById('f-status')?.value || '';
   
     const filtrados = cache.filter(p => {
       if (busca) {
         const alvo = `${p.nome} ${p.codigo} ${p.sku}`.toLowerCase();
         if (!alvo.includes(busca)) return false;
       }
       if (catId && String(p.categoriaId) !== catId) return false;
       if (marcId && String(p.marcaId) !== marcId) return false;
       if (status && p.status !== status) return false;
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
             ? `<img src="${escapeAttr(imgPrincipal.url)}" alt=""
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