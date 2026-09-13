package br.com.arthurbaby;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import br.com.arthurbaby.fragments.CarrinhoFragment;
import br.com.arthurbaby.fragments.CategoriasFragment;
import br.com.arthurbaby.fragments.HomeFragment;
import br.com.arthurbaby.fragments.PerfilFragment;
import br.com.arthurbaby.fragments.PesquisaFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

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
    }

    private void trocarFragment(Fragment fragment) {
        br.com.arthurbaby.utils.NavUtils.trocarSemEmpilhar(this, fragment);
    }
}