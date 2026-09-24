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
import br.com.arthurbaby.network.dto.RedefinirSenhaRequest;
import br.com.arthurbaby.utils.LoadingUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RedefinirSenhaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redefinir_senha);

        TextInputEditText etToken = findViewById(R.id.etToken);
        TextInputEditText etNovaSenha = findViewById(R.id.etNovaSenha);
        MaterialButton btnRedefinir = findViewById(R.id.btnRedefinir);

        // Se o usuário chegou aqui com um token vindo de outra tela, preenche
        String tokenInicial = getIntent().getStringExtra("token");
        if (tokenInicial != null && !tokenInicial.isEmpty()) {
            etToken.setText(tokenInicial);
        }

        btnRedefinir.setOnClickListener(v -> {
            String token = etToken.getText() != null ? etToken.getText().toString().trim() : "";
            String nova = etNovaSenha.getText() != null ? etNovaSenha.getText().toString().trim() : "";

            if (TextUtils.isEmpty(token)) {
                etToken.setError("Informe o código recebido");
                return;
            }
            if (TextUtils.isEmpty(nova) || nova.length() < 4) {
                etNovaSenha.setError("Senha deve ter ao menos 4 caracteres");
                return;
            }

            LoadingUtils.mostrar(this);

            ApiService api = RetrofitClient.getApi(this);
            api.redefinirSenha(new RedefinirSenhaRequest(token, nova))
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            LoadingUtils.esconder();

                            if (response.isSuccessful()) {
                                Toast.makeText(RedefinirSenhaActivity.this,
                                        "Senha alterada! Faça login.",
                                        Toast.LENGTH_LONG).show();

                                Intent i = new Intent(RedefinirSenhaActivity.this,
                                        LoginActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                finish();
                            } else {
                                Toast.makeText(RedefinirSenhaActivity.this,
                                        "Código inválido ou expirado",
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            LoadingUtils.esconder();
                            Toast.makeText(RedefinirSenhaActivity.this,
                                    "Erro de conexão: " + t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        findViewById(R.id.tvVoltar).setOnClickListener(v -> finish());
    }
}