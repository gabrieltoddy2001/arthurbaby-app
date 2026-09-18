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

        // Simula chamada à API
        br.com.arthurbaby.utils.LoadingUtils.mostrar(this);

        new android.os.Handler().postDelayed(() -> {
            br.com.arthurbaby.utils.LoadingUtils.esconder();
            Toast.makeText(this, "Login realizado (mock)", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }, 900);
    }
}