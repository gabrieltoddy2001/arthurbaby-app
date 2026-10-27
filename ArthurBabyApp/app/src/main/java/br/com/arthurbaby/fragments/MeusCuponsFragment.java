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
import java.util.List;
import br.com.arthurbaby.R;
import br.com.arthurbaby.adapters.CupomAdapter;
import br.com.arthurbaby.network.ApiService;
import br.com.arthurbaby.network.RetrofitClient;
import br.com.arthurbaby.network.dto.CupomResponse;
import br.com.arthurbaby.utils.ErrorUtils;
import br.com.arthurbaby.utils.LoadingView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeusCuponsFragment extends Fragment {
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_meus_cupons, container, false);
        v.findViewById(R.id.btnVoltar).setOnClickListener(x -> requireActivity().getSupportFragmentManager().popBackStack());

        RecyclerView rv = v.findViewById(R.id.rvCupons);
        View progress = v.findViewById(R.id.progressBar);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        LoadingView.mostrar(progress, rv);
        ApiService api = RetrofitClient.getApi(requireContext());
        api.listarCuponsDisponiveis().enqueue(new Callback<List<CupomResponse>>() {
            @Override public void onResponse(Call<List<CupomResponse>> call, Response<List<CupomResponse>> response) {
                LoadingView.esconder(progress, rv);
                if (response.isSuccessful() && response.body() != null) {
                    rv.setAdapter(new CupomAdapter(response.body()));
                } else {
                    Toast.makeText(getContext(), ErrorUtils.extrairMensagem(response), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<List<CupomResponse>> call, Throwable t) {
                LoadingView.esconder(progress, rv);
                Toast.makeText(getContext(), "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        return v;
    }
}