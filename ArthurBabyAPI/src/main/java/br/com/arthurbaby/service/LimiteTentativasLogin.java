package br.com.arthurbaby.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proteção contra tentativas de senha em massa no login (força bruta).
 *
 * <ul>
 *   <li>Por conta (e-mail/CPF digitado): 5 erros dentro de 15 minutos bloqueiam aquela conta por 15 minutos.</li>
 *   <li>Por endereço IP: 20 erros dentro de 15 minutos bloqueiam o IP por 15 minutos (impede testar uma
 *       senha comum em muitas contas).</li>
 *   <li>Login certo zera o contador da conta.</li>
 * </ul>
 * O bloqueio é temporário de propósito: um atacante não consegue travar a conta de alguém para sempre.
 * Fica em memória (reinicia junto com a API), o suficiente para uma única instância do servidor.
 * Bloqueado: HTTP 429 com {"erro": "Muitas tentativas..."}, mensagem que o app e o painel exibem.
 */
@Component
public class LimiteTentativasLogin {
    static final int MAX_ERROS_CONTA = 5;
    static final int MAX_ERROS_IP = 20;
    static final Duration JANELA = Duration.ofMinutes(15);
    static final Duration BLOQUEIO = Duration.ofMinutes(15);

    private static final class Contador {
        int erros;
        Instant inicio;
        Instant bloqueadoAte;
    }

    private final Map<String, Contador> contadores = new ConcurrentHashMap<>();

    /** Chamado antes de conferir a senha: recusa (429) se a conta ou o IP estiverem bloqueados. */
    public void verificar(String login, String ip) {
        Instant agora = Instant.now();
        limparAntigos(agora);
        for (String chave : new String[]{chaveConta(login), chaveIp(ip)}) {
            Contador c = chave == null ? null : contadores.get(chave);
            if (c != null && c.bloqueadoAte != null && agora.isBefore(c.bloqueadoAte)) {
                long minutos = Math.max(1, (Duration.between(agora, c.bloqueadoAte).toSeconds() + 59) / 60);
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Muitas tentativas de login. Tente novamente em " + minutos + (minutos == 1 ? " minuto." : " minutos."));
            }
        }
    }

    public void registrarFalha(String login, String ip) {
        Instant agora = Instant.now();
        contar(chaveConta(login), MAX_ERROS_CONTA, agora);
        contar(chaveIp(ip), MAX_ERROS_IP, agora);
    }

    public void registrarSucesso(String login) {
        String chave = chaveConta(login);
        if (chave != null) contadores.remove(chave);
    }

    private void contar(String chave, int maximo, Instant agora) {
        if (chave == null) return;
        contadores.compute(chave, (k, c) -> {
            if (c == null || (c.bloqueadoAte == null && c.inicio.plus(JANELA).isBefore(agora))
                    || (c.bloqueadoAte != null && !agora.isBefore(c.bloqueadoAte))) {
                c = new Contador();
                c.inicio = agora;
            }
            c.erros++;
            if (c.erros >= maximo) c.bloqueadoAte = agora.plus(BLOQUEIO);
            return c;
        });
    }

    /** Evita crescer sem limite: descarta contadores vencidos quando o mapa fica grande. */
    private void limparAntigos(Instant agora) {
        if (contadores.size() < 5_000) return;
        contadores.entrySet().removeIf(e -> {
            Contador c = e.getValue();
            Instant fim = c.bloqueadoAte != null ? c.bloqueadoAte : c.inicio.plus(JANELA);
            return fim.isBefore(agora);
        });
    }

    private static String chaveConta(String login) {
        if (login == null || login.isBlank()) return null;
        String l = login.trim().toLowerCase(Locale.ROOT);
        // CPF digitado com ou sem pontuação conta como a mesma conta
        if (!l.contains("@")) l = l.replaceAll("\\D", "");
        return l.isEmpty() ? null : "conta:" + l;
    }

    private static String chaveIp(String ip) {
        return ip == null || ip.isBlank() ? null : "ip:" + ip;
    }
}
