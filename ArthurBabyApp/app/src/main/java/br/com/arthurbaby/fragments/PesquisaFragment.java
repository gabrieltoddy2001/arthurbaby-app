package br.com.arthurbaby.fragments;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PesquisaFragment extends Fragment {

    private RecyclerView rvResultados;
    private TextView tvInfo, chipTodas, chipPromocao, chipBaratos, chipLimpar;
    private EditText etBusca;
    private ProdutoAdapter adapter;

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
        chipTodas = v.findViewById(R.id.chipTodas);
        chipPromocao = v.findViewById(R.id.chipPromocao);
        chipBaratos = v.findViewById(R.id.chipBaratos);
        chipLimpar = v.findViewById(R.id.chipLimpar);

        v.findViewById(R.id.btnVoltarBusca).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack());

        rvResultados.setLayoutManager(new GridLayoutManager(getContext(), 2));

        buscarProdutos(null, null);

        etBusca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                termoAtual = s.toString();
                aplicarFiltro();
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
            aplicarFiltro();
        });

        return v;
    }

    private void aplicarFiltro() {
        pintarChip(chipTodas, "TODAS".equals(filtroAtual));
        pintarChip(chipPromocao, "PROMOCAO".equals(filtroAtual));
        pintarChip(chipBaratos, "BARATOS".equals(filtroAtual));

        if ("PROMOCAO".equals(filtroAtual)) {
            // Filtro de promoção (preço < 50 no front, até o back implementar)
            buscarProdutos(termoAtual, null);
        } else if ("BARATOS".equals(filtroAtual)) {
            buscarProdutos(termoAtual, null);
        } else {
            buscarProdutos(termoAtual, null);
        }
    }

    private void buscarProdutos(String busca, Long categoriaId) {
        ApiService api = RetrofitClient.getApi(requireContext());
        api.listarProdutos(busca, categoriaId, null, 0, 50)
                .enqueue(new Callback<PageResponse<ProdutoResponse>>() {
                    @Override
                    public void onResponse(Call<PageResponse<ProdutoResponse>> call,
                                           Response<PageResponse<ProdutoResponse>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().content != null) {

                            java.util.List<br.com.arthurbaby.models.Produto> produtos =
                                    Conversor.paraProdutos(response.body().content);

                            // Filtro local de preço/promoção
                            java.util.List<br.com.arthurbaby.models.Produto> filtrados =
                                    new java.util.ArrayList<>();
                            for (br.com.arthurbaby.models.Produto p : produtos) {
                                double preco = p.getPreco().doubleValue();
                                if ("PROMOCAO".equals(filtroAtual) && preco >= 50) continue;
                                if ("BARATOS".equals(filtroAtual) && preco > 50) continue;
                                filtrados.add(p);
                            }

                            adapter = new ProdutoAdapter(filtrados, produto -> {
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
                        Toast.makeText(requireContext(),
                                "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pintarChip(TextView chip, boolean ativo) {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(40f);
        if (ativo) {
            bg.setColor(0xFF37B6B0);
            chip.setTextColor(Color.WHITE);
        } else {
            bg.setColor(0xFFF0F2F5);
            chip.setTextColor(0xFF1E293B);
        }
        chip.setBackground(bg);
    }
}