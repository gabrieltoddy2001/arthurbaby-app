package br.com.arthurbaby.network;

import java.util.List;

import br.com.arthurbaby.network.dto.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ---------- AUTH ----------
    @POST("api/auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("api/auth/cadastro")
    Call<AuthResponse> cadastrar(@Body CadastroRequest request);

    @GET("api/auth/me")
    Call<UsuarioResponse> buscarMeuPerfil();

    @POST("api/auth/recuperar-senha")
    Call<Void> recuperarSenha(@Body RecuperarSenhaRequest request);

    @POST("api/auth/redefinir-senha")
    Call<Void> redefinirSenha(@Body RedefinirSenhaRequest request);

    // ---------- CATÁLOGO ----------
    @GET("api/categorias")
    Call<List<CategoriaResponse>> listarCategorias();

    @GET("api/produtos")
    Call<PageResponse<ProdutoResponse>> listarProdutos(
            @Query("q") String busca,
            @Query("categoriaId") Long categoriaId,
            @Query("promocao") Boolean promocao,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/produtos/{id}")
    Call<ProdutoResponse> buscarProduto(@Path("id") Long id);

    // ---------- CUPONS ----------
    @POST("api/cupons/validar")
    Call<CupomValidacaoResponse> validarCupom(@Body CupomValidacaoRequest request);

    // ---------- FAVORITOS ----------
    @GET("api/favoritos/cliente/{clienteId}")
    Call<List<FavoritoResponse>> listarFavoritos(@Path("clienteId") Long clienteId);

    @POST("api/favoritos")
    Call<FavoritoResponse> adicionarFavorito(@Body FavoritoRequest request);

    @DELETE("api/favoritos/cliente/{clienteId}/produto/{produtoId}")
    Call<Void> removerFavorito(@Path("clienteId") Long clienteId, @Path("produtoId") Long produtoId);

    // ---------- PEDIDOS ----------
    @POST("api/pedidos")
    Call<PedidoResponse> criarPedido(@Body PedidoRequest request);

    // AQUI ESTÁ A MÁGICA DO FILTRO
    @GET("api/pedidos/cliente/{clienteId}")
    Call<List<PedidoResponse>> listarPedidosCliente(
            @Path("clienteId") Long clienteId,
            @Query("status") String status,
            @Query("dataInicio") String dataInicio,
            @Query("dataFim") String dataFim
    );

    @GET("api/pedidos/{id}")
    Call<PedidoResponse> buscarPedido(@Path("id") Long id);

    @GET("api/pedidos/numero/{numeroPedido}")
    Call<PedidoResponse> buscarPedidoPorNumero(@Path("numeroPedido") String numeroPedido);

    @PUT("api/pedidos/{numeroPedido}/cancelar")
    Call<PedidoResponse> cancelarPedido(@Path("numeroPedido") String numeroPedido, @Body CancelamentoRequest request);

    // ---------- CLIENTES ----------
    @GET("api/clientes/{id}")
    Call<UsuarioResponse> buscarCliente(@Path("id") Long id);

    @PUT("api/clientes/{id}")
    Call<UsuarioResponse> atualizarCliente(@Path("id") Long id, @Body ClienteRequest request);

    // ---------- ENDEREÇOS ----------
    @GET("api/clientes/{clienteId}/enderecos")
    Call<List<EnderecoResponse>> listarEnderecos(@Path("clienteId") Long clienteId);

    @POST("api/clientes/{clienteId}/enderecos")
    Call<EnderecoResponse> criarEndereco(@Path("clienteId") Long clienteId, @Body EnderecoRequest request);

    @DELETE("api/clientes/{clienteId}/enderecos/{enderecoId}")
    Call<Void> removerEndereco(@Path("clienteId") Long clienteId, @Path("enderecoId") Long enderecoId);
}