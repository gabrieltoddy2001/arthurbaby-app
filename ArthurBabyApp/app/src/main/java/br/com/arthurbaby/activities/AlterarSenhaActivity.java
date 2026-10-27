package br.com.arthurbaby.activities;

import android.content.Intent;
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
import br.com.arthurbaby.network.dto.AlterarSenhaRequest;
import br.com.arthurbaby.utils.ErrorUtils;
import br.com.arthurbaby.utils.LoadingUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlterarSenhaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar_senha);

        TextInputEditText etSenhaAtual = findViewById(R.id.etSenhaAtual);
        TextInputEditText etNovaSenha = findViewById(R.id.etNovaSenha);
        TextInputEditText etConfirmar = findViewById(R.id.etConfirmarSenha);
        MaterialButton btnSalvar = findViewById(R.id.btnSalvarSenha);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        btnSalvar.setOnClickListener(v -> {
            String atual = etSenhaAtual.getText() != null ? etSenhaAtual.getText().toString().trim() : "";
            String nova = etNovaSenha.getText() != null ? etNovaSenha.getText().toString().trim() : "";
            String confirmar = etConfirmar.getText() != null ? etConfirmar.getText().toString().trim() : "";

            if (TextUtils.isEmpty(atual)) { etSenhaAtual.setError("Informe a senha atual"); return; }
            if (TextUtils.isEmpty(nova) || nova.length() < 4) { etNovaSenha.setError("Nova senha muito curta"); return; }
            if (!nova.equals(confirmar)) { etConfirmar.setError("As senhas não coincidem"); return; }

            Long clienteId = TokenStorage.getUsuarioId(this);
            if (clienteId == null) {
                Toast.makeText(this, "Sessão expirada", Toast.LENGTH_SHORT).show();
                return;
            }

            LoadingUtils.mostrar(this);

            ApiService api = RetrofitClient.getApi(this);
            api.alterarSenha(clienteId, new AlterarSenhaRequest(atual, nova))
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            LoadingUtils.esconder();

                            if (response.isSuccessful()) {
                                Toast.makeText(AlterarSenhaActivity.this,
                                        "Senha alterada! Faça login novamente.",
                                        Toast.LENGTH_LONG).show();

                                // 🚀 LOGOUT AUTOMÁTICO
                                TokenStorage.limpar(AlterarSenhaActivity.this);
                                br.com.arthurbaby.repositories.FavoritoRepository.getInstance().limpar();

                                Intent i = new Intent(AlterarSenhaActivity.this, LoginActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                finish();
                            } else {
                                Toast.makeText(AlterarSenhaActivity.this,
                                        ErrorUtils.extrairMensagem(response),
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            LoadingUtils.esconder();
                            Toast.makeText(AlterarSenhaActivity.this,
                                    "Erro de conexão: " + t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}