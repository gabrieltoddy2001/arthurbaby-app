# Checklist de Integração — Front + Back (2026)

Passo a passo para conectar o app Android ao backend Spring Boot.
Marque cada item conforme concluir.

> Especificação dos endpoints: `CONTRATOS_API.md`.

---

## 1. Rede

- [ ] Trocar `base_url` em `res/values/strings.xml`
- [ ] Criar `network/RetrofitClient.java`
- [ ] Criar `network/ApiService.java`
- [ ] Criar `network/AuthInterceptor.java`
- [ ] Adicionar Retrofit, Gson e Logging Interceptor no `app/build.gradle.kts`
- [ ] Confirmar permissão `INTERNET` no `AndroidManifest.xml`

## 2. Autenticação

- [ ] Backend: `POST /api/auth/login`
- [ ] Backend: `POST /api/auth/cadastro`
- [ ] Backend: `POST /api/auth/recuperar-senha`
- [ ] Front: substituir mock do Login
- [ ] Front: substituir mock do Cadastro
- [ ] Front: substituir mock do Recuperar Senha
- [ ] Salvar token em `EncryptedSharedPreferences`
- [ ] Adicionar interceptor de 401 (logout automático)

## 3. Catálogo

- [ ] Backend: `GET /api/categorias`
- [ ] Backend: `GET /api/categorias/{id}/produtos`
- [ ] Backend: `GET /api/produtos` (filtros + paginação)
- [ ] Backend: `GET /api/produtos/{id}`
- [ ] Backend: `GET /api/produtos/busca?q=`
- [ ] Front: substituir `MockData` por Retrofit
- [ ] Front: trocar cor sólida por `Glide.with(...).load(produto.getImagemUrl())`

## 4. Favoritos

- [ ] Backend: `GET/POST/DELETE /api/favoritos`
- [ ] Front: substituir `FavoritoRepository` por Retrofit

## 5. Endereços

- [ ] Backend: `GET/POST/DELETE /api/clientes/{id}/enderecos`
- [ ] Front: substituir `EnderecoRepository` por Retrofit
- [ ] Front: criar tela de cadastro de endereço

## 6. Pedidos

- [ ] Backend: `POST /api/pedidos`
- [ ] Backend: `GET /api/pedidos/cliente/{id}`
- [ ] Backend: `GET /api/pedidos/{numero}`
- [ ] Backend: `PUT /api/pedidos/{numero}/status`
- [ ] Backend: `PUT /api/pedidos/{numero}/cancelar`
- [ ] Front: substituir mock do `ConfirmaPedidoFragment`
- [ ] Front: substituir mock do `MeusPedidosFragment`
- [ ] Front: substituir mock do `DetalhePedidoFragment`

## 7. Configurações da loja

- [ ] Backend: `GET /api/configuracoes`
- [ ] Front: substituir dados fixos na tela "Sobre"

## 8. Tratamento de erros

- [ ] Criar `ErrorHandler` central
- [ ] Tratar 400 (dados inválidos)
- [ ] Tratar 401 (não autorizado)
- [ ] Tratar 403 (sem permissão)
- [ ] Tratar 404 (não encontrado)
- [ ] Tratar 500 (erro do servidor)
- [ ] Tratar timeout / sem conexão

## 9. Segurança

- [ ] HTTPS em produção
- [ ] Token em `EncryptedSharedPreferences`
- [ ] Senhas nunca em logs
- [ ] LGPD registrado no cadastro
- [ ] CORS configurado no backend

## 10. Publicação

- [ ] Configurar keystore de release
- [ ] Gerar APK/AAB assinado
- [ ] Publicar na Play Store (se aplicável)

## 11. Validação final

- [ ] Fluxo: Splash → Login → Home → Produto → Carrinho → Pedido → Meus Pedidos
- [ ] Testar em 3 tamanhos de tela
- [ ] Testar com internet lenta
- [ ] Testar com backend desligado (mensagens amigáveis)
- [ ] Testar CPF inválido
- [ ] Testar CEP inexistente (ViaCEP)

---

## Observações

- **Ajustes visuais:** o layout atual é uma aproximação funcional do protótipo.
- **Modo escuro:** não implementado nesta versão.
- **Menu lateral (drawer):** não implementado (BottomNavigation cobre os fluxos).

---

*Documento gerado em 2026.*