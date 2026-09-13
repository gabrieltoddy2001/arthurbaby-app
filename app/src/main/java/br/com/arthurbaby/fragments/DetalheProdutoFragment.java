package br.com.arthurbaby.fragments;

import android.graphics.Color;
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
import androidx.fragment.app.Fragment;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import br.com.arthurbaby.R;
import br.com.arthurbaby.models.Produto;
import br.com.arthurbaby.models.Variacao;
import br.com.arthurbaby.repositories.CarrinhoRepository;

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

        v.findViewById(R.id.btnVoltar).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        if (getArguments() != null) {
            produto = (Produto) getArguments().getSerializable(ARG_PRODUTO);
        }

        TextView tvNome = v.findViewById(R.id.tvNome);
        TextView tvPreco = v.findViewById(R.id.tvPreco);
        TextView tvDescricao = v.findViewById(R.id.tvDescricao);
        TextView tvMarca = v.findViewById(R.id.tvMarca);
        TextView tvQtd = v.findViewById(R.id.tvQtd);
        ImageView img = v.findViewById(R.id.imgProduto);

        if (produto != null) {
            tvNome.setText(produto.getNome());
            tvDescricao.setText(produto.getDescricao());
            tvMarca.setText("Marca: " + produto.getMarca());

            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            tvPreco.setText(nf.format(produto.getPreco()));
            img.setBackgroundColor(0xFF37B6B0);

            // Bloqueia se esgotado
            if (produto.isEsgotado()) {
                TextView btn = v.findViewById(R.id.btnAddCarrinho);
                btn.setEnabled(false);
                btn.setBackgroundColor(0xFF999999);
                btn.setText("PRODUTO ESGOTADO");
            }
        }

        // Tamanhos
        LinearLayout cTam = v.findViewById(R.id.containerTamanhos);
        List<String> tamanhos = Arrays.asList("RN", "P", "M", "G", "GG");
        for (String t : tamanhos) {
            cTam.addView(criarChip(t, t.equals(tamSel), view -> {
                tamSel = t;
                atualizarChips(cTam, t);
            }));
        }

        // Cores
        LinearLayout cCor = v.findViewById(R.id.containerCores);
        List<String> cores = Arrays.asList("Branco", "Azul", "Rosa", "Amarelo", "Verde");
        for (String c : cores) {
            cCor.addView(criarChip(c, c.equals(corSel), view -> {
                corSel = c;
                atualizarChips(cCor, c);
            }));
        }

        // Modelos
        LinearLayout cMod = v.findViewById(R.id.containerModelos);
        List<String> modelos = Arrays.asList("Padrão", "Premium", "Deluxe");
        for (String m : modelos) {
            cMod.addView(criarChip(m, m.equals(modSel), view -> {
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

        // Adicionar ao carrinho com variação
        v.findViewById(R.id.btnAddCarrinho).setOnClickListener(x -> {
            if (produto != null && !produto.isEsgotado()) {
                Variacao variacao = new Variacao(tamSel, corSel, modSel);
                CarrinhoRepository.getInstance().adicionar(produto, qtd, variacao);
                Toast.makeText(requireContext(),
                        qtd + " item(ns) adicionado(s) — " + variacao.resumo(),
                        Toast.LENGTH_LONG).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return v;
    }

    private TextView criarChip(String texto, boolean selecionado, View.OnClickListener onClick) {
        TextView chip = new TextView(getContext());
        chip.setText(texto);
        chip.setPadding(28, 18, 28, 18);
        chip.setTextSize(14f);
        chip.setGravity(Gravity.CENTER);
        aplicarEstilo(chip, selecionado);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 12, 0);
        chip.setLayoutParams(lp);

        chip.setOnClickListener(onClick);
        return chip;
    }

    private void aplicarEstilo(TextView chip, boolean selecionado) {
        if (selecionado) {
            chip.setBackgroundColor(0xFF37B6B0);
            chip.setTextColor(Color.WHITE);
        } else {
            chip.setBackgroundColor(0xFFF0F2F5);
            chip.setTextColor(0xFF1E293B);
        }
    }

    private void atualizarChips(LinearLayout container, String selecionado) {
        for (int i = 0; i < container.getChildCount(); i++) {
            TextView chip = (TextView) container.getChildAt(i);
            aplicarEstilo(chip, chip.getText().toString().equals(selecionado));
        }
    }
}