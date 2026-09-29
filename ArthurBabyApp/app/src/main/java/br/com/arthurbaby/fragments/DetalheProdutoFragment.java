package br.com.arthurbaby.fragments;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.com.arthurbaby.R;
import br.com.arthurbaby.models.Produto;
import br.com.arthurbaby.models.Variacao;
import br.com.arthurbaby.network.ApiService;
import br.com.arthurbaby.network.Conversor;
import br.com.arthurbaby.network.RetrofitClient;
import br.com.arthurbaby.network.dto.ProdutoResponse;
import br.com.arthurbaby.repositories.CarrinhoRepository;
import br.com.arthurbaby.repositories.FavoritoRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalheProdutoFragment extends Fragment {

    private static final String ARG_PRODUTO = "produto";
    private Produto produto;
    private int qtd = 1;

    private String tamSel = null;
    private String corSel = null;
    private String modSel = null;
    private Long variacaoIdSel = null;

    public static DetalheProdutoFragment newInstance(Produto p) {
        DetalheProdutoFragment f = new DetalheProdutoFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_PRODUTO, p);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_detalhe_produto, container, false);

        if (getArguments() != null) {
            produto = (Produto) getArguments().getSerializable(ARG_PRODUTO);
        }

        TextView tvNome = v.findViewById(R.id.tvNome);
        TextView tvMarca = v.findViewById(R.id.tvMarca);
        TextView tvPreco = v.findViewById(R.id.tvPreco);
        TextView tvPrecoAntigo = v.findViewById(R.id.tvPrecoAntigo);
        TextView tvDescricao = v.findViewById(R.id.tvDescricao);
        TextView tvQtd = v.findViewById(R.id.tvQtd);
        TextView tvEstrelas = v.findViewById(R.id.tvEstrelas);
        TextView tvAvaliacao = v.findViewById(R.id.tvAvaliacao);
        ImageView img = v.findViewById(R.id.imgProduto);
        ImageView btnFav = v.findViewById(R.id.btnFavoritoDetalhe);

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        // Preenche com dados do adapter (imediatamente)
        if (produto != null) {
            tvNome.setText(produto.getNome());
            tvMarca.setText("Marca: " + produto.getMarca());
            tvDescricao.setText(produto.getDescricao());
            tvPreco.setText(nf.format(produto.getPreco()));
            tvEstrelas.setText("★★★★★");
            tvAvaliacao.setText("(5.0)");
            img.setBackgroundColor(0xFFFAD1DE);

            if (produto.getPreco().doubleValue() < 50 && produto.temEstoque()) {
                tvPrecoAntigo.setVisibility(View.VISIBLE);
                tvPrecoAntigo.setText("R$ " + String.format("%.2f",
                        produto.getPreco().doubleValue() * 1.3));
                tvPrecoAntigo.setPaintFlags(tvPrecoAntigo.getPaintFlags()
                        | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            boolean fav = FavoritoRepository.getInstance().isFavorito(produto.getId());
            atualizarCoracao(btnFav, fav);
            btnFav.setOnClickListener(x -> {
                boolean agora = FavoritoRepository.getInstance()
                        .toggle(requireContext(), produto.getId(), () -> {});
                atualizarCoracao(btnFav, agora);
            });

            // Busca detalhes atualizados no backend
            carregarProdutoBackend(v);
        }

        v.findViewById(R.id.btnVoltar).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack());

        // Quantidade
        v.findViewById(R.id.btnMais).setOnClickListener(x -> {
            qtd++;
            tvQtd.setText(String.valueOf(qtd));
        });
        v.findViewById(R.id.btnMenos).setOnClickListener(x -> {
            if (qtd > 1) {
                qtd--;
                tvQtd.setText(String.valueOf(qtd));
            }
        });

        // Adicionar ao carrinho
        v.findViewById(R.id.btnAddCarrinho).setOnClickListener(x -> {
            if (produto != null && produto.temEstoque()) {
                Variacao variacao = null;
                if (tamSel != null || corSel != null || modSel != null) {
                    variacao = new Variacao(
                            tamSel != null ? tamSel : "-",
                            corSel != null ? corSel : "-",
                            modSel != null ? modSel : "-");
                }
                CarrinhoRepository.getInstance().adicionar(produto, qtd, variacao, variacaoIdSel);
                Toast.makeText(requireContext(),
                        qtd + " item(ns) adicionado(s)", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return v;
    }

    private void carregarProdutoBackend(View v) {
        ApiService api = RetrofitClient.getApi(requireContext());
        api.buscarProduto(produto.getId()).enqueue(new Callback<ProdutoResponse>() {
            @Override
            public void onResponse(Call<ProdutoResponse> call, Response<ProdutoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Produto atualizado = Conversor.paraProduto(response.body());
                    if (atualizado == null) return;

                    produto = atualizado;

                    TextView tvNome = v.findViewById(R.id.tvNome);
                    TextView tvPreco = v.findViewById(R.id.tvPreco);
                    TextView tvDescricao = v.findViewById(R.id.tvDescricao);
                    TextView tvAvaliacao = v.findViewById(R.id.tvAvaliacao);

                    tvNome.setText(atualizado.getNome());
                    tvDescricao.setText(atualizado.getDescricao());

                    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
                    tvPreco.setText(nf.format(atualizado.getPreco()));

                    if (atualizado.getAvaliacao() > 0) {
                        tvAvaliacao.setText("(" + atualizado.getAvaliacao() + ".0)");
                    }

                    // Desenha as variações
                    desenharVariacoes(v);
                }
            }

            @Override
            public void onFailure(Call<ProdutoResponse> call, Throwable t) {
                // Silencioso
            }
        });
    }

    /**
     * Desenha as seções de Tamanho, Cor e Modelo com base nas variações reais.
     */
    private void desenharVariacoes(View v) {
        if (produto == null || produto.getVariacoes().isEmpty()) return;

        List<String> tamanhos = new ArrayList<>();
        List<String> cores = new ArrayList<>();
        List<String> modelos = new ArrayList<>();

        for (Produto.VariacaoReal var : produto.getVariacoes()) {
            if (var.tamanho != null && !tamanhos.contains(var.tamanho)) tamanhos.add(var.tamanho);
            if (var.cor != null && !cores.contains(var.cor)) cores.add(var.cor);
            if (var.modelo != null && !modelos.contains(var.modelo)) modelos.add(var.modelo);
        }

        // TAMANHO
        LinearLayout cTam = v.findViewById(R.id.containerTamanhos);
        if (!tamanhos.isEmpty()) {
            cTam.removeAllViews();
            for (String t : tamanhos) {
                cTam.addView(criarChip(t, t.equals(tamSel), view -> {
                    tamSel = t;
                    atualizarChips(cTam, t);
                    atualizarVariacaoIdSel();
                }));
            }
            // seleciona o primeiro
            if (tamSel == null) {
                tamSel = tamanhos.get(0);
                atualizarChips(cTam, tamSel);
            }
        } else {
            esconderSecao(v, cTam);
        }

        // COR
        LinearLayout cCor = v.findViewById(R.id.containerCores);
        if (!cores.isEmpty()) {
            cCor.removeAllViews();
            for (String c : cores) {
                cCor.addView(criarChip(c, c.equals(corSel), view -> {
                    corSel = c;
                    atualizarChips(cCor, c);
                    atualizarVariacaoIdSel();
                }));
            }
            if (corSel == null) {
                corSel = cores.get(0);
                atualizarChips(cCor, corSel);
            }
        } else {
            esconderSecao(v, cCor);
        }

        // MODELO
        LinearLayout cMod = v.findViewById(R.id.containerModelos);
        if (!modelos.isEmpty()) {
            cMod.removeAllViews();
            for (String m : modelos) {
                cMod.addView(criarChip(m, m.equals(modSel), view -> {
                    modSel = m;
                    atualizarChips(cMod, m);
                    atualizarVariacaoIdSel();
                }));
            }
            if (modSel == null) {
                modSel = modelos.get(0);
                atualizarChips(cMod, modSel);
            }
        } else {
            esconderSecao(v, cMod);
        }

        atualizarVariacaoIdSel();
    }

    /**
     * Descobre qual variação corresponde à seleção atual.
     */
    private void atualizarVariacaoIdSel() {
        variacaoIdSel = null;
        if (produto == null) return;
        for (Produto.VariacaoReal var : produto.getVariacoes()) {
            boolean tamOk = (tamSel == null) || tamSel.equals(var.tamanho);
            boolean corOk = (corSel == null) || corSel.equals(var.cor);
            boolean modOk = (modSel == null) || modSel.equals(var.modelo);
            if (tamOk && corOk && modOk) {
                variacaoIdSel = var.id;
                return;
            }
        }
    }

    private void esconderSecao(View root, View container) {
        if (container != null) container.setVisibility(View.GONE);
        // Esconde o título acima
        if (root instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                if (group.getChildAt(i) == container && i > 0) {
                    View ant = group.getChildAt(i - 1);
                    if (ant instanceof TextView) ant.setVisibility(View.GONE);
                    return;
                }
            }
        }
    }

    private TextView criarChip(String texto, boolean selecionado, View.OnClickListener onClick) {
        TextView chip = new TextView(getContext());
        chip.setText(texto);
        chip.setPadding(dp(18), dp(10), dp(18), dp(10));
        chip.setTextSize(13f);
        chip.setGravity(Gravity.CENTER);
        aplicarEstiloChip(chip, selecionado);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 10, 0);
        chip.setLayoutParams(lp);
        chip.setOnClickListener(onClick);
        return chip;
    }

    private void aplicarEstiloChip(TextView chip, boolean selecionado) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(20));
        if (selecionado) {
            bg.setColor(0xFF37B6B0);
            bg.setStroke(dp(2), 0xFF37B6B0);
            chip.setTextColor(Color.WHITE);
        } else {
            bg.setColor(0xFFF0F2F5);
            bg.setStroke(dp(1), 0xFFE2E8F0);
            chip.setTextColor(0xFF1E293B);
        }
        chip.setBackground(bg);
    }

    private void atualizarChips(LinearLayout container, String selecionado) {
        for (int i = 0; i < container.getChildCount(); i++) {
            TextView chip = (TextView) container.getChildAt(i);
            aplicarEstiloChip(chip, chip.getText().toString().equals(selecionado));
        }
    }

    private void atualizarCoracao(ImageView btn, boolean fav) {
        btn.setImageResource(fav ? R.drawable.ic_favorito_preenchido : R.drawable.ic_favorito);
        btn.setColorFilter(ContextCompat.getColor(requireContext(),
                fav ? R.color.coracao_ativo : R.color.coracao_inativo));
    }

    private int dp(int valor) {
        float densidade = getResources().getDisplayMetrics().density;
        return (int) (valor * densidade + 0.5f);
    }
}