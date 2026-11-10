/* ============================================
   configuracoes.js — configurações da loja
   ============================================ */

   let config = {};

   async function carregar() {
     const conteudo = document.getElementById('conteudo');
     conteudo.innerHTML = '<div class="loading">Carregando...</div>';
   
     try {
       // A configuração é um registro único na tabela configuracao_loja
       const lista = await adminApi.listar('configuracoes-loja');
       config = lista[0] || {};
       renderForm();
     } catch (err) {
       conteudo.innerHTML = `<div class="alert alert-erro">Erro: ${err.message}</div>`;
     }
   }
   
   function renderForm() {
     const conteudo = document.getElementById('conteudo');
   
     const corP = escapeAttr(config.corPrimaria || '#37B6B0');
     const corD = escapeAttr(config.corDestaque || '#ED83A4');
     conteudo.innerHTML = `
       <div class="form-produto form-compacto">
         <div class="form-header">
           <p class="texto-sec">
             Dados exibidos no app do cliente e usados nos contatos da loja.
             ${config.atualizadoEm ? `Última alteração: ${new Date(config.atualizadoEm).toLocaleString('pt-BR')}.` : ''}
           </p>
           <div>
             <button class="btn btn-primary" onclick="salvar()" id="btn-salvar">Salvar alterações</button>
           </div>
         </div>

         <div id="alerta-config" class="alert alert-sucesso hidden"></div>

         <div class="card">
           <h3 class="secao-titulo">Dados da loja, contato e redes sociais</h3>
           <div class="grid-4">
             <div class="field">
               <label for="c-nomeFantasia">Nome fantasia *</label>
               <input type="text" id="c-nomeFantasia" value="${escapeAttr(config.nomeFantasia || '')}">
             </div>
             <div class="field">
               <label for="c-razaoSocial">Razão social</label>
               <input type="text" id="c-razaoSocial" value="${escapeAttr(config.razaoSocial || '')}">
             </div>
             <div class="field">
               <label for="c-cnpj">CNPJ</label>
               <input type="text" id="c-cnpj" value="${escapeAttr(config.cnpj || '')}" placeholder="00.000.000/0000-00">
             </div>
             <div class="field">
               <label for="c-telefone">Telefone</label>
               <input type="text" id="c-telefone" value="${escapeAttr(config.telefone || '')}" placeholder="(71) 99999-9999">
             </div>
             <div class="field">
               <label for="c-whatsapp">WhatsApp</label>
               <input type="text" id="c-whatsapp" value="${escapeAttr(config.whatsapp || '')}" placeholder="(71) 99999-9999">
             </div>
             <div class="field">
               <label for="c-email">E-mail</label>
               <input type="email" id="c-email" value="${escapeAttr(config.email || '')}">
             </div>
             <div class="field">
               <label for="c-emailPedidos">E-mail para pedidos</label>
               <input type="email" id="c-emailPedidos" value="${escapeAttr(config.emailPedidos || '')}">
             </div>
             <div class="field">
               <label for="c-instagram">Instagram</label>
               <input type="text" id="c-instagram" value="${escapeAttr(config.instagram || '')}" placeholder="@loja">
             </div>
             <div class="field" style="grid-column: span 2;">
               <label for="c-shopeeUrl">Shopee</label>
               <input type="url" id="c-shopeeUrl" value="${escapeAttr(config.shopeeUrl || '')}" placeholder="https://...">
             </div>
             <div class="field" style="grid-column: span 2;">
               <label for="c-facebookUrl">Facebook</label>
               <input type="url" id="c-facebookUrl" value="${escapeAttr(config.facebookUrl || '')}" placeholder="https://...">
             </div>
           </div>
         </div>

         <div class="linha-cards">
           <div class="card">
             <h3 class="secao-titulo">Endereço da loja física</h3>
             <div class="grid-4">
               <div class="field">
                 <label for="c-cep">CEP</label>
                 <input type="text" id="c-cep" value="${escapeAttr(config.cep || '')}" placeholder="00000-000">
               </div>
               <div class="field" style="grid-column: span 3;">
                 <label for="c-logradouro">Logradouro</label>
                 <input type="text" id="c-logradouro" value="${escapeAttr(config.logradouro || '')}">
               </div>
               <div class="field">
                 <label for="c-numero">Número</label>
                 <input type="text" id="c-numero" value="${escapeAttr(config.numero || '')}">
               </div>
               <div class="field" style="grid-column: span 3;">
                 <label for="c-complemento">Complemento</label>
                 <input type="text" id="c-complemento" value="${escapeAttr(config.complemento || '')}">
               </div>
               <div class="field" style="grid-column: span 2;">
                 <label for="c-bairro">Bairro</label>
                 <input type="text" id="c-bairro" value="${escapeAttr(config.bairro || '')}">
               </div>
               <div class="field">
                 <label for="c-cidade">Cidade</label>
                 <input type="text" id="c-cidade" value="${escapeAttr(config.cidade || '')}">
               </div>
               <div class="field">
                 <label for="c-uf">UF</label>
                 <input type="text" id="c-uf" value="${escapeAttr(config.uf || '')}" maxlength="2" placeholder="BA">
               </div>
             </div>
           </div>

           <div class="card">
             <h3 class="secao-titulo">Cores da marca (app e painel)</h3>
             <div class="grid-2">
               <div class="field">
                 <label for="c-corPrimaria">Cor primária (verde)</label>
                 <div class="cor-campo">
                   <input type="color" id="c-corPrimaria-picker" value="${corP}" aria-label="Escolher cor primária">
                   <input type="text" id="c-corPrimaria" value="${corP}" maxlength="7">
                 </div>
               </div>
               <div class="field">
                 <label for="c-corDestaque">Cor de destaque (rosa)</label>
                 <div class="cor-campo">
                   <input type="color" id="c-corDestaque-picker" value="${corD}" aria-label="Escolher cor de destaque">
                   <input type="text" id="c-corDestaque" value="${corD}" maxlength="7">
                 </div>
               </div>
             </div>
             <div class="preview-cores">
               <div id="preview-header" style="background:${corP};">
                 <strong>ArthurBaby</strong> — Tudo para seu bebê!
               </div>
               <div class="preview-corpo">
                 <span class="texto-mini">Prévia</span>
                 <button type="button" id="preview-botao" style="background:${corD};">Comprar agora</button>
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
       // Mantém campos que esta tela não edita (logo, demais cores) mesclando com o registro atual
       const corpo = { ...config, ...novoConfig };
       config = config.id
         ? await adminApi.atualizar('configuracoes-loja', config.id, corpo)
         : await adminApi.criar('configuracoes-loja', corpo);
   
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