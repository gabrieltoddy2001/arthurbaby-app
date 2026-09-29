/* ============================================
   configuracoes.js — configurações da loja
   ============================================ */

   let config = {};

   async function carregar() {
     const conteudo = document.getElementById('conteudo');
     conteudo.innerHTML = '<div class="loading">Carregando...</div>';
   
     try {
       // O config é um objeto único (não é uma lista)
       // Pega direto do MOCK_DB
       config = MOCK_DB.configuracoes || {};
       renderForm();
     } catch (err) {
       conteudo.innerHTML = `<div class="alert alert-erro">Erro: ${err.message}</div>`;
     }
   }
   
   function renderForm() {
     const conteudo = document.getElementById('conteudo');
   
     conteudo.innerHTML = `
       <div class="form-produto">
         <div class="form-header">
           <h1>Configurações da loja</h1>
           <div>
             <button class="btn btn-primary" onclick="salvar()" id="btn-salvar">Salvar alterações</button>
           </div>
         </div>
   
         <div id="alerta-config" class="alert alert-sucesso hidden"></div>
   
         <!-- ===== DADOS DA LOJA ===== -->
         <div class="card">
           <h3 class="secao-titulo">Dados da loja</h3>
           <div class="grid-2">
             <div class="field">
               <label>Nome fantasia *</label>
               <input type="text" id="c-nomeFantasia" value="${escapeAttr(config.nomeFantasia || '')}">
             </div>
             <div class="field">
               <label>Razão social</label>
               <input type="text" id="c-razaoSocial" value="${escapeAttr(config.razaoSocial || '')}">
             </div>
             <div class="field">
               <label>CNPJ</label>
               <input type="text" id="c-cnpj" value="${escapeAttr(config.cnpj || '')}" placeholder="00.000.000/0000-00">
             </div>
             <div class="field">
               <label>Telefone</label>
               <input type="text" id="c-telefone" value="${escapeAttr(config.telefone || '')}" placeholder="(71) 99999-9999">
             </div>
             <div class="field">
               <label>WhatsApp</label>
               <input type="text" id="c-whatsapp" value="${escapeAttr(config.whatsapp || '')}" placeholder="(71) 99999-9999">
             </div>
             <div class="field">
               <label>E-mail</label>
               <input type="email" id="c-email" value="${escapeAttr(config.email || '')}">
             </div>
             <div class="field">
               <label>E-mail para pedidos</label>
               <input type="email" id="c-emailPedidos" value="${escapeAttr(config.emailPedidos || '')}">
             </div>
           </div>
         </div>
   
         <!-- ===== REDES SOCIAIS ===== -->
         <div class="card">
           <h3 class="secao-titulo">Redes sociais</h3>
           <div class="grid-3">
             <div class="field">
               <label>Instagram</label>
               <input type="text" id="c-instagram" value="${escapeAttr(config.instagram || '')}" placeholder="@loja">
             </div>
             <div class="field">
               <label>Shopee</label>
               <input type="url" id="c-shopeeUrl" value="${escapeAttr(config.shopeeUrl || '')}" placeholder="https://...">
             </div>
             <div class="field">
               <label>Facebook</label>
               <input type="url" id="c-facebookUrl" value="${escapeAttr(config.facebookUrl || '')}" placeholder="https://...">
             </div>
           </div>
         </div>
   
         <!-- ===== ENDEREÇO ===== -->
         <div class="card">
           <h3 class="secao-titulo">Endereço da loja física</h3>
           <div class="grid-3">
             <div class="field">
               <label>CEP</label>
               <input type="text" id="c-cep" value="${escapeAttr(config.cep || '')}" placeholder="00000-000">
             </div>
             <div class="field" style="grid-column: span 2;">
               <label>Logradouro</label>
               <input type="text" id="c-logradouro" value="${escapeAttr(config.logradouro || '')}">
             </div>
             <div class="field">
               <label>Número</label>
               <input type="text" id="c-numero" value="${escapeAttr(config.numero || '')}">
             </div>
             <div class="field">
               <label>Complemento</label>
               <input type="text" id="c-complemento" value="${escapeAttr(config.complemento || '')}">
             </div>
             <div class="field">
               <label>Bairro</label>
               <input type="text" id="c-bairro" value="${escapeAttr(config.bairro || '')}">
             </div>
             <div class="field">
               <label>Cidade</label>
               <input type="text" id="c-cidade" value="${escapeAttr(config.cidade || '')}">
             </div>
             <div class="field">
               <label>UF</label>
               <input type="text" id="c-uf" value="${escapeAttr(config.uf || '')}" maxlength="2" placeholder="BA">
             </div>
           </div>
         </div>
   
         <!-- ===== CORES ===== -->
         <div class="card">
           <h3 class="secao-titulo">Cores da marca</h3>
           <p style="color:var(--texto-secundario);font-size:.9rem;margin-bottom:1rem;">
             Essas cores são usadas no app do cliente e no painel administrativo.
           </p>
   
           <div class="grid-2">
             <div class="field">
               <label>Cor primária (verde)</label>
               <div style="display:flex; gap:.5rem; align-items:center;">
                 <input type="color" id="c-corPrimaria-picker"
                        value="${escapeAttr(config.corPrimaria || '#37B6B0')}"
                        style="width:50px; height:40px; padding:0; border:1px solid var(--borda); border-radius:6px; cursor:pointer;">
                 <input type="text" id="c-corPrimaria" value="${escapeAttr(config.corPrimaria || '#37B6B0')}"
                        style="flex:1;" maxlength="7">
               </div>
             </div>
             <div class="field">
               <label>Cor de destaque (rosa)</label>
               <div style="display:flex; gap:.5rem; align-items:center;">
                 <input type="color" id="c-corDestaque-picker"
                        value="${escapeAttr(config.corDestaque || '#ED83A4')}"
                        style="width:50px; height:40px; padding:0; border:1px solid var(--borda); border-radius:6px; cursor:pointer;">
                 <input type="text" id="c-corDestaque" value="${escapeAttr(config.corDestaque || '#ED83A4')}"
                        style="flex:1;" maxlength="7">
               </div>
             </div>
           </div>
   
           <!-- Preview das cores -->
           <div style="margin-top:1.5rem;">
             <label style="font-size:.85rem;font-weight:600;color:var(--texto-secundario);display:block;margin-bottom:.5rem;">
               Preview
             </label>
             <div style="background:#fff; border:1px solid var(--borda); border-radius:var(--radius); overflow:hidden;">
               <div id="preview-header" style="background:${escapeAttr(config.corPrimaria || '#37B6B0')}; color:#fff; padding:1rem;">
                 <strong>ArthurBaby</strong> — Tudo para seu bebê!
               </div>
               <div style="padding:1rem; background:var(--superficie);">
                 <button id="preview-botao" style="background:${escapeAttr(config.corDestaque || '#ED83A4')}; color:#fff; border:none; padding:.5rem 1rem; border-radius:6px; font-weight:600; cursor:pointer;">
                   Comprar agora
                 </button>
               </div>
             </div>
           </div>
         </div>
       </div>
     `;
   
     // Sincroniza color pickers com os campos de texto
     sincronizarCor('c-corPrimaria', 'preview-header', 'background');
     sincronizarCor('c-corDestaque', 'preview-botao', 'background');
   }
   
   function sincronizarCor(campoId, previewId, propriedade) {
     const campo = document.getElementById(campoId);
     const picker = document.getElementById(campoId + '-picker');
     const preview = document.getElementById(previewId);
     if (!campo || !picker || !preview) return;
   
     picker.addEventListener('input', () => {
       campo.value = picker.value.toUpperCase();
       preview.style[propriedade] = picker.value;
     });
     campo.addEventListener('input', () => {
       if (/^#[0-9A-Fa-f]{6}$/.test(campo.value)) {
         picker.value = campo.value;
         preview.style[propriedade] = campo.value;
       }
     });
   }
   
   /* ============================================
      SALVAR
      ============================================ */
   async function salvar() {
     const alerta = document.getElementById('alerta-config');
     const btn = document.getElementById('btn-salvar');
   
     const getVal = (id) => document.getElementById(id)?.value ?? '';
   
     const novoConfig = {
       nomeFantasia: getVal('c-nomeFantasia').trim(),
       razaoSocial: getVal('c-razaoSocial').trim(),
       cnpj: getVal('c-cnpj').trim(),
       telefone: getVal('c-telefone').trim(),
       whatsapp: getVal('c-whatsapp').trim(),
       email: getVal('c-email').trim(),
       emailPedidos: getVal('c-emailPedidos').trim(),
       instagram: getVal('c-instagram').trim(),
       shopeeUrl: getVal('c-shopeeUrl').trim(),
       facebookUrl: getVal('c-facebookUrl').trim(),
       cep: getVal('c-cep').trim(),
       logradouro: getVal('c-logradouro').trim(),
       numero: getVal('c-numero').trim(),
       complemento: getVal('c-complemento').trim(),
       bairro: getVal('c-bairro').trim(),
       cidade: getVal('c-cidade').trim(),
       uf: getVal('c-uf').trim().toUpperCase(),
       corPrimaria: getVal('c-corPrimaria').trim().toUpperCase(),
       corDestaque: getVal('c-corDestaque').trim().toUpperCase(),
     };
   
     if (!novoConfig.nomeFantasia) {
       alerta.className = 'alert alert-erro';
       alerta.textContent = 'O nome fantasia é obrigatório.';
       alerta.classList.remove('hidden');
       return;
     }
   
     btn.disabled = true;
     btn.textContent = 'Salvando...';
   
     try {
       // Configurações não são uma lista — é um objeto único.
       // Salvamos direto no MOCK_DB e persistimos.
       MOCK_DB.configuracoes = novoConfig;
       salvarMock();
   
       alerta.className = 'alert alert-sucesso';
       alerta.textContent = '✅ Configurações salvas com sucesso!';
       alerta.classList.remove('hidden');
   
       window.scrollTo({ top: 0, behavior: 'smooth' });
       setTimeout(() => alerta.classList.add('hidden'), 3000);
     } catch (err) {
       alerta.className = 'alert alert-erro';
       alerta.textContent = 'Erro ao salvar: ' + err.message;
       alerta.classList.remove('hidden');
     } finally {
       btn.disabled = false;
       btn.textContent = 'Salvar alterações';
     }
   }
   
   /* ============================================
      HELPERS
      ============================================ */
   function escapeHtml(s) {
     return String(s ?? '').replace(/[&<>"']/g, m => ({
       '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'
     }[m]));
   }
   function escapeAttr(s) { return escapeHtml(s); }
   
   /* ============================================
      INIT
      ============================================ */
   document.addEventListener('DOMContentLoaded', () => {
     if (montarLayout('Configurações', 'configuracoes')) carregar();
   });