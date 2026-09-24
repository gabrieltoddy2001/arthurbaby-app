package br.com.arthurbaby.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import br.com.arthurbaby.R;
import br.com.arthurbaby.adapters.EnderecoAdapter;
import br.com.arthurbaby.models.Endereco;
import br.com.arthurbaby.network.ApiService;
import br.com.arthurbaby.network.Conversor;
import br.com.arthurbaby.network.RetrofitClient;
import br.com.arthurbaby.network.TokenStorage;
import br.com.arthurbaby.network.dto.EnderecoRequest;
import br.com.arthurbaby.network.dto.EnderecoResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnderecosFragment extends Fragment {

    private RecyclerView rv;
    private EnderecoAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_enderecos, container, false);

        v.findViewById(R.id.btnVoltar).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack());

        rv = v.findViewById(R.id.rvEnderecos);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        v.findViewById(R.id.btnNovoEndereco).setOnClickListener(x ->
                abrirDialogNovoEndereco());

        carregarEnderecos();
        return v;
    }

    private void carregarEnderecos() {
        Long clienteId = TokenStorage.getUsuarioId(requireContext());
        if (clienteId == null) return;

        ApiService api = RetrofitClient.getApi(requireContext());
        api.listarEnderecos(clienteId).enqueue(new Callback<List<EnderecoResponse>>() {
            @Override
            public void onResponse(Call<List<EnderecoResponse>> call,
                                   Response<List<EnderecoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter = new EnderecoAdapter(
                            Conversor.paraEnderecos(response.body()),
                            EnderecosFragment.this::removerEndereco);
                    rv.setAdapter(adapter);
                } else {
                    Toast.makeText(requireContext(),
                            "Erro ao carregar endereços", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<EnderecoResponse>> call, Throwable t) {
                Toast.makeText(requireContext(),
                        "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removerEndereco(Endereco e) {
        Long clienteId = TokenStorage.getUsuarioId(requireContext());
        if (clienteId == null || e.getId() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Remover endereço")
                .setMessage("Deseja remover este endereço?")
                .setPositiveButton("Sim", (d, w) -> {
                    ApiService api = RetrofitClient.getApi(requireContext());
                    api.removerEndereco(clienteId, e.getId())
                            .enqueue(new Callback<Void>() {
                                @Override public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(requireContext(),
                                                "Endereço removido", Toast.LENGTH_SHORT).show();
                                        carregarEnderecos();
                                    }
                                }
                                @Override public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(requireContext(),
                                            "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Não", null)
                .show();
    }

    /**
     * Abre um dialog simples para cadastrar endereço.
     * (Depois podemos trocar por uma tela cheia.)
     */
    private void abrirDialogNovoEndereco() {
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(40, 20, 40, 20);

        TextInputEditText etCep = new TextInputEditText(getContext());
        etCep.setHint("CEP");
        container.addView(etCep);

        TextInputEditText etLog = new TextInputEditText(getContext());
        etLog.setHint("Logradouro");
        container.addView(etLog);

        TextInputEditText etNum = new TextInputEditText(getContext());
        etNum.setHint("Número");
        container.addView(etNum);

        TextInputEditText etComp = new TextInputEditText(getContext());
        etComp.setHint("Complemento");
        container.addView(etComp);

        TextInputEditText etBairro = new TextInputEditText(getContext());
        etBairro.setHint("Bairro");
        container.addView(etBairro);

        TextInputEditText etCidade = new TextInputEditText(getContext());
        etCidade.setHint("Cidade");
        container.addView(etCidade);

        TextInputEditText etUf = new TextInputEditText(getContext());
        etUf.setHint("UF");
        container.addView(etUf);

        new AlertDialog.Builder(requireContext())
                .setTitle("Novo endereço")
                .setView(container)
                .setPositiveButton("Salvar", (d, w) -> {
                    EnderecoRequest req = new EnderecoRequest(
                            texto(etCep), texto(etLog), texto(etNum),
                            texto(etComp), texto(etBairro), texto(etCidade),
                            texto(etUf), null, true);

                    Long clienteId = TokenStorage.getUsuarioId(requireContext());
                    if (clienteId == null) return;

                    ApiService api = RetrofitClient.getApi(requireContext());
                    api.criarEndereco(clienteId, req).enqueue(new Callback<EnderecoResponse>() {
                        @Override public void onResponse(Call<EnderecoResponse> call, Response<EnderecoResponse> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(),
                                        "Endereço salvo", Toast.LENGTH_SHORT).show();
                                carregarEnderecos();
                            } else {
                                Toast.makeText(requireContext(),
                                        "Erro ao salvar", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override public void onFailure(Call<EnderecoResponse> call, Throwable t) {
                            Toast.makeText(requireContext(),
                                    "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String texto(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}