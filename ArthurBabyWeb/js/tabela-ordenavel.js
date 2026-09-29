/* ============================================
   tabela-ordenavel.js
   Torna tabelas com .tabela-ordenavel ordenáveis.
   Basta adicionar data-ordenavel="nome-do-campo" nos <th>
   e data-campo="nome-do-campo" nos <td>.
   ============================================ */

   (function () {
    document.addEventListener('click', (e) => {
      const th = e.target.closest('th[data-ordenavel]');
      if (!th) return;
  
      const tabela = th.closest('table');
      if (!tabela) return;
  
      const campo = th.dataset.ordenavel;
      const tbody = tabela.querySelector('tbody');
      if (!tbody) return;
  
      const ordemAtual = th.classList.contains('ordem-asc') ? 'asc'
                       : th.classList.contains('ordem-desc') ? 'desc'
                       : null;
  
      tabela.querySelectorAll('th[data-ordenavel]').forEach(h => {
        h.classList.remove('ordem-asc', 'ordem-desc');
      });
  
      const novaOrdem = ordemAtual === 'asc' ? 'desc' : 'asc';
      th.classList.add('ordem-' + novaOrdem);
  
      const linhas = Array.from(tbody.querySelectorAll('tr'));
      if (linhas.length === 0) return;
  
      const primeiraCelula = linhas[0].querySelector(`[data-campo="${campo}"]`);
      const valor = primeiraCelula ? primeiraCelula.textContent.trim() : '';
  
      let tipo = 'texto';
      if (/^R?\$?\s*[\d.,]+$/.test(valor)) tipo = 'numero';
      else if (/^\d{2}\/\d{2}\/\d{4}/.test(valor)) tipo = 'data';
  
      linhas.sort((a, b) => {
        const aCell = a.querySelector(`[data-campo="${campo}"]`);
        const bCell = b.querySelector(`[data-campo="${campo}"]`);
        if (!aCell || !bCell) return 0;
  
        let aVal = aCell.textContent.trim();
        let bVal = bCell.textContent.trim();
  
        if (tipo === 'numero') {
          aVal = parseFloat(aVal.replace(/[^\d,.-]/g, '').replace(/\./g, '').replace(',', '.')) || 0;
          bVal = parseFloat(bVal.replace(/[^\d,.-]/g, '').replace(/\./g, '').replace(',', '.')) || 0;
          return novaOrdem === 'asc' ? aVal - bVal : bVal - aVal;
        }
  
        if (tipo === 'data') {
          const parseData = (s) => {
            const [d, h] = s.split(' ');
            const [dia, mes, ano] = d.split('/');
            const [hora, min] = (h || '00:00').split(':');
            return new Date(ano, mes - 1, dia, hora, min).getTime();
          };
          aVal = parseData(aVal);
          bVal = parseData(bVal);
          return novaOrdem === 'asc' ? aVal - bVal : bVal - aVal;
        }
  
        return novaOrdem === 'asc'
          ? aVal.localeCompare(bVal, 'pt-BR')
          : bVal.localeCompare(aVal, 'pt-BR');
      });
  
      linhas.forEach(linha => tbody.appendChild(linha));
    });
  })();