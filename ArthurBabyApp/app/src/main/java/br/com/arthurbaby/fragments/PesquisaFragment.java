package br.com.arthurbaby.fragments;

import br.com.arthurbaby.utils.ErrorUtils;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.arthurbaby.R;
import br.com.arthurbaby.adapters.ProdutoAdapter;
import br.com.arthurbaby.network.ApiService;
import br.com.arthurbaby.network.Conversor;
import br.com.arthurbaby.network.RetrofitClient;
import br.com.arthurbaby.network.dto.PageResponse;
import br.com.arthurbaby.network.dto.ProdutoResponse;
import br.com.arthurbaby.utils.BuscaStorage;
import br.com.arthurbaby.utils.LoadingView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PesquisaFragment extends Fragment {

    private RecyclerView rvResultados;
    private View progressBar;
    private TextView tvInfo, chipTodas, chipPromocao, chipBaratos, chipLimpar;
    private EditText etBusca;
    private ProdutoAdapter adapter;

    private View containerHistorico;
    private LinearLayout containerChipsHistorico;
    private View estadoVazio;
    private TextView tvVazioTitulo, tvVazioTexto;
    private TextView btnLimparHistorico;

    private String filtroAtual = "TODAS";
    private String termoAtual = "";

    /** Espera o usuário parar de digitar antes de chamar a API (evita 1 requisição por letra). */
    private static final long DEBOUNCE_MS = 400;
    private final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable buscaAgendada = () -> buscarProdutos(termoAtual, null);
    private Call<PageResponse<ProdutoResponse>> chamadaAtual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_pesquisa, container, false);

        etBusca = v.findViewById(R.id.etBusca);
        tvInfo = v.findViewById(R.id.tvResultadoInfo);
        estadoVazio = v.findViewById(R.id.estadoVazio);
        tvVazioTitulo = v.findViewById(R.id.tvVazioTitulo);
        tvVazioTexto = v.findViewById(R.id.tvVazioTexto);
        rvResultados = v.findViewById(R.id.rvResultados);
        progressBar = v.findViewById(R.id.progressBar);
        chipTodas = v.findViewById(R.id.chipTodas);
        chipPromocao = v.findViewById(R.id.chipPromocao);
        chipBaratos = v.findViewById(R.id.chipBaratos);
        chipLimpar = v.findViewById(R.id.chipLimpar);

        containerHistorico = v.findViewById(R.id.containerHistorico);
        containerChipsHistorico = v.findViewById(R.id.containerChipsHistorico);
        btnLimparHistorico = v.findViewById(R.id.btnLimparHistorico);

        v.findViewById(R.id.btnVoltarBusca).setOnClickListener(x ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        rvResultados.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Busca via Enter/actionSearch
        etBusca.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String termo = etBusca.getText().toString().trim();
                if (!termo.isEmpty()) {
                    BuscaStorage.adicionar(requireContext(), termo);
                    mostrarHistorico(false);
                    handler.removeCallbacks(buscaAgendada);
                    buscarProdutos(termoAtual, null);
                }
                return true;
            }
            return false;
        });

        // Botão limpar histórico
        btnLimparHistorico.setOnClickListener(x -> {
            BuscaStorage.limpar(requireContext());
            mostrarHistorico(false);
        });

        // Escuta digitação
        etBusca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                termoAtual = s.toString();
                if (termoAtual.trim().isEmpty()) {
                    handler.removeCallbacks(buscaAgendada);
                    cancelarChamada();
                    mostrarHistorico(true);
                    rvResultados.setVisibility(View.GONE);
                    tvInfo.setText("Digite para buscar");
                    mostrarEstadoInicial();
                } else {
                    mostrarHistorico(false);
                    pintarChips();
                    handler.removeCallbacks(buscaAgendada);
                    handler.postDelayed(buscaAgendada, DEBOUNCE_MS);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        chipTodas.setOnClickListener(x -> { filtroAtual = "TODAS"; aplicarFiltro(); });
        chipPromocao.setOnClickListener(x -> { filtroAtual = "PROMOCAO"; aplicarFiltro(); });
        chipBaratos.setOnClickListener(x -> { filtroAtual = "BARATOS"; aplicarFiltro(); });
        chipLimpar.setOnClickListener(x -> {
            filtroAtual = "TODAS";
            pintarChips();
            etBusca.setText("");
            termoAtual = "";
            mostrarHistorico(true);
            rvResultados.setVisibility(View.GONE);
            tvInfo.setText("Digite para buscar");
            mostrarEstadoInicial();
        });

        // Estado inicial
        pintarChips();
        pintarChipLimpar();
        mostrarHistorico(true);
        rvResultados.setVisibility(View.GONE);
        tvInfo.setText("Digite para buscar");
        mostrarEstadoInicial();

        return v;
    }

    /**
     * Mostra ou esconde o bloco de buscas recentes.
     */
    private void mostrarHistorico(boolean mostrar) {
        if (!mostrar) {
            containerHistorico.setVisibility(View.GONE);
            return;
        }

        List<String> historico = BuscaStorage.getHistorico(requireContext());

        if (historico.isEmpty()) {
            containerHistorico.setVisibility(View.GONE);
            return;
        }

        containerHistorico.setVisibility(View.VISIBLE);
        containerChipsHistorico.removeAllViews();

        for (String termo : historico) {
            TextView chip = criarChipHistorico(termo);
            containerChipsHistorico.addView(chip);
        }
    }

    /**
     * Cria um chip clicável de busca recente.
     */
    private TextView criarChipHistorico(String termo) {
        TextView chip = new TextView(getContext());
        chip.setText(termo);
        chip.setPadding(dp(16), dp(8), dp(16), dp(8));
        chip.setTextSize(13f);
        chip.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(20));
        bg.setColor(ContextCompat.getColor(requireContext(), R.color.branco));
        bg.setStroke(dp(1), ContextCompat.getColor(requireContext(), R.color.rosa_claro));
        chip.setBackground(bg);
        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.rosa_escuro));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, dp(8), 0);
        chip.setLayoutParams(lp);

        chip.setOnClickListener(x -> {
            etBusca.setText(termo);
            etBusca.setSelection(termo.length());
            mostrarHistorico(false);
            aplicarFiltro();
        });

        return chip;
    }

    private void aplicarFiltro() {
        pintarChips();
        handler.removeCallbacks(buscaAgendada);
        buscarProdutos(termoAtual, null);
    }

    private void pintarChips() {
        pintarChip(chipTodas, "TODAS".equals(filtroAtual));
        pintarChip(chipPromocao, "PROMOCAO".equals(filtroAtual));
        pintarChip(chipBaratos, "BARATOS".equals(filtroAtual));
    }

    private void cancelarChamada() {
        if (chamadaAtual != null) chamadaAtual.cancel();
        chamadaAtual = null;
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    /** Teto do filtro "Baratos". */
    private static final java.math.BigDecimal PRECO_BARATO = new java.math.BigDecimal("50");

    private void buscarProdutos(String busca, Long categoriaId) {
        if (!isAdded()) return;
        // Uma resposta antiga não pode sobrescrever o resultado da busca mais recente
        cancelarChamada();
        estadoVazio.setVisibility(View.GONE);
        LoadingView.mostrar(progressBar, rvResultados);

        String termo = busca != null && !busca.trim().isEmpty() ? busca.trim() : null;
        // Filtros feitos pela própria API (antes "Promoção" era qualquer produto abaixo de R$ 50)
        Boolean promocao = "PROMOCAO".equals(filtroAtual) ? Boolean.TRUE : null;
        java.math.BigDecimal precoMax = "BARATOS".equals(filtroAtual) ? PRECO_BARATO : null;

        ApiService api = RetrofitClient.getApi(requireContext());
        Call<PageResponse<ProdutoResponse>> chamada =
                api.listarProdutos(termo, categoriaId, null, precoMax, promocao, 0, 50);
        chamadaAtual = chamada;
        chamada.enqueue(new Callback<PageResponse<ProdutoResponse>>() {
                    @Override
                    public void onResponse(Call<PageResponse<ProdutoResponse>> call,
                                           Response<PageResponse<ProdutoResponse>> response) {
                        if (call != chamadaAtual) return;
                        chamadaAtual = null;
                        LoadingView.esconder(progressBar, rvResultados);
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null
                                && response.body().content != null) {

                            List<br.com.arthurbaby.models.Produto> filtrados =
                                    Conversor.paraProdutos(response.body().content);

                            adapter = new ProdutoAdapter(filtrados, produto -> {
                                // Salva no histórico quando o usuário clica em um produto
                                if (termoAtual != null && !termoAtual.trim().isEmpty()) {
                                    BuscaStorage.adicionar(requireContext(), termoAtual);
                                }

                                DetalheProdutoFragment frag =
                                        DetalheProdutoFragment.newInstance(produto);
                                requireActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.frameContainer, frag)
                                        .addToBackStack(null)
                                        .commit();
                            });
                            rvResultados.setAdapter(adapter);

                            if (filtrados.isEmpty()) {
                                tvInfo.setText("Nenhum resultado encontrado");
                                rvResultados.setVisibility(View.GONE);
                                mostrarEstadoVazio("Nenhum produto encontrado",
                                        "Tente outra palavra ou remova o filtro. Temos muitas novidades esperando por você 💕");
                            }
                            else if (filtrados.size() == 1) tvInfo.setText("1 resultado");
                            else tvInfo.setText(filtrados.size() + " resultados");
                        } else {
                            Toast.makeText(requireContext(),
                                    "Erro ao buscar produtos", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PageResponse<ProdutoResponse>> call, Throwable t) {
                        if (call.isCanceled() || call != chamadaAtual) return;
                        chamadaAtual = null;
                        LoadingView.esconder(progressBar, rvResultados);
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(),
                                ErrorUtils.mensagemFalha(t), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacks(buscaAgendada);
        cancelarChamada();
        super.onDestroyView();
    }

    /** Filtro ativo em verde ArthurBaby; inativos em rosa suave. */
    private void pintarChip(TextView chip, boolean ativo) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(20));
        if (ativo) {
            bg.setColor(ContextCompat.getColor(requireContext(), R.color.verde_arthur));
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.branco));
            chip.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            bg.setColor(ContextCompat.getColor(requireContext(), R.color.rosa_suave));
            bg.setStroke(dp(1), ContextCompat.getColor(requireContext(), R.color.rosa_claro));
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.rosa_escuro));
            chip.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
        chip.setBackground(bg);
    }

    private void pintarChipLimpar() {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(20));
        bg.setColor(android.graphics.Color.TRANSPARENT);
        bg.setStroke(dp(1), ContextCompat.getColor(requireContext(), R.color.cinza_borda));
        chipLimpar.setBackground(bg);
    }

    private void mostrarEstadoInicial() {
        mostrarEstadoVazio("O que o seu bebê precisa hoje?",
                "Busque roupinhas, enxoval, acessórios e muito mais — feito com carinho para você.");
    }

    private void mostrarEstadoVazio(String titulo, String texto) {
        if (estadoVazio == null) return;
        tvVazioTitulo.setText(titulo);
        tvVazioTexto.setText(texto);
        estadoVazio.setVisibility(View.VISIBLE);
    }

    private int dp(int valor) {
        float densidade = getResources().getDisplayMetrics().density;
        return (int) (valor * densidade + 0.5f);
    }
}