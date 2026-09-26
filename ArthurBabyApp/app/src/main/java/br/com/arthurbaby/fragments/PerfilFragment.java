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
import br.com.arthurbaby.network.TokenStorage;
import br.com.arthurbaby.repositories.FavoritoRepository;
import br.com.arthurbaby.utils.AuthGuard;

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

            // Cada opção pede login
            v.findViewById(R.id.opMeusPedidos).setOnClickListener(x ->
                    AuthGuard.mostrarDialogLogin(requireActivity(),
                            "Entre para ver seus pedidos."));

            v.findViewById(R.id.opEnderecos).setOnClickListener(x ->
                    AuthGuard.mostrarDialogLogin(requireActivity(),
                            "Entre para gerenciar endereços."));

            v.findViewById(R.id.opFavoritos).setOnClickListener(x ->
                    AuthGuard.mostrarDialogLogin(requireActivity(),
                            "Entre para ver seus favoritos."));

            // Ajuda e Sobre continuam funcionando
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

            // "Sair" vira "Entrar" (defensivo)
            try {
                LinearLayout opSair = v.findViewById(R.id.opSair);
                if (opSair != null) {
                    // Procura um TextView dentro do opSair
                    TextView tvSair = encontrarTextView(opSair);
                    if (tvSair != null) {
                        tvSair.setText("Entrar / Cadastrar");
                    }

                    opSair.setOnClickListener(x ->
                            startActivity(new Intent(getActivity(), LoginActivity.class)));
                }
            } catch (Exception e) {
                // Se falhar, não quebra
                android.util.Log.e("PERFIL_DEBUG", "Erro no opSair", e);
            }

            return v;
        }

        // === USUÁRIO LOGADO ===
        String nome = TokenStorage.getNome(requireContext());
        String perfil = TokenStorage.getPerfil(requireContext());

        tvNome.setText(nome != null && !nome.isEmpty() ? nome : "Usuário");
        tvEmail.setText(perfil != null ? perfil : "ArthurBaby");

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

            // Volta para a Home
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameContainer, new HomeFragment())
                    .commit();
        });

        return v;
    }

    /**
     * Procura recursivamente por um TextView dentro de um ViewGroup.
     * Retorna null se não encontrar.
     */
    private TextView encontrarTextView(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View filho = group.getChildAt(i);
            if (filho instanceof TextView) {
                return (TextView) filho;
            }
            if (filho instanceof ViewGroup) {
                TextView achado = encontrarTextView((ViewGroup) filho);
                if (achado != null) return achado;
            }
        }
        return null;
    }
}