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
import br.com.arthurbaby.network.dto.RecuperarSenhaRequest;
import br.com.arthurbaby.utils.LoadingUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecuperarSenhaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_senha);

        TextInputEditText etEmail = findViewById(R.id.etEmailRecuperar);
        MaterialButton btnEnviar = findViewById(R.id.btnEnviar);

        btnEnviar.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Informe seu e-mail");
                return;
            }

            LoadingUtils.mostrar(this);

            ApiService api = RetrofitClient.getApi(this);
            api.recuperarSenha(new RecuperarSenhaRequest(email))
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            LoadingUtils.esconder();

                            if (response.isSuccessful()) {
                                Toast.makeText(RecuperarSenhaActivity.this,
                                        "Se o e-mail estiver cadastrado, você receberá o código.",
                                        Toast.LENGTH_LONG).show();

                                // Abre a tela de redefinir senha
                                startActivity(new Intent(RecuperarSenhaActivity.this,
                                        RedefinirSenhaActivity.class));
                                finish();
                            } else {
                                Toast.makeText(RecuperarSenhaActivity.this,
                                        "Erro ao solicitar recuperação",
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            LoadingUtils.esconder();
                            Toast.makeText(RecuperarSenhaActivity.this,
                                    "Erro de conexão: " + t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        findViewById(R.id.tvVoltar).setOnClickListener(v -> finish());
    }
}