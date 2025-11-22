package br.com.gymgante.gymgante_api.domain;

import br.com.gymgante.gymgante_api.dto.DadosCadastroAnamnese;
import jakarta.persistence.*;

@Entity
@Table(name = "tb_anamnese")
public class Anamnese {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "objetivo_principal", nullable = false)
    private String objetivoPrincipal;

    @Column(name = "dias_por_semana", nullable = false)
    private String diasPorSemana;

    @Column(nullable = false)
    private String nivel;

    @Column(name = "tem_restricao", nullable = false)
    private Boolean temRestricao;

    // Construtor vazio (obrigatório para JPA)
    public Anamnese() {
    }

    // Construtor com dados
    public Anamnese(DadosCadastroAnamnese dados, Usuario usuario) {
        this.usuario = usuario;
        this.objetivoPrincipal = dados.objetivoPrincipal();
        this.diasPorSemana = dados.diasPorSemana();
        this.nivel = dados.nivel();
        this.temRestricao = dados.temRestricao();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getObjetivoPrincipal() {
        return objetivoPrincipal;
    }

    public void setObjetivoPrincipal(String objetivoPrincipal) {
        this.objetivoPrincipal = objetivoPrincipal;
    }

    public String getDiasPorSemana() {
        return diasPorSemana;
    }

    public void setDiasPorSemana(String diasPorSemana) {
        this.diasPorSemana = diasPorSemana;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public Boolean getTemRestricao() {
        return temRestricao;
    }

    public void setTemRestricao(Boolean temRestricao) {
        this.temRestricao = temRestricao;
    }
}