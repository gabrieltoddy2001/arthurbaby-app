package br.com.arthurbaby.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import br.com.arthurbaby.R;

public class SobreFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sobre, container, false);

        v.findViewById(R.id.btnVoltar).setOnClickListener(x ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        // WhatsApp
        v.findViewById(R.id.tvWhatsapp).setOnClickListener(x ->
                abrirUrl("https://wa.me/5571991311944")
        );

        // E-mail
        v.findViewById(R.id.tvEmail).setOnClickListener(x ->
                abrirUrl("mailto:pedidoababy@gmail.com")
        );

        // Instagram
        v.findViewById(R.id.tvInstagram).setOnClickListener(x ->
                abrirUrl("https://instagram.com/lojao_arthur_baby")
        );

        // Shopee
        v.findViewById(R.id.tvShopee).setOnClickListener(x ->
                abrirUrl("https://shopee.com.br/arthurbabylojao")
        );

        // Facebook
        v.findViewById(R.id.tvFacebook).setOnClickListener(x ->
                abrirUrl("https://www.facebook.com/p/loj%C3%A3o-Arthur-baby-61590691140950/")
        );

        // Endereço → Google Maps
        v.findViewById(R.id.tvEndereco).setOnClickListener(x ->
                abrirUrl("https://maps.google.com/?q=Avenida+Sete+de+Setembro+548+Salvador+BA")
        );

        return v;
    }

    private void abrirUrl(String url) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        } catch (Exception e) {
            // ignora
        }
    }
}
