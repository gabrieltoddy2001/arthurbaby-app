package br.com.arthurbaby.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import br.com.arthurbaby.MainActivity;
import br.com.arthurbaby.R;
import br.com.arthurbaby.utils.RedeLocalPermissao;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        br.com.arthurbaby.utils.BarrasSistema.aplicar(this);

        LinearLayout containerLogo = findViewById(R.id.containerLogo);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.splash_scale);
        containerLogo.startAnimation(anim);

        // Android 17: a permissão de rede local é pedida aqui, ANTES de abrir o Início.
        // Antes era pedida já com a Home aberta: as buscas saíam sem permissão, falhavam
        // e a tela ficava sem categorias/produtos mesmo depois de permitir.
        aguardandoPermissao = RedeLocalPermissao.precisaPedir(this);
        if (aguardandoPermissao) RedeLocalPermissao.solicitarSeNecessario(this);

        // Sempre vai para a MainActivity (catálogo é público)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            tempoMinimoPassou = true;
            abrirInicioSePronto();
        }, 2400);
    }

    private boolean aguardandoPermissao;
    private boolean tempoMinimoPassou;
    private boolean abriu;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RedeLocalPermissao.REQUEST_CODE) {
            aguardandoPermissao = false;  // permitiu ou não, segue para o Início
            abrirInicioSePronto();
        }
    }

    /** Abre o Início só depois do tempo mínimo do splash e da resposta da permissão. */
    private void abrirInicioSePronto() {
        if (abriu || !tempoMinimoPassou || aguardandoPermissao || isFinishing()) return;
        abriu = true;
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }
}