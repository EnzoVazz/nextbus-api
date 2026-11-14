package br.com.fiap.nextbus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "AVISO")
public class Aviso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aviso")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rotina", nullable = false)
    private Rotina rotina;

    @Column(name = "data_aviso", nullable = false)
    private LocalDate dataAviso;

    @Column(name = "letreiro", nullable = false, length = 20)
    private String letreiro;

    @Column(name = "nome_ponto", nullable = false, length = 160)
    private String nomePonto;

    @Column(name = "identificador_veiculo", length = 20)
    private String identificadorVeiculo;

    @Column(name = "minutos")
    private Integer minutos;

    @Column(name = "idade_gps_segundos")
    private Integer idadeGpsSegundos;

    @Column(name = "texto", nullable = false, length = 400)
    private String texto;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private StatusAviso status;

    public Aviso() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Rotina getRotina() { return rotina; }
    public void setRotina(Rotina rotina) { this.rotina = rotina; }

    public LocalDate getDataAviso() { return dataAviso; }
    public void setDataAviso(LocalDate dataAviso) { this.dataAviso = dataAviso; }

    public String getLetreiro() { return letreiro; }
    public void setLetreiro(String letreiro) { this.letreiro = letreiro; }

    public String getNomePonto() { return nomePonto; }
    public void setNomePonto(String nomePonto) { this.nomePonto = nomePonto; }

    public String getIdentificadorVeiculo() { return identificadorVeiculo; }
    public void setIdentificadorVeiculo(String identificadorVeiculo) { this.identificadorVeiculo = identificadorVeiculo; }

    public Integer getMinutos() { return minutos; }
    public void setMinutos(Integer minutos) { this.minutos = minutos; }

    public Integer getIdadeGpsSegundos() { return idadeGpsSegundos; }
    public void setIdadeGpsSegundos(Integer idadeGpsSegundos) { this.idadeGpsSegundos = idadeGpsSegundos; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public StatusAviso getStatus() { return status; }
    public void setStatus(StatusAviso status) { this.status = status; }
}
