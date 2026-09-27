package br.com.arthurbaby.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import br.com.arthurbaby.MainActivity;
import br.com.arthurbaby.R;

public class SplashActivity extends AppCompatActivity {

    /**
     * Android 17+ (API 37) bloqueia o acesso a IPs da rede local (10.0.2.2, 192.168.x.x)
     * sem esta permissão — é onde o backend roda em desenvolvimento.
     */
    private static final String PERMISSAO_REDE_LOCAL = "android.permission.ACCESS_LOCAL_NETWORK";

    private final ActivityResultLauncher<String> pedirRedeLocal =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    concedida -> abrirMain());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LinearLayout containerLogo = findViewById(R.id.containerLogo);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.splash_scale);
        containerLogo.startAnimation(anim);

        // Sempre vai para a MainActivity (catálogo é público)
        new Handler().postDelayed(() -> {
            if (precisaPedirRedeLocal()) {
                pedirRedeLocal.launch(PERMISSAO_REDE_LOCAL);
            } else {
                abrirMain();
            }
        }, 2400);
    }

    private boolean precisaPedirRedeLocal() {
        return Build.VERSION.SDK_INT >= 37
                && ContextCompat.checkSelfPermission(this, PERMISSAO_REDE_LOCAL)
                != PackageManager.PERMISSION_GRANTED;
    }

    private void abrirMain() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }
}
