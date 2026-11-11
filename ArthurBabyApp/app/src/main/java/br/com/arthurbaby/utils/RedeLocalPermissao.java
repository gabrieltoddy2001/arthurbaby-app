package br.com.arthurbaby.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import br.com.arthurbaby.network.RetrofitClient;

/**
 * Android 17 (API 37): apps com targetSdk 37 precisam da permissão ACCESS_LOCAL_NETWORK
 * para acessar endereços da rede local (10.x, 192.168.x, 172.16-31.x). Sem ela o sistema
 * descarta as conexões em silêncio e o login dá timeout ("O servidor demorou para responder").
 * Em desenvolvimento a API roda no PC (10.0.2.2 no emulador, IP do Wi-Fi no celular),
 * então pedimos a permissão só quando a URL da API é de rede local.
 */
public final class RedeLocalPermissao {

    private static final String PERMISSAO = "android.permission.ACCESS_LOCAL_NETWORK";
    public static final int REQUEST_CODE = 4370;

    private RedeLocalPermissao() {}

    /** true quando a API está na rede local e a permissão ainda não foi concedida. */
    public static boolean precisaPedir(Activity activity) {
        if (Build.VERSION.SDK_INT < 37) return false;
        if (!apiEmRedeLocal()) return false;
        return activity.checkSelfPermission(PERMISSAO) != PackageManager.PERMISSION_GRANTED;
    }

    /** Pede a permissão; a resposta chega em onRequestPermissionsResult com {@link #REQUEST_CODE}. */
    public static void solicitarSeNecessario(Activity activity) {
        if (!precisaPedir(activity)) return;
        activity.requestPermissions(new String[]{PERMISSAO}, REQUEST_CODE);
    }

    /** true se o resultado recebido concedeu a permissão de rede local. */
    public static boolean foiConcedida(int requestCode, int[] resultados) {
        return requestCode == REQUEST_CODE && resultados.length > 0
                && resultados[0] == PackageManager.PERMISSION_GRANTED;
    }

    static boolean apiEmRedeLocal() {
        String host = Uri.parse(RetrofitClient.getBaseUrl()).getHost();
        return ehHostLocal(host);
    }

    static boolean ehHostLocal(String host) {
        if (host == null) return false;
        if (host.equals("localhost") || host.endsWith(".local")) return true;
        if (host.startsWith("10.") || host.startsWith("192.168.")) return true;
        if (host.startsWith("172.")) {
            String[] partes = host.split("\\.");
            if (partes.length > 1) {
                try {
                    int segundo = Integer.parseInt(partes[1]);
                    return segundo >= 16 && segundo <= 31;
                } catch (NumberFormatException ignored) {
                    return false;
                }
            }
        }
        return false;
    }
}
