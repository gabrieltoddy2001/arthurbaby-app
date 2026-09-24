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

        // 1) Mostra o loading
        LoadingUtils.mostrar(this);

        // 2) Pega o serviço Retrofit
        ApiService api = RetrofitClient.getApi(this);

        // 3) Faz a chamada POST /api/auth/login
        api.login(new AuthRequest(login, senha))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        LoadingUtils.esconder();

                        // 4) Verifica se a resposta foi OK (HTTP 200-299)
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse auth = response.body();

                            // 5) Salva o token e dados do usuário no "cofre"
                            TokenStorage.salvar(
                                    LoginActivity.this,
                                    auth.token,
                                    auth.usuarioId,
                                    auth.nome,
                                    auth.perfil
                            );

                            // 6) Mostra mensagem de boas-vindas
                            Toast.makeText(LoginActivity.this,
                                    "Bem-vindo, " + auth.nome + "!",
                                    Toast.LENGTH_SHORT).show();

                            // 7) Vai para a Home
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            // 8) Erro do servidor (401, 400, etc)
                            Toast.makeText(LoginActivity.this,
                                    "Login inválido. Verifique seus dados.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        LoadingUtils.esconder();

                        // 9) Erro de rede (backend desligado, sem internet, etc)
                        Toast.makeText(LoginActivity.this,
                                "Erro de conexão: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}