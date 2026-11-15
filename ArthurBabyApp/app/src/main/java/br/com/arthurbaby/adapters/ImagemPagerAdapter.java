package br.com.arthurbaby.adapters;

import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import br.com.arthurbaby.R;

public class ImagemPagerAdapter extends RecyclerView.Adapter<ImagemPagerAdapter.VH> {

    private final List<String> urls;

    public ImagemPagerAdapter(List<String> urls) {
        this.urls = urls;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView iv = new ImageView(parent.getContext());
        iv.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setContentDescription("Imagem do produto");
        return new VH(iv);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Glide.with(h.iv.getContext())
                .load(urls.get(position))
                .placeholder(R.drawable.bg_imagem_produto)
                .error(R.drawable.bg_imagem_produto)
                .fitCenter()
                .into(h.iv);
    }

    @Override
    public int getItemCount() { return urls.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView iv;
        VH(ImageView v) { super(v); iv = v; }
    }
}