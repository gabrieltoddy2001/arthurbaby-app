# ArthurBabyApp — Aplicativo Android

Aplicativo da cliente da **ARTHURBABY**: catálogo, carrinho, cupons, pedidos e perfil.
Feito em Java no Android Studio (minSdk 24) e integrado à API do projeto (`ArthurBabyAPI`).

## Endereço da API

Por padrão o app usa a API publicada no Render: `https://arthurbaby-api.onrender.com/`.

Para apontar para a API rodando no seu computador, crie ou edite `local.properties` (não vai para o Git):

```properties
# Emulador: o localhost do PC é 10.0.2.2
api.url=http://10.0.2.2:8080/
# Celular na mesma rede Wi-Fi: use o IP do PC
# api.url=http://192.168.0.10:8080/
```

Depois é só sincronizar o Gradle e rodar.

## Gerar o APK

```powershell
./gradlew assembleDebug
```

O arquivo sai em `app/build/outputs/apk/debug/app-debug.apk`.

## Testes

```powershell
./gradlew testDebugUnitTest
```

## Estrutura

```
activities/     telas cheias: splash, login, cadastro, recuperar/redefinir senha, perfil
fragments/      telas da navegação: início, categorias, busca, carrinho, perfil, pedidos...
adapters/       listas (produtos, categorias, carrinho, pedidos, cupons, endereços)
network/        Retrofit (ApiService, RetrofitClient), DTOs e conversão para os modelos
repositories/   carrinho (salvo no aparelho) e favoritos (sincronizados com a API)
utils/          barras do sistema, login obrigatório, moeda, avaliação, máscaras
res/values/     cores, textos (strings.xml) e tamanhos de texto (dimens.xml)
```

## Padrões visuais

- Tamanhos de texto: use os de `res/values/dimens.xml` (`texto_mini` a `texto_display`), não valores soltos em sp.
- Textos de tela ficam em `res/values/strings.xml`.
- Cabeçalhos usam o degradê `@drawable/bg_gradiente_header`, igual à barra de status (`utils/BarrasSistema`).
