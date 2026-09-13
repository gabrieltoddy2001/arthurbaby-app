package br.com.arthurbaby.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import br.com.arthurbaby.R;
import br.com.arthurbaby.utils.LoadingUtils;

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

            // MOCK: POST /api/auth/recuperar-senha
            LoadingUtils.mostrar(this);

            new android.os.Handler().postDelayed(() -> {
                LoadingUtils.esconder();
                Toast.makeText(this,
                        "Se o e-mail estiver cadastrado, você receberá um link.",
                        Toast.LENGTH_LONG).show();
                finish();
            }, 900);
        });

        findViewById(R.id.tvVoltar).setOnClickListener(v -> finish());
    }
}