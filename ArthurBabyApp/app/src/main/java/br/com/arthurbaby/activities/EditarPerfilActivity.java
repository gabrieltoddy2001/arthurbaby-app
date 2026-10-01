package br.com.arthurbaby.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import br.com.arthurbaby.R;
import br.com.arthurbaby.network.ApiService;
import br.com.arthurbaby.network.RetrofitClient;
import br.com.arthurbaby.network.TokenStorage;
import br.com.arthurbaby.network.dto.ClienteRequest;
import br.com.arthurbaby.network.dto.UsuarioResponse;
import br.com.arthurbaby.utils.ErrorUtils;
import br.com.arthurbaby.utils.LoadingUtils;
import br.com.arthurbaby.utils.MaskUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarPerfilActivity extends AppCompatActivity {

    private TextInputEditText etNome, etEmail, etTelefone, etCpf;
    private Long clienteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        etNome = findViewById(R.id.etNome);
        etEmail = findViewById(R.id.etEmail);
        etTelefone = findViewById(R.id.etTelefone);
        etCpf = findViewById(R.id.etCpf);

        MaskUtils.aplicarMascaraTelefone(etTelefone);
        MaskUtils.aplicarMascaraCpf(etCpf);

        clienteId = TokenStorage.getUsuarioId(this);

        findViewById(R.id.btnVoltar).setOnClickListener(x -> finish());
        findViewById(R.id.btnSalvar).setOnClickListener(x -> salvar());

        carregarPerfil();
    }

    private void carregarPerfil() {
        if (clienteId == null) return;

        LoadingUtils.mostrar(this);

        ApiService api = RetrofitClient.getApi(this);
        api.buscarCliente(clienteId).enqueue(new Callback<UsuarioResponse>() {
            @Override
            public void onResponse(Call<UsuarioResponse> call, Response<UsuarioResponse> response) {
                LoadingUtils.esconder();

                if (response.isSuccessful() && response.body() != null) {
                    UsuarioResponse u = response.body();
                    if (u.nomeCompleto != null) etNome.setText(u.nomeCompleto);
                    if (u.email != null) etEmail.setText(u.email);
                    if (u.telefone != null) etTelefone.setText(u.telefone);
                    if (u.cpf != null) etCpf.setText(u.cpf);
                } else {
                    Toast.makeText(EditarPerfilActivity.this,
                            ErrorUtils.extrairMensagem(response), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UsuarioResponse> call, Throwable t) {
                LoadingUtils.esconder();
                Toast.makeText(EditarPerfilActivity.this,
                        "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void salvar() {
        String nome = texto(etNome);
        String email = texto(etEmail);
        String telefone = texto(etTelefone);
        String cpf = texto(etCpf);

        if (TextUtils.isEmpty(nome)) {
            etNome.setError("Informe seu nome");
            return;
        }
        if (clienteId == null) {
            Toast.makeText(this, "Sessão expirada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        LoadingUtils.mostrar(this);

        ClienteRequest req = new ClienteRequest(nome, email, cpf, telefone);
        ApiService api = RetrofitClient.getApi(this);
        api.atualizarCliente(clienteId, req).enqueue(new Callback<UsuarioResponse>() {
            @Override
            public void onResponse(Call<UsuarioResponse> call, Response<UsuarioResponse> response) {
                LoadingUtils.esconder();

                if (response.isSuccessful() && response.body() != null) {
                    UsuarioResponse u = response.body();

                    TokenStorage.salvar(
                            EditarPerfilActivity.this,
                            TokenStorage.getToken(EditarPerfilActivity.this),
                            u.id, u.nomeCompleto, u.perfil);

                    Toast.makeText(EditarPerfilActivity.this,
                            "Dados atualizados!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditarPerfilActivity.this,
                            ErrorUtils.extrairMensagem(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<UsuarioResponse> call, Throwable t) {
                LoadingUtils.esconder();
                Toast.makeText(EditarPerfilActivity.this,
                        "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String texto(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}