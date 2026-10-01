package br.com.arthurbaby.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import br.com.arthurbaby.MainActivity;
import br.com.arthurbaby.R;
import br.com.arthurbaby.network.ApiService;
import br.com.arthurbaby.network.RetrofitClient;
import br.com.arthurbaby.network.TokenStorage;
import br.com.arthurbaby.network.dto.AuthRequest;
import br.com.arthurbaby.network.dto.AuthResponse;
import br.com.arthurbaby.utils.ErrorUtils;
import br.com.arthurbaby.utils.LoadingUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etLogin, etSenha;
    private MaterialButton btnEntrar;
    private TextView tvCadastrar, tvEsqueciSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLogin = findViewById(R.id.etLogin);
        etSenha = findViewById(R.id.etSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        tvCadastrar = findViewById(R.id.tvCadastrar);
        tvEsqueciSenha = findViewById(R.id.tvEsqueciSenha);

        btnEntrar.setOnClickListener(v -> validarLogin());

        tvCadastrar.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, CadastroActivity.class)));

        tvEsqueciSenha.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RecuperarSenhaActivity.class)));
    }

    private void validarLogin() {
        String login = etLogin.getText() != null ? etLogin.getText().toString().trim() : "";
        String senha = etSenha.getText() != null ? etSenha.getText().toString().trim() : "";

        if (TextUtils.isEmpty(login)) {
            etLogin.setError("Informe e-mail ou CPF");
            return;
        }
        if (TextUtils.isEmpty(senha) || senha.length() < 4) {
            etSenha.setError("Senha deve ter ao menos 4 caracteres");
            return;
        }

        LoadingUtils.mostrar(this);

        ApiService api = RetrofitClient.getApi(this);
        api.login(new AuthRequest(login, senha))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        LoadingUtils.esconder();

                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse auth = response.body();

                            TokenStorage.salvar(
                                    LoginActivity.this,
                                    auth.token,
                                    auth.usuarioId,
                                    auth.nome,
                                    auth.perfil
                            );

                            Toast.makeText(LoginActivity.this,
                                    "Bem-vindo, " + auth.nome + "!",
                                    Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    ErrorUtils.extrairMensagem(response),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        LoadingUtils.esconder();
                        Toast.makeText(LoginActivity.this,
                                "Erro de conexão: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}