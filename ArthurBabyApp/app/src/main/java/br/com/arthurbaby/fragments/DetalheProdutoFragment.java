package br.com.arthurbaby.fragments;

import br.com.arthurbaby.utils.ErrorUtils;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

import br.com.arthurbaby.R;
import br.com.arthurbaby.adapters.ImagemPagerAdapter;
import br.com.arthurbaby.models.Produto;
import br.com.arthurbaby.models.Variacao;
import br.com.arthurbaby.network.ApiService;
import br.com.arthurbaby.network.Conversor;
import br.com.arthurbaby.network.RetrofitClient;
import br.com.arthurbaby.network.dto.ProdutoResponse;
import br.com.arthurbaby.repositories.CarrinhoRepository;
import br.com.arthurbaby.repositories.FavoritoRepository;
import br.com.arthurbaby.utils.AuthGuard;
import br.com.arthurbaby.utils.AvaliacaoUtils;
import br.com.arthurbaby.utils.MoedaUtils;
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

    private androidx.viewpager2.widget.ViewPager2 viewPagerImagens;
    private LinearLayout indicadoresImagens;

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
        TextView tvDescricao = v.findViewById(R.id.tvDescricao);
        TextView tvQtd = v.findViewById(R.id.tvQtd);
        ImageView btnFav = v.findViewById(R.id.btnFavoritoDetalhe);
        ImageView btnCompartilhar = v.findViewById(R.id.btnCompartilhar);

        viewPagerImagens = v.findViewById(R.id.viewPagerImagens);
        indicadoresImagens = v.findViewById(R.id.indicadoresImagens);

        if (produto != null) {
            tvNome.setText(produto.getNome());
            tvMarca.setText("Marca: " + produto.getMarca());
            tvDescricao.setText(produto.getDescricao());
            tvPreco.setText(MoedaUtils.formatar(produto.getPreco()));
            mostrarAvaliacao(v);
            mostrarPrecoAntigo(v);

            // ✅ TODAS as imagens
            configurarImagens(produto.getImagens());

            boolean logado = AuthGuard.estaLogado(requireContext());
            btnFav.setVisibility(logado ? View.VISIBLE : View.GONE);

            if (logado) {
                boolean fav = FavoritoRepository.getInstance().isFavorito(produto.getId());
                atualizarCoracao(btnFav, fav);
                btnFav.setOnClickListener(x -> {
                    boolean agora = FavoritoRepository.getInstance()
                            .toggle(requireContext(), produto.getId(), () -> {});
                    atualizarCoracao(btnFav, agora);
                });
            } else {
                btnFav.setOnClickListener(x -> AuthGuard
                        .mostrarDialogLogin(requireActivity(),
                                "Entre para salvar seus favoritos."));
            }

            if (btnCompartilhar != null) {
                btnCompartilhar.setOnClickListener(x -> compartilharProduto());
            }

            com.google.android.material.button.MaterialButton btnAvaliar =
                    v.findViewById(R.id.btnAvaliar);
            if (btnAvaliar != null && AuthGuard.estaLogado(requireContext())) {
                btnAvaliar.setOnClickListener(x -> abrirDialogAvaliacao());
            } else if (btnAvaliar != null) {
                btnAvaliar.setOnClickListener(x -> AuthGuard.mostrarDialogLogin(
                        requireActivity(), "Entre para avaliar o produto."));
            }

            carregarProdutoBackend(v);
        }

        v.findViewById(R.id.btnVoltar).setOnClickListener(x ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

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

        v.findViewById(R.id.btnAddCarrinho).setOnClickListener(x -> adicionarAoCarrinho());

        return v;
    }

    // ============================================================
    // NOVO: carrossel
    // ============================================================
    private void configurarImagens(List<String> urlsRecebidas) {
        if (viewPagerImagens == null || indicadoresImagens == null) return;

        List<String> urls = new ArrayList<>();
        if (urlsRecebidas != null) {
            for (String u : urlsRecebidas) {
                if (u != null && !u.isEmpty()) urls.add(u);
            }
        }

        indicadoresImagens.removeAllViews();
        viewPagerImagens.setAdapter(null);

        if (urls.isEmpty()) {
            viewPagerImagens.setVisibility(View.GONE);
            return;
        }
        viewPagerImagens.setVisibility(View.VISIBLE);
        viewPagerImagens.setAdapter(new ImagemPagerAdapter(urls));

        if (urls.size() <= 1) return;

        for (int i = 0; i < urls.size(); i++) {
            View dot = new View(requireContext());
            int s = dp(6);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(s, s);
            lp.setMargins(dp(3), 0, dp(3), 0);
            dot.setLayoutParams(lp);
            dot.setBackgroundResource(i == 0
                    ? R.drawable.bg_dot_ativo
                    : R.drawable.bg_dot_inativo);
            indicadoresImagens.addView(dot);
        }

        viewPagerImagens.registerOnPageChangeCallback(
                new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        for (int i = 0; i < indicadoresImagens.getChildCount(); i++) {
                            indicadoresImagens.getChildAt(i).setBackgroundResource(
                                    i == position
                                            ? R.drawable.bg_dot_ativo
                                            : R.drawable.bg_dot_inativo);
                        }
                    }
                });
    }

    private void carregarProdutoBackend(View v) {
        ApiService api = RetrofitClient.getApi(requireContext());
        api.buscarProduto(produto.getId()).enqueue(new Callback<ProdutoResponse>() {
            @Override
            public void onResponse(Call<ProdutoResponse> call, Response<ProdutoResponse> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    Produto atualizado = Conversor.paraProduto(response.body());
                    if (atualizado == null) return;

                    produto = atualizado;

                    TextView tvNome = v.findViewById(R.id.tvNome);
                    TextView tvPreco = v.findViewById(R.id.tvPreco);
                    TextView tvDescricao = v.findViewById(R.id.tvDescricao);

                    tvNome.setText(atualizado.getNome());
                    tvDescricao.setText(atualizado.getDescricao());
                    tvPreco.setText(MoedaUtils.formatar(atualizado.getPreco()));
                    mostrarAvaliacao(v);
                    mostrarPrecoAntigo(v);

                    // ✅ TODAS as imagens novamente
                    configurarImagens(atualizado.getImagens());

                    desenharVariacoes(v);
                    atualizarPrecoVariacao();
                }
            }

            @Override
            public void onFailure(Call<ProdutoResponse> call, Throwable t) {
                if (!isAdded()) return;
            }
        });
    }

    private void compartilharProduto() {
        if (produto == null) return;

        String texto = "Olha esse produto na ArthurBaby 💕\n\n"
                + produto.getNome() + "\n"
                + "Por apenas " + MoedaUtils.formatar(produto.getPreco()) + "!\n\n"
                + "Baixe o app: https://play.google.com/store/apps/details?id=br.com.arthurbaby";

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, produto.getNome() + " - ArthurBaby");
        share.putExtra(Intent.EXTRA_TEXT, texto);

        startActivity(Intent.createChooser(share, "Compartilhar produto"));
    }

    private void abrirDialogAvaliacao() {
        android.widget.LinearLayout container = new android.widget.LinearLayout(getContext());
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setPadding(50, 30, 50, 10);

        android.widget.RatingBar ratingBar = new android.widget.RatingBar(getContext());
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1.0f);
        ratingBar.setRating(5);
        container.addView(ratingBar);

        android.widget.EditText etComentario = new android.widget.EditText(getContext());
        etComentario.setHint("Escreva seu comentário (opcional)");
        etComentario.setMinLines(2);
        etComentario.setPadding(0, 20, 0, 0);
        container.addView(etComentario);

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Avaliar Produto")
                .setView(container)
                .setPositiveButton("Enviar", (d, w) -> {
                    int nota = (int) ratingBar.getRating();
                    if (nota < 1) {
                        Toast.makeText(requireContext(), "Escolha de 1 a 5 estrelas.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String comentario = etComentario.getText().toString().trim();
                    enviarAvaliacao(nota, comentario);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void enviarAvaliacao(int nota, String comentario) {
        Long clienteId = br.com.arthurbaby.network.TokenStorage.getUsuarioId(requireContext());
        if (clienteId == null || produto == null) return;

        br.com.arthurbaby.utils.LoadingUtils.mostrar(requireContext());

        ApiService api = RetrofitClient.getApi(requireContext());
        api.avaliarProduto(produto.getId(),
                        new br.com.arthurbaby.network.dto.AvaliacaoRequest(clienteId, nota, comentario))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        br.com.arthurbaby.utils.LoadingUtils.esconder();
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Avaliação enviada! Obrigado.", Toast.LENGTH_LONG).show();
                            if (getView() != null) carregarProdutoBackend(getView());
                        } else {
                            Toast.makeText(requireContext(),
                                    br.com.arthurbaby.utils.ErrorUtils.extrairMensagem(response),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        br.com.arthurbaby.utils.LoadingUtils.esconder();
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(),
                                ErrorUtils.mensagemFalha(t), Toast.LENGTH_SHORT).show();
                    }
                });
    }

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

        LinearLayout cTam = v.findViewById(R.id.containerTamanhos);
        if (!tamanhos.isEmpty()) {
            cTam.removeAllViews();
            for (String t : tamanhos) {
                cTam.addView(criarChip(t, t.equals(tamSel), view -> {
                    tamSel = t;
                    atualizarChips(cTam, t);
                    atualizarVariacaoIdSel();
                    atualizarPrecoVariacao();
                }));
            }
            if (tamSel == null) {
                tamSel = tamanhos.get(0);
                atualizarChips(cTam, tamSel);
            }
        } else {
            esconderSecao(v, cTam);
        }

        LinearLayout cCor = v.findViewById(R.id.containerCores);
        if (!cores.isEmpty()) {
            cCor.removeAllViews();
            for (String c : cores) {
                cCor.addView(criarChip(c, c.equals(corSel), view -> {
                    corSel = c;
                    atualizarChips(cCor, c);
                    atualizarVariacaoIdSel();
                    atualizarPrecoVariacao();
                }));
            }
            if (corSel == null) {
                corSel = cores.get(0);
                atualizarChips(cCor, corSel);
            }
        } else {
            esconderSecao(v, cCor);
        }

        LinearLayout cMod = v.findViewById(R.id.containerModelos);
        if (!modelos.isEmpty()) {
            cMod.removeAllViews();
            for (String m : modelos) {
                cMod.addView(criarChip(m, m.equals(modSel), view -> {
                    modSel = m;
                    atualizarChips(cMod, m);
                    atualizarVariacaoIdSel();
                    atualizarPrecoVariacao();
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

    private void atualizarVariacaoIdSel() {
        variacaoIdSel = null;
        Produto.VariacaoReal var = variacaoSelecionada();
        if (var != null) variacaoIdSel = var.id;
    }

    private Produto.VariacaoReal variacaoSelecionada() {
        if (produto == null) return null;
        for (Produto.VariacaoReal var : produto.getVariacoes()) {
            boolean tamOk = (tamSel == null) || tamSel.equals(var.tamanho);
            boolean corOk = (corSel == null) || corSel.equals(var.cor);
            boolean modOk = (modSel == null) || modSel.equals(var.modelo);
            if (tamOk && corOk && modOk) return var;
        }
        return null;
    }

    private void atualizarPrecoVariacao() {
        View v = getView();
        if (v == null || produto == null) return;
        Produto.VariacaoReal var = variacaoSelecionada();
        java.math.BigDecimal preco = var != null && var.preco != null ? var.preco : produto.getPreco();
        ((TextView) v.findViewById(R.id.tvPreco)).setText(MoedaUtils.formatar(preco));
    }

    private void mostrarAvaliacao(View v) {
        TextView tvEstrelas = v.findViewById(R.id.tvEstrelas);
        TextView tvAvaliacao = v.findViewById(R.id.tvAvaliacao);
        tvEstrelas.setText(AvaliacaoUtils.estrelas(produto.getAvaliacao()));
        tvAvaliacao.setText(AvaliacaoUtils.nota(produto.getAvaliacao(), "(sem avaliações)"));
    }

    private void mostrarPrecoAntigo(View v) {
        TextView tvPrecoAntigo = v.findViewById(R.id.tvPrecoAntigo);
        if (produto.temDesconto()) {
            tvPrecoAntigo.setVisibility(View.VISIBLE);
            tvPrecoAntigo.setText(MoedaUtils.formatar(produto.getPrecoOriginal()));
            tvPrecoAntigo.setPaintFlags(tvPrecoAntigo.getPaintFlags()
                    | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tvPrecoAntigo.setVisibility(View.GONE);
        }
    }

    private void adicionarAoCarrinho() {
        if (produto == null) return;
        if (produto.isEsgotado()) {
            Toast.makeText(requireContext(), "Produto esgotado", Toast.LENGTH_SHORT).show();
            return;
        }

        Produto.VariacaoReal var = null;
        if (!produto.getVariacoes().isEmpty()) {
            var = variacaoSelecionada();
            if (var == null) {
                Toast.makeText(requireContext(),
                        "Essa combinação de tamanho/cor/modelo não está disponível.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            int jaNoCarrinho = CarrinhoRepository.getInstance()
                    .quantidadeNoCarrinho(produto.getId(), var.id);
            int disponivel = var.estoqueAtual - jaNoCarrinho;
            if (disponivel <= 0) {
                Toast.makeText(requireContext(),
                        jaNoCarrinho > 0 ? "Todo o estoque dessa opção já está no seu carrinho."
                                : "Sem estoque para essa opção.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (qtd > disponivel) {
                Toast.makeText(requireContext(),
                        "Só há " + disponivel + " unidade(s) disponível(is) dessa opção.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Variacao variacao = null;
        if (tamSel != null || corSel != null || modSel != null) {
            variacao = new Variacao(
                    tamSel != null ? tamSel : "-",
                    corSel != null ? corSel : "-",
                    modSel != null ? modSel : "-");
        }
        CarrinhoRepository.getInstance().adicionar(produto, qtd, variacao,
                var != null ? var.id : null, var != null ? var.preco : null);
        Toast.makeText(requireContext(),
                qtd + " item(ns) adicionado(s)", Toast.LENGTH_SHORT).show();
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void esconderSecao(View root, View container) {
        if (container != null) container.setVisibility(View.GONE);
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
            bg.setColor(ContextCompat.getColor(requireContext(), R.color.chip_selecionado));
            bg.setStroke(dp(2), ContextCompat.getColor(requireContext(), R.color.chip_selecionado));
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.chip_selecionado_texto));
        } else {
            bg.setColor(ContextCompat.getColor(requireContext(), R.color.chip_fundo));
            bg.setStroke(dp(1), ContextCompat.getColor(requireContext(), R.color.chip_borda));
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.chip_texto));
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
        String dica = fav ? "Remover dos favoritos" : "Adicionar aos favoritos";
        btn.setContentDescription(dica);
        androidx.appcompat.widget.TooltipCompat.setTooltipText(btn, dica);
    }

    private int dp(int valor) {
        float densidade = getResources().getDisplayMetrics().density;
        return (int) (valor * densidade + 0.5f);
    }
}