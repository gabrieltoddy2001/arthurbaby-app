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

    /** Gera ids novos a cada adapter, para que um novo filtro recrie as listas em vez de reaproveitar as antigas. */
    private static long geracao = 0;
    private final long base;

    public PedidosPagerAdapter(@NonNull FragmentActivity activity,
                               List<Pedido> emAndamento,
                               List<Pedido> finalizados) {
        super(activity);
        this.emAndamento = emAndamento;
        this.finalizados = finalizados;
        this.base = (++geracao) * 10;
    }

    public PedidosPagerAdapter(@NonNull Fragment host,
                               List<Pedido> emAndamento,
                               List<Pedido> finalizados) {
        super(host);
        this.emAndamento = emAndamento;
        this.finalizados = finalizados;
        this.base = (++geracao) * 10;
    }

    @Override
    public long getItemId(int position) { return base + position; }

    @Override
    public boolean containsItem(long itemId) { return itemId == base || itemId == base + 1; }

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