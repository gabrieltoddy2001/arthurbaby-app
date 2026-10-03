package br.com.arthurbaby.fragments;

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
    private TextView btnLimparHistorico;

    private String filtroAtual = "TODAS";
    private String termoAtual = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_pesquisa, container, false);

        etBusca = v.findViewById(R.id.etBusca);
        tvInfo = v.findViewById(R.id.tvResultadoInfo);
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
                requireActivity().getSupportFragmentManager().popBackStack());

        rvResultados.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Busca via Enter/actionSearch
        etBusca.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String termo = etBusca.getText().toString().trim();
                if (!termo.isEmpty()) {
                    BuscaStorage.adicionar(requireContext(), termo);
                    mostrarHistorico(false);
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
                    mostrarHistorico(true);
                    rvResultados.setVisibility(View.GONE);
                    tvInfo.setText("Digite para buscar");
                } else {
                    mostrarHistorico(false);
                    aplicarFiltro();
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        chipTodas.setOnClickListener(x -> { filtroAtual = "TODAS"; aplicarFiltro(); });
        chipPromocao.setOnClickListener(x -> { filtroAtual = "PROMOCAO"; aplicarFiltro(); });
        chipBaratos.setOnClickListener(x -> { filtroAtual = "BARATOS"; aplicarFiltro(); });
        chipLimpar.setOnClickListener(x -> {
            filtroAtual = "TODAS";
            etBusca.setText("");
            termoAtual = "";
            mostrarHistorico(true);
            rvResultados.setVisibility(View.GONE);
            tvInfo.setText("Digite para buscar");
        });

        // Estado inicial
        mostrarHistorico(true);
        rvResultados.setVisibility(View.GONE);
        tvInfo.setText("Digite para buscar");

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
        bg.setColor(ContextCompat.getColor(requireContext(), R.color.chip_fundo));
        chip.setBackground(bg);
        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.chip_texto));

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
        pintarChip(chipTodas, "TODAS".equals(filtroAtual));
        pintarChip(chipPromocao, "PROMOCAO".equals(filtroAtual));
        pintarChip(chipBaratos, "BARATOS".equals(filtroAtual));
        buscarProdutos(termoAtual, null);
    }

    private void buscarProdutos(String busca, Long categoriaId) {
        LoadingView.mostrar(progressBar, rvResultados);

        ApiService api = RetrofitClient.getApi(requireContext());
        api.listarProdutos(busca, categoriaId, null, 0, 50)
                .enqueue(new Callback<PageResponse<ProdutoResponse>>() {
                    @Override
                    public void onResponse(Call<PageResponse<ProdutoResponse>> call,
                                           Response<PageResponse<ProdutoResponse>> response) {
                        LoadingView.esconder(progressBar, rvResultados);

                        if (response.isSuccessful() && response.body() != null
                                && response.body().content != null) {

                            List<br.com.arthurbaby.models.Produto> produtos =
                                    Conversor.paraProdutos(response.body().content);

                            List<br.com.arthurbaby.models.Produto> filtrados =
                                    new java.util.ArrayList<>();
                            for (br.com.arthurbaby.models.Produto p : produtos) {
                                double preco = p.getPreco().doubleValue();
                                if ("PROMOCAO".equals(filtroAtual) && preco >= 50) continue;
                                if ("BARATOS".equals(filtroAtual) && preco > 50) continue;
                                filtrados.add(p);
                            }

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

                            if (filtrados.isEmpty()) tvInfo.setText("Nenhum resultado encontrado");
                            else if (filtrados.size() == 1) tvInfo.setText("1 resultado");
                            else tvInfo.setText(filtrados.size() + " resultados");
                        } else {
                            Toast.makeText(requireContext(),
                                    "Erro ao buscar produtos", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PageResponse<ProdutoResponse>> call, Throwable t) {
                        LoadingView.esconder(progressBar, rvResultados);
                        Toast.makeText(requireContext(),
                                "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pintarChip(TextView chip, boolean ativo) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(40f);
        if (ativo) {
            bg.setColor(ContextCompat.getColor(requireContext(), R.color.chip_selecionado));
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.chip_selecionado_texto));
        } else {
            bg.setColor(ContextCompat.getColor(requireContext(), R.color.chip_fundo));
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.chip_texto));
        }
        chip.setBackground(bg);
    }

    private int dp(int valor) {
        float densidade = getResources().getDisplayMetrics().density;
        return (int) (valor * densidade + 0.5f);
    }
}