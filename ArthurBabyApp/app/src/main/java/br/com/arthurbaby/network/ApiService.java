package br.com.arthurbaby.network;

import java.util.List;

import br.com.arthurbaby.network.dto.AuthRequest;
import br.com.arthurbaby.network.dto.AuthResponse;
import br.com.arthurbaby.network.dto.CadastroRequest;
import br.com.arthurbaby.network.dto.CategoriaResponse;
import br.com.arthurbaby.network.dto.EnderecoRequest;
import br.com.arthurbaby.network.dto.EnderecoResponse;
import br.com.arthurbaby.network.dto.FavoritoRequest;
import br.com.arthurbaby.network.dto.FavoritoResponse;
import br.com.arthurbaby.network.dto.PageResponse;
import br.com.arthurbaby.network.dto.PedidoRequest;
import br.com.arthurbaby.network.dto.PedidoResponse;
import br.com.arthurbaby.network.dto.ProdutoResponse;
import br.com.arthurbaby.network.dto.CancelamentoRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import br.com.arthurbaby.network.dto.RecuperarSenhaRequest;
import br.com.arthurbaby.network.dto.RedefinirSenhaRequest;

public interface ApiService {

    // ---------- AUTH ----------
    @POST("api/auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("api/auth/cadastro")
    Call<AuthResponse> cadastrar(@Body CadastroRequest request);

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

    // ---------- FAVORITOS ----------
    @GET("api/favoritos/cliente/{clienteId}")
    Call<List<FavoritoResponse>> listarFavoritos(@Path("clienteId") Long clienteId);

    @POST("api/favoritos")
    Call<FavoritoResponse> adicionarFavorito(@Body FavoritoRequest request);

    @DELETE("api/favoritos")
    Call<Void> removerFavorito(@Body FavoritoRequest request);

    // ---------- PEDIDOS ----------
    @POST("api/pedidos")
    Call<PedidoResponse> criarPedido(@Body PedidoRequest request);

    @GET("api/pedidos/cliente/{clienteId}")
    Call<List<PedidoResponse>> listarPedidosCliente(@Path("clienteId") Long clienteId);

    @GET("api/pedidos/{id}")
    Call<PedidoResponse> buscarPedido(@Path("id") Long id);

    @PUT("api/pedidos/{numeroPedido}/cancelar")
    Call<PedidoResponse> cancelarPedido(@Path("numeroPedido") String numeroPedido,
                                        @Body CancelamentoRequest request);

    @GET("api/pedidos/numero/{numeroPedido}")
    Call<PedidoResponse> buscarPedidoPorNumero(@Path("numeroPedido") String numeroPedido);

    // ---------- ENDEREÇOS ----------
    @GET("api/clientes/{clienteId}/enderecos")
    Call<List<EnderecoResponse>> listarEnderecos(@Path("clienteId") Long clienteId);

    @POST("api/clientes/{clienteId}/enderecos")
    Call<EnderecoResponse> criarEndereco(@Path("clienteId") Long clienteId,
                                         @Body EnderecoRequest request);

    @DELETE("api/clientes/{clienteId}/enderecos/{enderecoId}")
    Call<Void> removerEndereco(@Path("clienteId") Long clienteId,
                               @Path("enderecoId") Long enderecoId);

    @PUT("api/clientes/{clienteId}/enderecos/{enderecoId}")
    Call<EnderecoResponse> atualizarEndereco(@Path("clienteId") Long clienteId,
                                             @Path("enderecoId") Long enderecoId,
                                             @Body EnderecoRequest request);
}