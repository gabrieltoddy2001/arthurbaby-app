# Guia de Integração — App Android + Backend Spring Boot

Passo a passo para integrar o app Android ao backend local.

---

## ✅ Pré-requisitos

- Backend rodando no IntelliJ (porta 8080)
- App Android aberto no Android Studio
- Emulador Android configurado (API 33+)
- **Os dois abertos ao mesmo tempo**

---

## 🎯 Como os dois se comunicam

```
[App Android no emulador] → http://10.0.2.2:8080/api/ → [Backend no PC]
```

- `10.0.2.2` = endereço especial do emulador que aponta para o `localhost` do PC
- `8080` = porta onde o backend roda
- `/api/` = prefixo das rotas do backend

---

## 📦 ETAPA 1 — Adicionar dependências Retrofit

Abra `app/build.gradle.kts` e adicione no bloco `dependencies`:

```kotlin
dependencies {
    // ... deps existentes ...

    // Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Para salvar token criptografado
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
```

Clique em **Sync Now** (barra amarela no topo).

---

## 📦 ETAPA 2 — Criar pasta `network`

Crie a pasta:
```
app/src/main/java/br/com/arthurbaby/network/
```

Dentro dela, crie as classes abaixo.

---

## 📦 ETAPA 3 — Criar `TokenStorage.java`

**Arquivo:** `network/TokenStorage.java`

```java
package br.com.arthurbaby.network;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStorage {

    private static final String PREF_NAME = "arthurbaby_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USUARIO_ID = "usuario_id";
    private static final String KEY_NOME = "nome";
    private static final String KEY_PERFIL = "perfil";

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void salvar(Context ctx, String token, Long usuarioId, String nome, String perfil) {
        prefs(ctx).edit()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_USUARIO_ID, usuarioId != null ? usuarioId : -1)
                .putString(KEY_NOME, nome)
                .putString(KEY_PERFIL, perfil)
                .apply();
    }

    public static String getToken(Context ctx) {
        return prefs(ctx).getString(KEY_TOKEN, null);
    }

    public static Long getUsuarioId(Context ctx) {
        long id = prefs(ctx).getLong(KEY_USUARIO_ID, -1);
        return id == -1 ? null : id;
    }

    public static String getNome(Context ctx) {
        return prefs(ctx).getString(KEY_NOME, null);
    }

    public static String getPerfil(Context ctx) {
        return prefs(ctx).getString(KEY_PERFIL, null);
    }

    public static void limpar(Context ctx) {
        prefs(ctx).edit().clear().apply();
    }
}
```

---

## 📦 ETAPA 4 — Criar `AuthInterceptor.java`

**Arquivo:** `network/AuthInterceptor.java`

```java
package br.com.arthurbaby.network;

import android.content.Context;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = TokenStorage.getToken(context);

        Request original = chain.request();
        Request.Builder builder = original.newBuilder()
                .header("Accept", "application/json");

        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        return chain.proceed(builder.build());
    }
}
```

---

## 📦 ETAPA 5 — Criar `ApiService.java`

**Arquivo:** `network/ApiService.java`

Contém **todos os endpoints** que o app vai chamar. Vou colocar os principais (depois você adiciona mais conforme precisa):

```java
package br.com.arthurbaby.network;

import br.com.arthurbaby.network.dto.AuthRequest;
import br.com.arthurbaby.network.dto.AuthResponse;
import br.com.arthurbaby.network.dto.CadastroRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("api/auth/cadastro")
    Call<AuthResponse> cadastrar(@Body CadastroRequest request);
}
```

---

## 📦 ETAPA 6 — Criar os DTOs de rede

Crie a pasta:
```
app/src/main/java/br/com/arthurbaby/network/dto/
```

**Arquivo:** `network/dto/AuthRequest.java`
```java
package br.com.arthurbaby.network.dto;

public class AuthRequest {
    public String login;
    public String senha;

    public AuthRequest(String login, String senha) {
        this.login = login;
        this.senha = senha;
    }
}
```

**Arquivo:** `network/dto/AuthResponse.java`
```java
package br.com.arthurbaby.network.dto;

public class AuthResponse {
    public String token;
    public Long usuarioId;
    public String nome;
    public String perfil;
}
```

**Arquivo:** `network/dto/CadastroRequest.java`
```java
package br.com.arthurbaby.network.dto;

public class CadastroRequest {
    public String nomeCompleto;
    public String email;
    public String cpf;
    public String telefone;
    public String senha;
    public boolean aceiteTermoUso;
    public boolean aceiteLgpd;
    // TODO: adicionar endereco quando o back implementar
}
```

---

## 📦 ETAPA 7 — Criar `RetrofitClient.java`

**Arquivo:** `network/RetrofitClient.java`

```java
package br.com.arthurbaby.network;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit;

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApi(Context context) {
        return getInstance(context).create(ApiService.class);
    }
}
```

---

## 📦 ETAPA 8 — Permitir HTTP puro no Android

