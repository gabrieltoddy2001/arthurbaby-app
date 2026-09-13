package br.com.arthurbaby.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class MaskUtils {

    public static void aplicarMascaraCpf(EditText et) {
        et.addTextChangedListener(new TextWatcher() {
            boolean atualizando = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (atualizando) return;
                atualizando = true;
                String v = s.toString().replaceAll("\\D", "");
                if (v.length() > 11) v = v.substring(0, 11);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < v.length(); i++) {
                    if (i == 3 || i == 6) sb.append('.');
                    if (i == 9) sb.append('-');
                    sb.append(v.charAt(i));
                }
                et.setText(sb.toString());
                et.setSelection(sb.length());
                atualizando = false;
            }
        });
    }

    public static void aplicarMascaraTelefone(EditText et) {
        et.addTextChangedListener(new TextWatcher() {
            boolean atualizando = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (atualizando) return;
                atualizando = true;
                String v = s.toString().replaceAll("\\D", "");
                if (v.length() > 11) v = v.substring(0, 11);
                StringBuilder sb = new StringBuilder();
                if (v.length() > 0) sb.append("(");
                for (int i = 0; i < v.length(); i++) {
                    if (i == 2) sb.append(") ");
                    if (i == 7) sb.append("-");
                    sb.append(v.charAt(i));
                }
                et.setText(sb.toString());
                et.setSelection(sb.length());
                atualizando = false;
            }
        });
    }

    public static void aplicarMascaraCep(EditText et) {
        et.addTextChangedListener(new TextWatcher() {
            boolean atualizando = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (atualizando) return;
                atualizando = true;
                String v = s.toString().replaceAll("\\D", "");
                if (v.length() > 8) v = v.substring(0, 8);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < v.length(); i++) {
                    if (i == 5) sb.append("-");
                    sb.append(v.charAt(i));
                }
                et.setText(sb.toString());
                et.setSelection(sb.length());
                atualizando = false;
            }
        });
    }
    /**
     * Valida os dígitos verificadores do CPF.
     * Retorna true se for válido.
     */
    public static boolean validarCpf(String cpf) {
        cpf = cpf.replaceAll("\\D", "");
        if (cpf.length() != 11) return false;
        if (cpf.matches("(\\d)\\1{10}")) return false; // todos iguais

        try {
            int soma = 0;
            for (int i = 0; i < 9; i++) soma += (cpf.charAt(i) - '0') * (10 - i);
            int d1 = 11 - (soma % 11);
            if (d1 >= 10) d1 = 0;

            soma = 0;
            for (int i = 0; i < 10; i++) soma += (cpf.charAt(i) - '0') * (11 - i);
            int d2 = 11 - (soma % 11);
            if (d2 >= 10) d2 = 0;

            return (cpf.charAt(9) - '0') == d1 && (cpf.charAt(10) - '0') == d2;
        } catch (Exception e) {
            return false;
        }
    }
}