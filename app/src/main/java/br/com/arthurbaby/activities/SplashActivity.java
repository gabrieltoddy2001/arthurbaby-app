package br.com.arthurbaby.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import br.com.arthurbaby.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Aplica a animação de escala + fade-in
        LinearLayout containerLogo = findViewById(R.id.containerLogo);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.splash_scale);
        containerLogo.startAnimation(anim);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, 2200);
    }
}