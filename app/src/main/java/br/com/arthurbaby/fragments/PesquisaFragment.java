package br.com.arthurbaby.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.arthurbaby.R;
import br.com.arthurbaby.adapters.ProdutoAdapter;
import br.com.arthurbaby.models.Produto;
import br.com.arthurbaby.repositories.ProdutoRepository;

public class PesquisaFragment extends Fragment {

    private RecyclerView rvResultados;
    private TextView tvInfo, chipPromocao, chipBaratos, chipLimpar;
    private EditText etBusca;
    private ProdutoAdapter adapter;

    private boolean filtroPromocao = false;
    private boolean filtroBaratos = false;
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
        chipPromocao = v.findViewById(R.id.chipPromocao);
        chipBaratos = v.findViewById(R.id.chipBaratos);
        chipLimpar = v.findViewById(R.id.chipLimpar);

        rvResultados.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Aplica filtros
        aplicarFiltros();

        etBusca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                termoAtual = s.toString();
                aplicarFiltros();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        chipPromocao.setOnClickListener(x -> {
            filtroPromocao = !filtroPromocao;
            aplicarFiltros();
        });

        chipBaratos.setOnClickListener(x -> {
            filtroBaratos = !filtroBaratos;
            aplicarFiltros();
        });

        chipLimpar.setOnClickListener(x -> {
            filtroPromocao = false;
            filtroBaratos = false;
            etBusca.setText("");
            termoAtual = "";
            aplicarFiltros();
        });

        return v;
    }

    private void aplicarFiltros() {
        // Estilo dos chips
        pintarChip(chipPromocao, filtroPromocao);
        pintarChip(chipBaratos, filtroBaratos);

        List<Produto> lista = ProdutoRepository.getInstance().buscar(termoAtual);

        // Aplica filtros extras
        java.util.List<Produto> filtrados = new java.util.ArrayList<>();
        for (Produto p : lista) {
            if (filtroPromocao && p.getPreco().doubleValue() >= 50) continue;
            if (filtroBaratos && p.getPreco().doubleValue() > 50) continue;
            filtrados.add(p);
        }

        adapter = new ProdutoAdapter(filtrados, produto -> {
            DetalheProdutoFragment frag = DetalheProdutoFragment.newInstance(produto);
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
    }

    private void pintarChip(TextView chip, boolean ativo) {
        if (ativo) {
            chip.setBackgroundColor(0xFF37B6B0);
            chip.setTextColor(Color.WHITE);
        } else {
            chip.setBackgroundColor(0xFFF0F2F5);
            chip.setTextColor(0xFF1E293B);
        }
    }
}