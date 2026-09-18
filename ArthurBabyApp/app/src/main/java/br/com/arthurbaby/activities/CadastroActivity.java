package br.com.arthurbaby.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import br.com.arthurbaby.MainActivity;
import br.com.arthurbaby.R;
import br.com.arthurbaby.utils.LoadingUtils;
import br.com.arthurbaby.utils.MaskUtils;
import br.com.arthurbaby.utils.ViaCepService;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText etNome, etCpf, etEmail, etTelefone, etSenha;
    private TextInputEditText etCep, etLogradouro, etNumero, etComplemento,
            etBairro, etCidade, etUf;
    private CheckBox cbTermos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        // Campos pessoais
        etNome = findViewById(R.id.etNome);
        etCpf = findViewById(R.id.etCpf);
        etEmail = findViewById(R.id.etEmail);
        etTelefone = findViewById(R.id.etTelefone);
        etSenha = findViewById(R.id.etSenhaCad);

        // Endereço
        etCep = findViewById(R.id.etCep);
        etLogradouro = findViewById(R.id.etLogradouro);
        etNumero = findViewById(R.id.etNumero);
        etComplemento = findViewById(R.id.etComplemento);
        etBairro = findViewById(R.id.etBairro);
        etCidade = findViewById(R.id.etCidade);
        etUf = findViewById(R.id.etUf);

        // Termos e botão
        cbTermos = findViewById(R.id.cbTermos);
        MaterialButton btnCadastrar = findViewById(R.id.btnCadastrar);

        // Aplica máscaras
        MaskUtils.aplicarMascaraCpf(etCpf);
        MaskUtils.aplicarMascaraTelefone(etTelefone);
        MaskUtils.aplicarMascaraCep(etCep);

        // Busca automática de CEP quando o usuário terminar de digitar
        etCep.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String cep = s.toString().replaceAll("\\D", "");
                if (cep.length() == 8) {
                    buscarCep(cep);
                }
            }
        });

        btnCadastrar.setOnClickListener(v -> cadastrar());
    }

    /**
     * Busca o endereço no ViaCEP e preenche os campos.
     */
    private void buscarCep(String cep) {
        ViaCepService.buscar(cep, new ViaCepService.Callback() {
            @Override
            public void onResultado(ViaCepService.EnderecoCep e) {
                if (!e.logradouro.isEmpty()) etLogradouro.setText(e.logradouro);
                if (!e.bairro.isEmpty()) etBairro.setText(e.bairro);
                if (!e.cidade.isEmpty()) etCidade.setText(e.cidade);
                if (!e.uf.isEmpty()) etUf.setText(e.uf);
                etNumero.requestFocus();
            }

            @Override
            public void onErro(String mensagem) {
                Toast.makeText(CadastroActivity.this, mensagem, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Valida os campos e simula o cadastro.
     */
    private void cadastrar() {
        String nome = texto(etNome);
        String cpf = texto(etCpf);
        String email = texto(etEmail);
        String telefone = texto(etTelefone);
        String senha = texto(etSenha);
        String cep = texto(etCep);
        String logradouro = texto(etLogradouro);
        String numero = texto(etNumero);
        String complemento = texto(etComplemento);
        String bairro = texto(etBairro);
        String cidade = texto(etCidade);
        String uf = texto(etUf);

        // Validações
        if (TextUtils.isEmpty(nome)) {
            etNome.setError("Informe seu nome");
            return;
        }
        if (TextUtils.isEmpty(cpf)) {
            etCpf.setError("Informe seu CPF");
            return;
        }
        if (!MaskUtils.validarCpf(cpf)) {
            etCpf.setError("CPF inválido");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Informe seu e-mail");
            return;
        }
        if (TextUtils.isEmpty(telefone)) {
            etTelefone.setError("Informe seu telefone");
            return;
        }
        if (TextUtils.isEmpty(senha) || senha.length() < 4) {
            etSenha.setError("Senha deve ter ao menos 4 caracteres");
            return;
        }
        if (TextUtils.isEmpty(cep)) {
            etCep.setError("Informe seu CEP");
            return;
        }
        if (TextUtils.isEmpty(logradouro)) {
            etLogradouro.setError("Informe o logradouro");
            return;
        }
        if (TextUtils.isEmpty(numero)) {
            etNumero.setError("Informe o número");
            return;
        }
        if (TextUtils.isEmpty(bairro)) {
            etBairro.setError("Informe o bairro");
            return;
        }
        if (TextUtils.isEmpty(cidade)) {
            etCidade.setError("Informe a cidade");
            return;
        }
        if (TextUtils.isEmpty(uf)) {
            etUf.setError("Informe a UF");
            return;
        }
        if (!cbTermos.isChecked()) {
            Toast.makeText(this, "Aceite os termos para continuar", Toast.LENGTH_SHORT).show();
            return;
        }

        // MOCK: aqui entrará a chamada POST /api/auth/cadastro
        LoadingUtils.mostrar(this);

        new android.os.Handler().postDelayed(() -> {
            LoadingUtils.esconder();

            // Pega o primeiro nome para saudação
            String primeiroNome = nome.split(" ")[0];

            Toast.makeText(this,
                    "Bem-vindo(a), " + primeiroNome + "!",
                    Toast.LENGTH_SHORT).show();

            // Já entra direto na MainActivity (simula login automático)
            Intent i = new Intent(CadastroActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();

        }, 900);
    }

    /**
     * Helper: pega o texto de um TextInputEditText sem risco de NPE.
     */
    private String texto(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}