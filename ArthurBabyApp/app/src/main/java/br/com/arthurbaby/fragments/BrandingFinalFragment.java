package br.com.arthurbaby.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import br.com.arthurbaby.R;

public class BrandingFinalFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_branding_final, container, false);

        v.findViewById(R.id.btnInstagram).setOnClickListener(x ->
                abrirUrl("https://instagram.com/lojao_arthur_baby"));

        v.findViewById(R.id.btnWhatsapp).setOnClickListener(x ->
                abrirUrl("https://wa.me/5571991311944"));

        v.findViewById(R.id.btnShopee).setOnClickListener(x ->
                abrirUrl("https://shopee.com.br/arthurbabylojao"));

        v.findViewById(R.id.btnVoltarInicio).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameContainer, new HomeFragment())
                        .commit());

        return v;
    }

    private void abrirUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception ignored) {}
    }
}