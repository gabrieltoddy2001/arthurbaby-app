package br.com.arthurbaby.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import br.com.arthurbaby.fragments.ListaPedidosFragment;
import br.com.arthurbaby.models.Pedido;

public class PedidosPagerAdapter extends FragmentStateAdapter {

    private final List<Pedido> emAndamento;
    private final List<Pedido> finalizados;

    public PedidosPagerAdapter(@NonNull FragmentActivity activity,
                               List<Pedido> emAndamento,
                               List<Pedido> finalizados) {
        super(activity);
        this.emAndamento = emAndamento;
        this.finalizados = finalizados;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0
                ? ListaPedidosFragment.newInstance(emAndamento)
                : ListaPedidosFragment.newInstance(finalizados);
    }

    @Override
    public int getItemCount() { return 2; }
}