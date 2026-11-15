package br.com.arthurbaby.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.com.arthurbaby.R;
import br.com.arthurbaby.adapters.ItemCarrinhoAdapter;
import br.com.arthurbaby.models.ItemCarrinho;
import br.com.arthurbaby.network.ApiService;
import br.com.arthurbaby.network.RetrofitClient;
import br.com.arthurbaby.network.TokenStorage;
import br.com.arthurbaby.network.dto.CupomValidacaoRequest;
import br.com.arthurbaby.network.dto.CupomValidacaoResponse;
import br.com.arthurbaby.network.dto.EnderecoResponse;
import br.com.arthurbaby.network.dto.PedidoItemRequest;
import br.com.arthurbaby.network.dto.PedidoRequest;
import br.com.arthurbaby.network.dto.PedidoResponse;
import br.com.arthurbaby.repositories.CarrinhoRepository;
import br.com.arthurbaby.utils.AuthGuard;
import br.com.arthurbaby.utils.ErrorUtils;
import br.com.arthurbaby.utils.LoadingUtils;
import br.com.arthurbaby.utils.MoedaUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarrinhoFragment extends Fragment {

    private RecyclerView rvCarrinho;
    private TextView tvSubtotal, tvFrete, tvTotal, tvContador;
    private View tvVazio;
    private ItemCarrinhoAdapter adapter;
    private CarrinhoRepository repo;
    private double descontoAplicado = 0;
    private String cupomAplicado = null;
    private boolean freteGratisPorCupom = false;
    /** Subtotal sobre o qual o cupom foi validado; se o carrinho mudar, o cupom precisa ser revalidado. */
    private double subtotalDoCupom = 0;
    private LinearLayout rowDesconto;

    // Endereço de entrega: lista do cliente e o escolhido (padrão: o principal)
    private View layoutEndereco, btnTrocarEndereco;
    private TextView tvEnderecoEntrega;
    private List<EnderecoResponse> enderecos = null; // null = ainda não carregou (ou falhou)
    private Long enderecoSelecionadoId = null;
    private boolean carregandoEnderecos = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_carrinho, container, false);

        v.findViewById(R.id.btnVoltarCarrinho).setOnClickListener(x ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        repo = CarrinhoRepository.getInstance();

        rvCarrinho = v.findViewById(R.id.rvCarrinho);
        tvSubtotal = v.findViewById(R.id.tvSubtotal);
        tvFrete = v.findViewById(R.id.tvFrete);
        tvTotal = v.findViewById(R.id.tvTotal);
        tvVazio = v.findViewById(R.id.tvVazio);
        tvContador = v.findViewById(R.id.tvContadorItens);

        rvCarrinho.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ItemCarrinhoAdapter(repo.getItens(), this::atualizarTotal);
        rvCarrinho.setAdapter(adapter);

        EditText etObservacao = v.findViewById(R.id.etObservacao);
        EditText etCupom = v.findViewById(R.id.etCupom);
        RadioGroup rgRecebimento = v.findViewById(R.id.rgRecebimento);
        RadioButton rbEntrega = v.findViewById(R.id.rbEntrega);
        RadioButton rbRetirada = v.findViewById(R.id.rbRetirada);
        rowDesconto = v.findViewById(R.id.rowDesconto);
        TextView tvDesconto = v.findViewById(R.id.tvDesconto);

        layoutEndereco = v.findViewById(R.id.layoutEnderecoEntrega);
        btnTrocarEndereco = v.findViewById(R.id.btnTrocarEndereco);
        tvEnderecoEntrega = v.findViewById(R.id.tvEnderecoEntrega);
        btnTrocarEndereco.setOnClickListener(x -> escolherEndereco());
        tvEnderecoEntrega.setOnClickListener(x -> escolherEndereco());

        if ("ENTREGA".equals(repo.getFormaRecebimento())) rbEntrega.setChecked(true);
        else rbRetirada.setChecked(true);

        rgRecebimento.setOnCheckedChangeListener((group, checkedId) -> {
            repo.setFormaRecebimento(checkedId == R.id.rbEntrega ? "ENTREGA" : "RETIRADA_LOJA");
            atualizarTotal();
            atualizarCardEndereco();
            if (ehEntrega() && enderecos == null) carregarEnderecos();
        });

        // CUPOM — via API (dinâmico)
        v.findViewById(R.id.btnAplicarCupom).setOnClickListener(x -> {
            String cupom = etCupom.getText().toString().trim().toUpperCase();
            if (cupom.isEmpty()) {
                Toast.makeText(requireContext(), "Digite o cupom", Toast.LENGTH_SHORT).show();
                return;
            }

            LoadingUtils.mostrar(requireContext());

            ApiService api = RetrofitClient.getApi(requireContext());
            api.validarCupom(new CupomValidacaoRequest(
                            cupom, BigDecimal.valueOf(repo.getSubtotal())))
                    .enqueue(new Callback<CupomValidacaoResponse>() {
                        @Override
                        public void onResponse(Call<CupomValidacaoResponse> call,
                                               Response<CupomValidacaoResponse> response) {
                            LoadingUtils.esconder();

                            if (!isAdded()) return;

                            if (response.isSuccessful() && response.body() != null) {
                                CupomValidacaoResponse c = response.body();

                                if (c.valido) {
                                    descontoAplicado = c.desconto != null ? c.desconto.doubleValue() : 0;
                                    cupomAplicado = c.codigo;
                                    subtotalDoCupom = repo.getSubtotal();
                                    freteGratisPorCupom = c.freteGratis;

                                    if (descontoAplicado > 0) {
                                        rowDesconto.setVisibility(View.VISIBLE);
                                        tvDesconto.setText("- " + MoedaUtils.formatar(descontoAplicado));
                                    } else {
                                        rowDesconto.setVisibility(View.GONE);
                                    }

                                    String msg = c.descricao != null ? c.descricao : "Cupom aplicado!";
                                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                                } else {
                                    descontoAplicado = 0;
                                    cupomAplicado = null;
                                    freteGratisPorCupom = false;
                                    rowDesconto.setVisibility(View.GONE);

                                    String motivo = c.motivo != null ? c.motivo : "Cupom inválido";
                                    Toast.makeText(requireContext(), motivo, Toast.LENGTH_SHORT).show();
                                }
                                atualizarTotal();
                            } else {
                                Toast.makeText(requireContext(),
                                        "Erro ao validar cupom", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<CupomValidacaoResponse> call, Throwable t) {
                            LoadingUtils.esconder();
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(),
                                    ErrorUtils.mensagemFalha(t),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // === FINALIZAR PEDIDO ===
        v.findViewById(R.id.btnFinalizar).setOnClickListener(x -> {

            if (repo.getItens().isEmpty()) {
                Toast.makeText(requireContext(), "Carrinho vazio!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!AuthGuard.estaLogado(requireContext())) {
                AuthGuard.mostrarDialogLogin(requireActivity(),
                        "Entre para finalizar seu pedido. Seus itens foram salvos!");
                return;
            }

            Long clienteId = TokenStorage.getUsuarioId(requireContext());
            if (clienteId == null) {
                Toast.makeText(requireContext(),
                        "Sessão expirada. Faça login novamente.", Toast.LENGTH_LONG).show();
                AuthGuard.mostrarDialogLogin(requireActivity(), null);
                return;
            }

            String observacao = etObservacao.getText().toString().trim();
            String forma = repo.getFormaRecebimento();

            Long enderecoId = null;
            if ("ENTREGA".equals(forma)) {
                if (enderecos == null) {
                    Toast.makeText(requireContext(), carregandoEnderecos
                            ? "Aguarde: carregando seus endereços."
                            : "Não foi possível carregar seus endereços. Toque no endereço para tentar de novo.",
                            Toast.LENGTH_LONG).show();
                    if (!carregandoEnderecos) carregarEnderecos();
                    return;
                }
                EnderecoResponse escolhido = enderecoSelecionado();
                if (escolhido == null) {
                    Toast.makeText(requireContext(),
                            "Cadastre um endereço para receber o pedido.", Toast.LENGTH_LONG).show();
                    abrirEnderecos();
                    return;
                }
                enderecoId = escolhido.id;
            }

            List<PedidoItemRequest> itensReq = new ArrayList<>();
            for (ItemCarrinho item : repo.getItens()) {
                itensReq.add(new PedidoItemRequest(
                        item.getProduto().getId(),
                        item.getVariacaoId(),
                        item.getQuantidade()
                ));
            }

            PedidoRequest request = new PedidoRequest(
                    clienteId,
                    forma,
                    cupomAplicado,
                    BigDecimal.valueOf(descontoAplicado),
                    BigDecimal.valueOf(freteGratisPorCupom ? 0 : repo.getFrete()),
                    observacao,
                    itensReq,
                    enderecoId
            );

            LoadingUtils.mostrar(requireContext());

            ApiService api = RetrofitClient.getApi(requireContext());
            api.criarPedido(request).enqueue(new Callback<PedidoResponse>() {
                @Override
                public void onResponse(Call<PedidoResponse> call, Response<PedidoResponse> response) {
                    LoadingUtils.esconder();

                    // Pedido criado: esvazia o carrinho mesmo que o usuário já tenha saído da tela,
                    // para não permitir enviar o mesmo pedido de novo
                    if (response.isSuccessful() && response.body() != null) repo.limpar();

                    if (!isAdded()) return;

                    if (response.isSuccessful() && response.body() != null) {
                        PedidoResponse pedido = response.body();

                        ConfirmaPedidoFragment frag = ConfirmaPedidoFragment.newInstance(
                                pedido.numero,
                                forma,
                                observacao,
                                pedido.total != null ? pedido.total.doubleValue() : 0,
                                pedido.subtotal != null ? pedido.subtotal.doubleValue() : 0,
                                pedido.frete != null ? pedido.frete.doubleValue() : 0,
                                pedido.desconto != null ? pedido.desconto.doubleValue() : descontoAplicado,
                                pedido.cupom != null ? pedido.cupom : cupomAplicado,
                                formatarEndereco(pedido.enderecoEntrega)
                        );
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.frameContainer, frag)
                                .addToBackStack(null)
                                .commit();
                    } else {
                        Toast.makeText(requireContext(),
                                ErrorUtils.extrairMensagem(response), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<PedidoResponse> call, Throwable t) {
                    LoadingUtils.esconder();
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(),
                            ErrorUtils.mensagemFalha(t), Toast.LENGTH_LONG).show();
                }
            });
        });

        atualizarTotal();
        atualizarCardEndereco();
        return v;
    }

    // ================= ENDEREÇO DE ENTREGA =================

    private boolean ehEntrega() {
        return "ENTREGA".equals(repo.getFormaRecebimento());
    }

    /** Busca os endereços do cliente; mantém o escolhido se ainda existir, senão usa o principal. */
    private void carregarEnderecos() {
        if (!isAdded() || !AuthGuard.estaLogado(requireContext())) return;
        Long clienteId = TokenStorage.getUsuarioId(requireContext());
        if (clienteId == null) return;

        carregandoEnderecos = true;
        atualizarCardEndereco();
        RetrofitClient.getApi(requireContext()).listarEnderecos(clienteId)
                .enqueue(new Callback<List<EnderecoResponse>>() {
                    @Override
                    public void onResponse(Call<List<EnderecoResponse>> call, Response<List<EnderecoResponse>> response) {
                        carregandoEnderecos = false;
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            enderecos = response.body();
                            if (enderecoSelecionado() == null) enderecoSelecionadoId = idDoPadrao(enderecos);
                        } else {
                            enderecos = null;
                        }
                        atualizarCardEndereco();
                    }

                    @Override
                    public void onFailure(Call<List<EnderecoResponse>> call, Throwable t) {
                        carregandoEnderecos = false;
                        if (!isAdded()) return;
                        enderecos = null;
                        atualizarCardEndereco();
                    }
                });
    }

    private static Long idDoPadrao(List<EnderecoResponse> lista) {
        if (lista.isEmpty()) return null;
        for (EnderecoResponse e : lista) if (e.principal) return e.id;
        return lista.get(0).id;
    }

    @Nullable
    private EnderecoResponse enderecoSelecionado() {
        if (enderecos == null || enderecoSelecionadoId == null) return null;
        for (EnderecoResponse e : enderecos) if (enderecoSelecionadoId.equals(e.id)) return e;
        return null;
    }

    private void atualizarCardEndereco() {
        if (layoutEndereco == null) return;
        if (!ehEntrega()) {
            layoutEndereco.setVisibility(View.GONE);
            return;
        }
        layoutEndereco.setVisibility(View.VISIBLE);

        boolean logado = isAdded() && AuthGuard.estaLogado(requireContext());
        String texto;
        boolean mostrarTrocar = false;
        if (!logado) {
            texto = "Entre na sua conta para escolher o endereço.";
        } else if (carregandoEnderecos && enderecos == null) {
            texto = "Carregando seus endereços…";
        } else if (enderecos == null) {
            texto = "Não foi possível carregar seus endereços. Toque para tentar de novo.";
        } else if (enderecos.isEmpty()) {
            texto = "Nenhum endereço cadastrado. Toque para cadastrar.";
        } else {
            EnderecoResponse e = enderecoSelecionado();
            texto = formatarEndereco(e) + (e != null && e.principal ? "\n(endereço principal)" : "");
            mostrarTrocar = true;
        }
        tvEnderecoEntrega.setText(texto);
        btnTrocarEndereco.setVisibility(mostrarTrocar ? View.VISIBLE : View.GONE);
    }

    /** Toque no card: login, nova tentativa, cadastro ou lista para escolher, conforme o caso. */
    private void escolherEndereco() {
        if (!AuthGuard.estaLogado(requireContext())) {
            AuthGuard.mostrarDialogLogin(requireActivity(), "Entre para escolher o endereço de entrega.");
            return;
        }
        if (enderecos == null) {
            if (!carregandoEnderecos) carregarEnderecos();
            return;
        }
        if (enderecos.isEmpty()) {
            abrirEnderecos();
            return;
        }

        String[] opcoes = new String[enderecos.size()];
        int marcado = 0;
        for (int i = 0; i < enderecos.size(); i++) {
            EnderecoResponse e = enderecos.get(i);
            opcoes[i] = formatarEndereco(e) + (e.principal ? "\n(principal)" : "");
            if (e.id != null && e.id.equals(enderecoSelecionadoId)) marcado = i;
        }
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Entregar em")
                .setSingleChoiceItems(opcoes, marcado, (d, which) -> {
                    enderecoSelecionadoId = enderecos.get(which).id;
                    atualizarCardEndereco();
                    d.dismiss();
                })
                .setNeutralButton("Novo endereço", (d, w) -> abrirEnderecos())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /** Abre "Meus endereços"; ao voltar, o onResume recarrega a lista. */
    private void abrirEnderecos() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameContainer, new EnderecosFragment())
                .addToBackStack(null)
                .commit();
    }

    /** "Rua X, 10 - Apto 2 / Bairro - Cidade/UF / CEP 00000-000". */
    static String formatarEndereco(@Nullable EnderecoResponse e) {
        if (e == null) return "";
        return montarEndereco(e.logradouro, e.numero, e.complemento, e.bairro, e.cidade, e.uf, e.cep);
    }

    static String formatarEndereco(@Nullable br.com.arthurbaby.network.dto.EnderecoEntregaResponse e) {
        if (e == null) return null;
        return montarEndereco(e.logradouro, e.numero, e.complemento, e.bairro, e.cidade, e.uf, e.cep);
    }

    private static String montarEndereco(String logradouro, String numero, String complemento,
                                         String bairro, String cidade, String uf, String cep) {
        StringBuilder sb = new StringBuilder();
        sb.append(vazio(logradouro) ? "Endereço" : logradouro);
        if (!vazio(numero)) sb.append(", ").append(numero);
        if (!vazio(complemento)) sb.append(" - ").append(complemento);
        StringBuilder linha2 = new StringBuilder();
        if (!vazio(bairro)) linha2.append(bairro);
        if (!vazio(cidade)) {
            if (linha2.length() > 0) linha2.append(" - ");
            linha2.append(cidade);
            if (!vazio(uf)) linha2.append("/").append(uf);
        }
        if (linha2.length() > 0) sb.append("\n").append(linha2);
        if (!vazio(cep)) {
            String d = cep.replaceAll("\\D", "");
            sb.append("\nCEP ").append(d.length() == 8 ? d.substring(0, 5) + "-" + d.substring(5) : cep);
        }
        return sb.toString();
    }

    private static boolean vazio(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void atualizarTotal() {
        repo.salvarAlteracoes(); // persiste +/- e atualiza o contador do ícone
        double subtotal = repo.getSubtotal();

        // Itens alterados depois de aplicar o cupom: o desconto exibido ficaria errado
        if (cupomAplicado != null && Math.abs(subtotal - subtotalDoCupom) > 0.001) {
            cupomAplicado = null;
            descontoAplicado = 0;
            freteGratisPorCupom = false;
            if (rowDesconto != null) rowDesconto.setVisibility(View.GONE);
            if (isAdded()) {
                Toast.makeText(requireContext(),
                        "Carrinho alterado. Aplique o cupom novamente.", Toast.LENGTH_SHORT).show();
            }
        }
        double frete = freteGratisPorCupom ? 0 : repo.getFrete();
        double total = subtotal + frete - descontoAplicado;

        tvSubtotal.setText(MoedaUtils.formatar(subtotal));
        tvFrete.setText(frete == 0 ? "Grátis" : MoedaUtils.formatar(frete));
        tvTotal.setText(MoedaUtils.formatar(total));
        tvVazio.setVisibility(repo.getItens().isEmpty() ? View.VISIBLE : View.GONE);
        tvContador.setText(repo.getTotalItens() + (repo.getTotalItens() == 1 ? " item" : " itens"));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) atualizarTotal();
        removerProdutosIndisponiveis();
        // Recarrega sempre: a pessoa pode ter cadastrado/apagado endereço ou entrado na conta
        if (repo != null && ehEntrega()) carregarEnderecos();
        else atualizarCardEndereco();
    }

    /**
     * O carrinho fica salvo no aparelho: um produto desativado pela loja depois de adicionado
     * continuaria aparecendo. Ao abrir o carrinho, cada produto é conferido na API (404 = indisponível)
     * e os itens dele são retirados, com aviso.
     */
    private void removerProdutosIndisponiveis() {
        if (repo == null || repo.getItens().isEmpty() || !isAdded()) return;
        java.util.Set<Long> ids = new java.util.LinkedHashSet<>();
        for (ItemCarrinho item : repo.getItens()) {
            if (item.getProduto() != null && item.getProduto().getId() != null) ids.add(item.getProduto().getId());
        }
        ApiService api = RetrofitClient.getApi(requireContext());
        for (Long produtoId : ids) {
            api.buscarProduto(produtoId).enqueue(new Callback<br.com.arthurbaby.network.dto.ProdutoResponse>() {
                @Override
                public void onResponse(Call<br.com.arthurbaby.network.dto.ProdutoResponse> call,
                                       Response<br.com.arthurbaby.network.dto.ProdutoResponse> response) {
                    if (!isAdded() || response.code() != 404) return;
                    String nome = null;
                    for (ItemCarrinho item : new ArrayList<>(repo.getItens())) {
                        if (item.getProduto() != null && produtoId.equals(item.getProduto().getId())) {
                            nome = item.getProduto().getNome();
                            repo.remover(item);
                        }
                    }
                    if (nome != null) {
                        Toast.makeText(requireContext(),
                                nome + " foi retirado do carrinho: não está mais disponível.",
                                Toast.LENGTH_LONG).show();
                        atualizarTotal();
                    }
                }

                @Override
                public void onFailure(Call<br.com.arthurbaby.network.dto.ProdutoResponse> call, Throwable t) {
                    // Sem conexão: mantém o carrinho; a API confere tudo de novo ao finalizar o pedido
                }
            });
        }
    }
}