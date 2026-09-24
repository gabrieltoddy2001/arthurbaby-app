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
import br.com.arthurbaby.network.ApiService;
import br.com.arthurbaby.network.RetrofitClient;
import br.com.arthurbaby.network.TokenStorage;
import br.com.arthurbaby.network.dto.AuthResponse;
import br.com.arthurbaby.network.dto.CadastroRequest;
import br.com.arthurbaby.network.dto.EnderecoRequest;
import br.com.arthurbaby.utils.LoadingUtils;
import br.com.arthurbaby.utils.MaskUtils;
import br.com.arthurbaby.utils.ViaCepService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        // Máscaras
        MaskUtils.aplicarMascaraCpf(etCpf);
        MaskUtils.aplicarMascaraTelefone(etTelefone);
        MaskUtils.aplicarMascaraCep(etCep);

        // Busca automática de CEP
        etCep.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String cep = s.toString().replaceAll("\\D", "");
                if (cep.length() == 8) buscarCep(cep);
            }
        });

        btnCadastrar.setOnClickListener(v -> cadastrar());
    }

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

        // Validações locais
        if (TextUtils.isEmpty(nome)) { etNome.setError("Informe seu nome"); return; }
        if (TextUtils.isEmpty(cpf)) { etCpf.setError("Informe seu CPF"); return; }
        if (!MaskUtils.validarCpf(cpf)) { etCpf.setError("CPF inválido"); return; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("Informe seu e-mail"); return; }
        if (TextUtils.isEmpty(telefone)) { etTelefone.setError("Informe seu telefone"); return; }
        if (TextUtils.isEmpty(senha) || senha.length() < 4) {
            etSenha.setError("Senha deve ter ao menos 4 caracteres"); return;
        }
        if (TextUtils.isEmpty(cep)) { etCep.setError("Informe seu CEP"); return; }
        if (TextUtils.isEmpty(logradouro)) { etLogradouro.setError("Informe o logradouro"); return; }
        if (TextUtils.isEmpty(numero)) { etNumero.setError("Informe o número"); return; }
        if (TextUtils.isEmpty(bairro)) { etBairro.setError("Informe o bairro"); return; }
        if (TextUtils.isEmpty(cidade)) { etCidade.setError("Informe a cidade"); return; }
        if (TextUtils.isEmpty(uf)) { etUf.setError("Informe a UF"); return; }
        if (!cbTermos.isChecked()) {
            Toast.makeText(this, "Aceite os termos para continuar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Monta o DTO
        EnderecoRequest endereco = new EnderecoRequest(
                cep, logradouro, numero, complemento, bairro, cidade, uf,
                null,  // referência (opcional)
                true   // principal
        );

        CadastroRequest request = new CadastroRequest(
                nome, email, cpf, telefone, senha,
                true, true,
                endereco
        );

        // Chamada real
        LoadingUtils.mostrar(this);

        ApiService api = RetrofitClient.getApi(this);
        api.cadastrar(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                LoadingUtils.esconder();

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();

                    // Salva o token (login automático)
                    TokenStorage.salvar(
                            CadastroActivity.this,
                            auth.token,
                            auth.usuarioId,
                            auth.nome,
                            auth.perfil
                    );

                    Toast.makeText(CadastroActivity.this,
                            "Bem-vindo, " + auth.nome + "!",
                            Toast.LENGTH_SHORT).show();

                    Intent i = new Intent(CadastroActivity.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                } else {
                    // Erro do backend — tenta ler a mensagem
                    String msg = "Erro ao cadastrar";
                    try {
                        if (response.errorBody() != null) {
                            String erroJson = response.errorBody().string();
                            // Extrai o campo "erro" do JSON
                            if (erroJson.contains("\"erro\"")) {
                                int inicio = erroJson.indexOf("\"erro\"") + 8;
                                int fim = erroJson.indexOf("\"", inicio);
                                msg = erroJson.substring(inicio, fim);
                            }
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(CadastroActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                LoadingUtils.esconder();
                Toast.makeText(CadastroActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private String texto(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}