package br.com.arthurbaby.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
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
import br.com.arthurbaby.utils.AuthGuard;
import br.com.arthurbaby.utils.ErrorUtils;
import br.com.arthurbaby.utils.LoadingView;
import br.com.arthurbaby.utils.MaskUtils;
import br.com.arthurbaby.utils.ViaCepService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnderecosFragment extends Fragment {

    private RecyclerView rv;
    private EnderecoAdapter adapter;
    private View progressBar, btnNovo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_enderecos, container, false);

        v.findViewById(R.id.btnVoltar).setOnClickListener(x ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        rv = v.findViewById(R.id.rvEnderecos);
        progressBar = v.findViewById(R.id.progressBar);
        btnNovo = v.findViewById(R.id.btnNovoEndereco);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        if (!AuthGuard.estaLogado(requireContext())) {
            AuthGuard.mostrarDialogLogin(requireActivity(),
                    "Entre para gerenciar seus endereços.");
            requireActivity().getSupportFragmentManager().popBackStack();
            return v;
        }

        btnNovo.setOnClickListener(x -> abrirDialogNovoEndereco());
        carregarEnderecos();
        return v;
    }

    private void carregarEnderecos() {
        Long clienteId = TokenStorage.getUsuarioId(requireContext());
        if (clienteId == null) return;

        LoadingView.mostrar(progressBar, rv, btnNovo);

        ApiService api = RetrofitClient.getApi(requireContext());
        api.listarEnderecos(clienteId).enqueue(new Callback<List<EnderecoResponse>>() {
            @Override
            public void onResponse(Call<List<EnderecoResponse>> call,
                                   Response<List<EnderecoResponse>> response) {
                LoadingView.esconder(progressBar, rv, btnNovo);

                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    adapter = new EnderecoAdapter(
                            Conversor.paraEnderecos(response.body()),
                            EnderecosFragment.this::removerEndereco);
                    rv.setAdapter(adapter);
                } else {
                    Toast.makeText(requireContext(),
                            ErrorUtils.extrairMensagem(response), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<EnderecoResponse>> call, Throwable t) {
                LoadingView.esconder(progressBar, rv, btnNovo);
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        ErrorUtils.mensagemFalha(t), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removerEndereco(Endereco e) {
        Long clienteId = TokenStorage.getUsuarioId(requireContext());
        if (clienteId == null || e.getId() == null) return;

        new MaterialAlertDialogBuilder(requireContext(), R.style.DialogArredondado)
                .setTitle("Remover endereço")
                .setMessage("Deseja remover este endereço?")
                .setPositiveButton("Sim", (d, w) -> {
                    ApiService api = RetrofitClient.getApi(requireContext());
                    api.removerEndereco(clienteId, e.getId())
                            .enqueue(new Callback<Void>() {
                                @Override public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (!isAdded()) return;
                                    if (response.isSuccessful()) {
                                        Toast.makeText(requireContext(),
                                                "Endereço removido", Toast.LENGTH_SHORT).show();
                                        carregarEnderecos();
                                    } else {
                                        Toast.makeText(requireContext(),
                                                ErrorUtils.extrairMensagem(response),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override public void onFailure(Call<Void> call, Throwable t) {
                                    if (!isAdded()) return;
                                    Toast.makeText(requireContext(),
                                            ErrorUtils.mensagemFalha(t), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void abrirDialogNovoEndereco() {
        // Formulário com campos arredondados (layout dialog_novo_endereco.xml)
        View form = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_novo_endereco, null, false);
        TextInputLayout tilCep = form.findViewById(R.id.tilCep);
        TextInputEditText etCep = form.findViewById(R.id.etCep);
        TextInputEditText etLog = form.findViewById(R.id.etLogradouro);
        TextInputEditText etNum = form.findViewById(R.id.etNumero);
        TextInputEditText etComp = form.findViewById(R.id.etComplemento);
        TextInputEditText etBairro = form.findViewById(R.id.etBairro);
        TextInputEditText etCidade = form.findViewById(R.id.etCidade);
        TextInputEditText etUf = form.findViewById(R.id.etUf);
        MaterialButton btnSalvar = form.findViewById(R.id.btnSalvarEndereco);

        // CEP no formato 00000-000 e busca automática no ViaCEP (igual à tela Criar Conta)
        MaskUtils.aplicarMascaraCep(etCep);
        final String[] ultimoCepBuscado = {""}; // a máscara reescreve o texto; evita buscar 2x
        etCep.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                String cep = s.toString().replaceAll("\\D", "");
                tilCep.setError(null);
                if (cep.length() != 8) { tilCep.setHelperText(null); ultimoCepBuscado[0] = ""; return; }
                if (cep.equals(ultimoCepBuscado[0])) return;
                ultimoCepBuscado[0] = cep;
                tilCep.setHelperText("Buscando endereço...");
                ViaCepService.buscar(cep, new ViaCepService.Callback() {
                    @Override public void onResultado(ViaCepService.EnderecoCep e) {
                        if (!isAdded()) return;
                        tilCep.setHelperText(null);
                        if (!e.logradouro.isEmpty()) etLog.setText(e.logradouro);
                        if (!e.bairro.isEmpty()) etBairro.setText(e.bairro);
                        if (!e.cidade.isEmpty()) etCidade.setText(e.cidade);
                        if (!e.uf.isEmpty()) etUf.setText(e.uf);
                        etNum.requestFocus();
                    }
                    @Override public void onErro(String mensagem) {
                        if (!isAdded()) return;
                        tilCep.setHelperText(null);
                        tilCep.setError(mensagem);
                    }
                });
            }
        });

        androidx.appcompat.app.AlertDialog dialog =
                new MaterialAlertDialogBuilder(requireContext(), R.style.DialogArredondado)
                        .setIcon(R.drawable.ic_endereco_titulo)
                        .setTitle("Novo Endereço")
                        .setView(form)
                        .create();

        form.findViewById(R.id.btnCancelarEndereco).setOnClickListener(x -> dialog.dismiss());

        btnSalvar.setOnClickListener(x -> {
            // Campos obrigatórios (o modal só fecha quando o endereço for salvo)
            if (texto(etCep).replaceAll("\\D", "").length() != 8) { tilCep.setError("Informe um CEP válido"); return; }
            if (texto(etLog).isEmpty()) { etLog.setError("Informe o logradouro"); return; }
            if (texto(etNum).isEmpty()) { etNum.setError("Informe o número"); return; }
            if (texto(etBairro).isEmpty()) { etBairro.setError("Informe o bairro"); return; }
            if (texto(etCidade).isEmpty()) { etCidade.setError("Informe a cidade"); return; }
            if (texto(etUf).length() != 2) { etUf.setError("UF com 2 letras"); return; }

            Long clienteId = TokenStorage.getUsuarioId(requireContext());
            if (clienteId == null) { dialog.dismiss(); return; }

            EnderecoRequest req = new EnderecoRequest(
                    texto(etCep), texto(etLog), texto(etNum),
                    texto(etComp), texto(etBairro), texto(etCidade),
                    texto(etUf).toUpperCase(java.util.Locale.ROOT), null, true);

            btnSalvar.setEnabled(false);
            btnSalvar.setText("SALVANDO...");

            ApiService api = RetrofitClient.getApi(requireContext());
            api.criarEndereco(clienteId, req).enqueue(new Callback<EnderecoResponse>() {
                @Override public void onResponse(Call<EnderecoResponse> call, Response<EnderecoResponse> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful()) {
                        dialog.dismiss();
                        Toast.makeText(requireContext(),
                                "Endereço salvo", Toast.LENGTH_SHORT).show();
                        carregarEnderecos();
                    } else {
                        reativar(btnSalvar);
                        Toast.makeText(requireContext(),
                                ErrorUtils.extrairMensagem(response),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(Call<EnderecoResponse> call, Throwable t) {
                    if (!isAdded()) return;
                    reativar(btnSalvar);
                    Toast.makeText(requireContext(),
                            ErrorUtils.mensagemFalha(t), Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void reativar(MaterialButton btn) {
        btn.setEnabled(true);
        btn.setText("SALVAR");
    }

    private String texto(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}