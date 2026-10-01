package br.com.arthurbaby;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import br.com.arthurbaby.fragments.CarrinhoFragment;
import br.com.arthurbaby.fragments.CategoriasFragment;
import br.com.arthurbaby.fragments.HomeFragment;
import br.com.arthurbaby.fragments.PerfilFragment;
import br.com.arthurbaby.fragments.PesquisaFragment;
import br.com.arthurbaby.repositories.CarrinhoRepository;
import br.com.arthurbaby.repositories.FavoritoRepository;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CarrinhoRepository.getInstance().carregar(this);
        FavoritoRepository.getInstance().sincronizar(this);

        bottomNav = findViewById(R.id.bottomNav);

        if (savedInstanceState == null) {
            trocarFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) fragment = new HomeFragment();
            else if (id == R.id.nav_categorias) fragment = new CategoriasFragment();
            else if (id == R.id.nav_pesquisa) fragment = new PesquisaFragment();
            else if (id == R.id.nav_carrinho) fragment = new CarrinhoFragment();
            else if (id == R.id.nav_perfil) fragment = new PerfilFragment();

            if (fragment != null) trocarFragment(fragment);
            return true;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 1) Se tem fragment empilhado, volta
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                    return;
                }

                // 2) Se não está na Home, vai pra Home
                Fragment atual = getSupportFragmentManager()
                        .findFragmentById(R.id.frameContainer);
                if (!(atual instanceof HomeFragment)) {
                    trocarFragment(new HomeFragment());
                    bottomNav.setSelectedItemId(R.id.nav_home);
                    return;
                }

                // 3) Já está na Home, fecha o app
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    /**
     * Limpa a pilha inteira e troca o fragment.
     * Isso garante que ao mudar de aba, não acumule lixo.
     */
    private void trocarFragment(Fragment fragment) {
        getSupportFragmentManager().popBackStack(
                null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameContainer, fragment)
                .commit();
    }
}