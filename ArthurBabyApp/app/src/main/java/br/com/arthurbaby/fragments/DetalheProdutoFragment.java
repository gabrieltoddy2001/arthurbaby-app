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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import br.com.arthurbaby.R;
import br.com.arthurbaby.models.Produto;
import br.com.arthurbaby.models.Variacao;
import br.com.arthurbaby.repositories.CarrinhoRepository;
import br.com.arthurbaby.repositories.FavoritoRepository;

public class DetalheProdutoFragment extends Fragment {

    private static final String ARG_PRODUTO = "produto";
    private Produto produto;
    private int qtd = 1;

    private String tamSel = "M";
    private String corSel = "Rosa";
    private String modSel = "Padrão";

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

        if (produto != null) {
            tvNome.setText(produto.getNome());
            tvMarca.setText("Marca: " + produto.getMarca());
            tvDescricao.setText(produto.getDescricao());
            tvPreco.setText(nf.format(produto.getPreco()));
            tvEstrelas.setText("★★★★★");
            tvAvaliacao.setText("(5.0)");

            // Preço antigo riscado se em promoção
            if (produto.getPreco().doubleValue() < 50 && produto.temEstoque()) {
                tvPrecoAntigo.setVisibility(View.VISIBLE);
                tvPrecoAntigo.setText("R$ " + String.format("%.2f",
                        produto.getPreco().doubleValue() * 1.3));
                tvPrecoAntigo.setPaintFlags(tvPrecoAntigo.getPaintFlags()
                        | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            // Coração
            boolean fav = FavoritoRepository.getInstance().isFavorito(produto);
            atualizarCoracao(btnFav, fav);
            btnFav.setOnClickListener(x -> {
                FavoritoRepository.getInstance().toggle(produto);
                boolean agora = FavoritoRepository.getInstance().isFavorito(produto);
                atualizarCoracao(btnFav, agora);
            });

            // Esgotado
            if (produto.isEsgotado()) {
                com.google.android.material.button.MaterialButton btn =
                        v.findViewById(R.id.btnAddCarrinho);
                btn.setEnabled(false);
                btn.setBackgroundColor(0xFF94A3B8);
                btn.setText("PRODUTO ESGOTADO");
            }
        }

        // Botão voltar
        v.findViewById(R.id.btnVoltar).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        // CORES — círculos
        LinearLayout cCor = v.findViewById(R.id.containerCores);
        List<String> cores = Arrays.asList("Branco", "Azul", "Rosa", "Amarelo", "Verde");
        int[] hexCores = {0xFFFFFFFF, 0xFF3B82F6, 0xFFED83A4, 0xFFFFC107, 0xFF10B981};
        for (int i = 0; i < cores.size(); i++) {
            final int idx = i;
            View circulo = criarCirculoCor(hexCores[i], cores.get(i).equals(corSel));
            circulo.setOnClickListener(x -> {
                corSel = cores.get(idx);
                atualizarCirculos(cCor, cores.indexOf(corSel), hexCores);
            });
            cCor.addView(circulo);
        }

        // TAMANHOS — botões redondos
        LinearLayout cTam = v.findViewById(R.id.containerTamanhos);
        List<String> tamanhos = Arrays.asList("RN", "P", "M", "G", "GG");
        for (String t : tamanhos) {
            cTam.addView(criarChipTamanho(t, t.equals(tamSel), view -> {
                tamSel = t;
                atualizarChips(cTam, t);
            }));
        }

        // MODELOS — chips
        LinearLayout cMod = v.findViewById(R.id.containerModelos);
        List<String> modelos = Arrays.asList("Padrão", "Premium", "Deluxe");
        for (String m : modelos) {
            cMod.addView(criarChipTamanho(m, m.equals(modSel), view -> {
                modSel = m;
                atualizarChips(cMod, m);
            }));
        }

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
                Variacao variacao = new Variacao(tamSel, corSel, modSel);
                CarrinhoRepository.getInstance().adicionar(produto, qtd, variacao);
                Toast.makeText(requireContext(),
                        qtd + " item(ns) adicionado(s)",
                        Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return v;
    }

    private void atualizarCoracao(ImageView btn, boolean fav) {
        btn.setImageResource(fav ? R.drawable.ic_favorito_preenchido : R.drawable.ic_favorito);
        btn.setColorFilter(ContextCompat.getColor(requireContext(),
                fav ? R.color.coracao_ativo : R.color.coracao_inativo));
    }

    /**
     * Cria um círculo de cor clicável (com borda se selecionado).
     */
    private View criarCirculoCor(int cor, boolean selecionado) {
        View container = new View(getContext());

        int tamanho = 44;
        int padding = 4;

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                dp(tamanho), dp(tamanho));
        lp.setMargins(0, 0, 12, 0);
        container.setLayoutParams(lp);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(cor);
        if (selecionado) {
            bg.setStroke(dp(3), 0xFF37B6B0);
        } else {
            bg.setStroke(dp(1), 0xFFE2E8F0);
        }
        container.setBackground(bg);

        return container;
    }

    private void atualizarCirculos(LinearLayout container, int idxSelecionado, int[] hexCores) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(hexCores[i]);
            if (i == idxSelecionado) {
                bg.setStroke(dp(3), 0xFF37B6B0);
            } else {
                bg.setStroke(dp(1), 0xFFE2E8F0);
            }
            child.setBackground(bg);
        }
    }

    private TextView criarChipTamanho(String texto, boolean selecionado, View.OnClickListener onClick) {
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

    private int dp(int valor) {
        float densidade = getResources().getDisplayMetrics().density;
        return (int) (valor * densidade + 0.5f);
    }
}