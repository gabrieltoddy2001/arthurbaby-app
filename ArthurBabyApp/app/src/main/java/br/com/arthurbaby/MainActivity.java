package br.com.arthurbaby;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.badge.BadgeDrawable;
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

    /** Mostra a quantidade de itens no ícone do carrinho (some quando está vazio). */
    private final CarrinhoRepository.OnCarrinhoAlterado contadorCarrinho = total -> {
        if (bottomNav == null) return;
        if (total > 0) {
            BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_carrinho);
            badge.setBackgroundColor(androidx.core.content.ContextCompat.getColor(this, R.color.verde_arthur));
            badge.setBadgeTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.branco));
            badge.setMaxCharacterCount(3); // acima de 99 mostra "99+"
            badge.setNumber(total);
            badge.setVisible(true);
        } else {
            bottomNav.removeBadge(R.id.nav_carrinho);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        br.com.arthurbaby.utils.BarrasSistema.aplicar(this);
        br.com.arthurbaby.utils.RedeLocalPermissao.solicitarSeNecessario(this);

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
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                    return;
                }
                Fragment atual = getSupportFragmentManager()
                        .findFragmentById(R.id.frameContainer);
                if (!(atual instanceof HomeFragment)) {
                    // O listener do menu já troca para a Home (antes trocava duas vezes)
                    if (bottomNav.getSelectedItemId() != R.id.nav_home) {
                        bottomNav.setSelectedItemId(R.id.nav_home);
                    } else {
                        trocarFragment(new HomeFragment());
                    }
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        CarrinhoRepository.getInstance().observar(contadorCarrinho);
    }

    @Override
    protected void onStop() {
        CarrinhoRepository.getInstance().pararDeObservar(contadorCarrinho);
        super.onStop();
    }

    /**
     * Troca o fragment com animação de fade.
     */
    private void trocarFragment(Fragment fragment) {
        getSupportFragmentManager().popBackStack(
                null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out)
                .replace(R.id.frameContainer, fragment)
                .commit();
    }
}