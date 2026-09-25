package br.com.arthurbaby.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import br.com.arthurbaby.MainActivity;
import br.com.arthurbaby.R;
import br.com.arthurbaby.network.TokenStorage;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Animação
        LinearLayout containerLogo = findViewById(R.id.containerLogo);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.splash_scale);
        containerLogo.startAnimation(anim);

        // Verifica se já tem token salvo
        String token = TokenStorage.getToken(this);

        new Handler().postDelayed(() -> {
            if (token != null && !token.isEmpty()) {
                // Já logado → vai direto para a Home
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                // Não logado → tela de Login
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, 2400);
    }
}