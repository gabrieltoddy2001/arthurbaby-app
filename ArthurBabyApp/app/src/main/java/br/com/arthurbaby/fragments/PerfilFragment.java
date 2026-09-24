package br.com.arthurbaby.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import br.com.arthurbaby.R;
import br.com.arthurbaby.activities.LoginActivity;
import br.com.arthurbaby.network.TokenStorage;
import br.com.arthurbaby.repositories.FavoritoRepository;

public class PerfilFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_perfil, container, false);

        TextView tvNome = v.findViewById(R.id.tvNome);
        TextView tvEmail = v.findViewById(R.id.tvEmail);

        // Dados reais do usuário logado
        String nome = TokenStorage.getNome(requireContext());
        String perfil = TokenStorage.getPerfil(requireContext());

        if (nome != null && !nome.isEmpty()) {
            tvNome.setText(nome);
        } else {
            tvNome.setText("Visitante");
        }

        // O back não retorna o email no login — usamos o perfil como subtítulo
        if (perfil != null) {
            tvEmail.setText(perfil);
        } else {
            tvEmail.setText("ArthurBaby");
        }

        // Meus Pedidos
        v.findViewById(R.id.opMeusPedidos).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new MeusPedidosFragment())
                        .addToBackStack(null)
                        .commit()
        );

        // Endereços
        v.findViewById(R.id.opEnderecos).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new EnderecosFragment())
                        .addToBackStack(null)
                        .commit()
        );

        // Favoritos
        v.findViewById(R.id.opFavoritos).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new FavoritosFragment())
                        .addToBackStack(null)
                        .commit()
        );

        // Ajuda
        v.findViewById(R.id.opAjuda).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new AjudaFragment())
                        .addToBackStack(null)
                        .commit()
        );

        // Sobre
        v.findViewById(R.id.opSobre).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new SobreFragment())
                        .addToBackStack(null)
                        .commit()
        );

        // Sair — limpa tudo
        v.findViewById(R.id.opSair).setOnClickListener(x -> {
            TokenStorage.limpar(requireContext());
            FavoritoRepository.getInstance().limpar();

            Toast.makeText(requireContext(), "Até logo!", Toast.LENGTH_SHORT).show();

            Intent i = new Intent(getActivity(), LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);

            if (getActivity() != null) getActivity().finish();
        });

        return v;
    }
}