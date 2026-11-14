package br.com.fiap.nextbus.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public class PaginaResponse<T> {

    private List<T> conteudo;
    private int pagina;
    private int tamanho;
    private long totalElementos;
    private int totalPaginas;

    public PaginaResponse() {
    }

    public PaginaResponse(Page<T> page) {
        this.conteudo = page.getContent();
        this.pagina = page.getNumber();
        this.tamanho = page.getSize();
        this.totalElementos = page.getTotalElements();
        this.totalPaginas = page.getTotalPages();
    }

    public List<T> getConteudo() { return conteudo; }
    public void setConteudo(List<T> conteudo) { this.conteudo = conteudo; }

    public int getPagina() { return pagina; }
    public void setPagina(int pagina) { this.pagina = pagina; }

    public int getTamanho() { return tamanho; }
    public void setTamanho(int tamanho) { this.tamanho = tamanho; }

    public long getTotalElementos() { return totalElementos; }
    public void setTotalElementos(long totalElementos) { this.totalElementos = totalElementos; }

    public int getTotalPaginas() { return totalPaginas; }
    public void setTotalPaginas(int totalPaginas) { this.totalPaginas = totalPaginas; }
}
