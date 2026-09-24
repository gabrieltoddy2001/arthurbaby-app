package br.com.arthurbaby.network.dto;

import java.util.List;

/**
 * Wrapper da paginação do Spring Data.
 * O backend retorna produtos num objeto com "content" (lista), "totalElements", etc.
 */
public class PageResponse<T> {
    public List<T> content;
    public int totalElements;
    public int totalPages;
    public int number;
    public int size;
}