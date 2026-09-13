package br.com.arthurbaby.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import br.com.arthurbaby.R;
import br.com.arthurbaby.adapters.ProdutoAdapter;
import br.com.arthurbaby.repositories.FavoritoRepository;

public class FavoritosFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_favoritos, container, false);

        v.findViewById(R.id.btnVoltar).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        RecyclerView rv = v.findViewById(R.id.rvFavoritos);
        TextView tvVazio = v.findViewById(R.id.tvVazio);

        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));

        java.util.List<br.com.arthurbaby.models.Produto> favoritos =
                FavoritoRepository.getInstance().getFavoritos();

        if (favoritos.isEmpty()) {
            tvVazio.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            tvVazio.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
            rv.setAdapter(new ProdutoAdapter(favoritos, produto -> {
                DetalheProdutoFragment frag = DetalheProdutoFragment.newInstance(produto);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, frag)
                        .addToBackStack(null)
                        .commit();
            }));
        }

        return v;
    }
}