Abra `AndroidManifest.xml` e adicione **antes** do `<application>`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

E **dentro** da tag `<application>`, adicione o atributo:

```xml
<application
    android:usesCleartextTraffic="true"
    ... >
```

> ⚠️ Isso libera HTTP sem HTTPS — **só para desenvolvimento**. Em produção, usar HTTPS.

---

## 📦 ETAPA 9 — Fazer o Login real

Abra `activities/LoginActivity.java` e substitua o método `validarLogin()` por:

```java
private void validarLogin() {
    String login = etLogin.getText() != null ? etLogin.getText().toString().trim() : "";
    String senha = etSenha.getText() != null ? etSenha.getText().toString().trim() : "";

    if (TextUtils.isEmpty(login)) {
        etLogin.setError("Informe e-mail ou CPF");
        return;
    }
    if (TextUtils.isEmpty(senha) || senha.length() < 4) {
        etSenha.setError("Senha deve ter ao menos 4 caracteres");
        return;
    }

    br.com.arthurbaby.utils.LoadingUtils.mostrar(this);

    br.com.arthurbaby.network.ApiService api =
            br.com.arthurbaby.network.RetrofitClient.getApi(this);

    api.login(new br.com.arthurbaby.network.dto.AuthRequest(login, senha))
            .enqueue(new retrofit2.Callback<br.com.arthurbaby.network.dto.AuthResponse>() {
                @Override
                public void onResponse(retrofit2.Call<br.com.arthurbaby.network.dto.AuthResponse> call,
                                       retrofit2.Response<br.com.arthurbaby.network.dto.AuthResponse> response) {
                    br.com.arthurbaby.utils.LoadingUtils.esconder();

                    if (response.isSuccessful() && response.body() != null) {
                        br.com.arthurbaby.network.dto.AuthResponse auth = response.body();

                        br.com.arthurbaby.network.TokenStorage.salvar(
                                LoginActivity.this,
                                auth.token, auth.usuarioId, auth.nome, auth.perfil);

                        Toast.makeText(LoginActivity.this,
                                "Bem-vindo, " + auth.nome + "!",
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Login inválido. Verifique seus dados.",
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<br.com.arthurbaby.network.dto.AuthResponse> call,
                                      Throwable t) {
                    br.com.arthurbaby.utils.LoadingUtils.esconder();
                    Toast.makeText(LoginActivity.this,
                            "Erro de conexão: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
}
```

---

## ▶️ ETAPA 10 — Testar

1. **Rode o backend no IntelliJ** (aguarde "Tomcat started on port 8080")
2. **Rode o app no emulador** (Shift+F10)
3. Na tela de Login, use:
  - **E-mail:** `cliente@teste.com`
  - **Senha:** `cliente123`
4. Clique em **ENTRAR**

### ✅ Se funcionar
- Toast "Bem-vindo, Cliente Teste!"
- Entra na Home
- No **Logcat** (Android Studio) você vê a requisição HTTP e a resposta

### ❌ Se não funcionar
- Verifique se o backend **ainda está rodando** no IntelliJ
- Verifique se o emulador tem **internet** (tente abrir o Chrome no emulador)
- No Logcat, procure por **"Failed to connect"** ou **"Connection refused"**

---

## 📋 Próximos passos (depois do login funcionar)

Faça na ordem:

1. **Cadastro** — substituir a chamada mock por `api.cadastrar(...)`
2. **Categorias** — `GET /api/categorias`
3. **Produtos** — `GET /api/produtos`
4. **Detalhe do produto** — `GET /api/produtos/{id}`
5. **Criar pedido** — `POST /api/pedidos`
6. **Listar pedidos** — `GET /api/pedidos/cliente/{id}`
7. **Favoritos** — `GET/POST/DELETE /api/favoritos`

**Cada item segue o mesmo padrão:**
- Adicionar método no `ApiService`
- Criar DTOs de request/response
- Chamar no fragment/activity
- Tratar erro

---

## 🆘 Erros comuns

| Erro | Causa | Solução |
|---|---|---|
| **"Connection refused"** | Backend desligado | Rodar de novo no IntelliJ |
| **"Cleartext HTTP not permitted"** | Falta `usesCleartextTraffic` | Adicionar no Manifest |
| **"401 Unauthorized"** | Token não enviado | Verificar `AuthInterceptor` |
| **"timeout"** | Backend travado | Reiniciar o backend |
| **"Expected BEGIN_OBJECT but was..."** | DTOs não batem | Conferir campos |
| **"Java version not compatible"** | JDK no Android Studio | **File → Project Structure → JDK 17** |

---

## 🎯 Dica de ouro

**Antes de mudar o código do app**, teste o endpoint **no Swagger** primeiro. Se funcionar no Swagger, o problema é no app. Se não funcionar no Swagger, o problema é no back.

---

*Guia gerado em 18/09/2025 — integração do app Android com backend Spring Boot local.*