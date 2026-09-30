package br.com.arthurbaby.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import br.com.arthurbaby.R;
import br.com.arthurbaby.activities.LoginActivity;
import br.com.arthurbaby.network.ApiService;
import br.com.arthurbaby.network.RetrofitClient;
import br.com.arthurbaby.network.TokenStorage;
import br.com.arthurbaby.network.dto.UsuarioResponse;
import br.com.arthurbaby.repositories.FavoritoRepository;
import br.com.arthurbaby.utils.AuthGuard;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_perfil, container, false);

        TextView tvNome = v.findViewById(R.id.tvNome);
        TextView tvEmail = v.findViewById(R.id.tvEmail);

        // === VISITANTE ===
        if (!AuthGuard.estaLogado(requireContext())) {
            tvNome.setText("Bem-vindo(a)!");
            tvEmail.setText("Entre para aproveitar tudo");

            v.findViewById(R.id.opMeusPedidos).setOnClickListener(x ->
                    AuthGuard.mostrarDialogLogin(requireActivity(),
                            "Entre para ver seus pedidos."));

            v.findViewById(R.id.opEnderecos).setOnClickListener(x ->
                    AuthGuard.mostrarDialogLogin(requireActivity(),
                            "Entre para gerenciar endereços."));

            v.findViewById(R.id.opFavoritos).setOnClickListener(x ->
                    AuthGuard.mostrarDialogLogin(requireActivity(),
                            "Entre para ver seus favoritos."));

            v.findViewById(R.id.opAjuda).setOnClickListener(x ->
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frameContainer, new AjudaFragment())
                            .addToBackStack(null)
                            .commit()
            );

            v.findViewById(R.id.opSobre).setOnClickListener(x ->
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frameContainer, new SobreFragment())
                            .addToBackStack(null)
                            .commit()
            );

            try {
                LinearLayout opSair = v.findViewById(R.id.opSair);
                if (opSair != null) {
                    TextView tvSair = encontrarTextView(opSair);
                    if (tvSair != null) tvSair.setText("Entrar / Cadastrar");
                    opSair.setOnClickListener(x ->
                            startActivity(new Intent(getActivity(), LoginActivity.class)));
                }
            } catch (Exception ignored) {}

            return v;
        }

        // === USUÁRIO LOGADO ===
        String nome = TokenStorage.getNome(requireContext());
        String perfil = TokenStorage.getPerfil(requireContext());

        tvNome.setText(nome != null && !nome.isEmpty() ? nome : "Usuário");
        tvEmail.setText(perfil != null ? perfil : "ArthurBaby");

        // Busca dados atualizados no backend
        carregarPerfilBackend(tvNome, tvEmail);

        v.findViewById(R.id.opMeusPedidos).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new MeusPedidosFragment())
                        .addToBackStack(null)
                        .commit()
        );

        v.findViewById(R.id.opEnderecos).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new EnderecosFragment())
                        .addToBackStack(null)
                        .commit()
        );

        v.findViewById(R.id.opFavoritos).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new FavoritosFragment())
                        .addToBackStack(null)
                        .commit()
        );

        v.findViewById(R.id.opAjuda).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new AjudaFragment())
                        .addToBackStack(null)
                        .commit()
        );

        v.findViewById(R.id.opSobre).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new SobreFragment())
                        .addToBackStack(null)
                        .commit()
        );

        v.findViewById(R.id.opSair).setOnClickListener(x -> {
            TokenStorage.limpar(requireContext());
            FavoritoRepository.getInstance().limpar();

            Toast.makeText(requireContext(), "Até logo!", Toast.LENGTH_SHORT).show();

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameContainer, new HomeFragment())
                    .commit();
        });

        return v;
    }

    /**
     * Busca dados atualizados do usuário logado via GET /api/auth/me.
     * PROTEÇÃO: verifica se o Fragment ainda está vivo antes de usar requireContext().
     */
    private void carregarPerfilBackend(TextView tvNome, TextView tvEmail) {
        ApiService api = RetrofitClient.getApi(requireContext());
        api.buscarMeuPerfil().enqueue(new Callback<UsuarioResponse>() {
            @Override
            public void onResponse(Call<UsuarioResponse> call, Response<UsuarioResponse> response) {
                // 🔴 PROTEÇÃO: verifica se o Fragment ainda está anexado
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    UsuarioResponse u = response.body();

                    if (u.nomeCompleto != null && !u.nomeCompleto.isEmpty()) {
                        tvNome.setText(u.nomeCompleto);
                        TokenStorage.salvar(
                                getContext(),
                                TokenStorage.getToken(getContext()),
                                u.id, u.nomeCompleto, u.perfil);
                    }

                    if (u.email != null && !u.email.isEmpty()) {
                        tvEmail.setText(u.email);
                    }
                }
            }

            @Override
            public void onFailure(Call<UsuarioResponse> call, Throwable t) {
                // 🔴 PROTEÇÃO: verifica se o Fragment ainda está anexado
                if (!isAdded() || getContext() == null) return;
                // Silencioso — mantém os dados locais
            }
        });
    }

    private TextView encontrarTextView(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View filho = group.getChildAt(i);
            if (filho instanceof TextView) return (TextView) filho;
            if (filho instanceof ViewGroup) {
                TextView achado = encontrarTextView((ViewGroup) filho);
                if (achado != null) return achado;
            }
        }
        return null;
    }
}