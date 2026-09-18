package br.com.arthurbaby.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import br.com.arthurbaby.R;
import br.com.arthurbaby.adapters.EnderecoAdapter;
import br.com.arthurbaby.repositories.EnderecoRepository;

public class EnderecosFragment extends Fragment {

    private RecyclerView rv;
    private EnderecoAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_enderecos, container, false);

        v.findViewById(R.id.btnVoltar).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        rv = v.findViewById(R.id.rvEnderecos);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EnderecoAdapter(EnderecoRepository.getInstance().getEnderecos(), e -> {
            EnderecoRepository.getInstance().remover(e);
            adapter.notifyDataSetChanged();
            Toast.makeText(requireContext(), "Endereço removido", Toast.LENGTH_SHORT).show();
        });
        rv.setAdapter(adapter);

        v.findViewById(R.id.btnNovoEndereco).setOnClickListener(x ->
                Toast.makeText(requireContext(), "Adicionar endereço (em breve)", Toast.LENGTH_SHORT).show()
        );

        return v;
    }
}