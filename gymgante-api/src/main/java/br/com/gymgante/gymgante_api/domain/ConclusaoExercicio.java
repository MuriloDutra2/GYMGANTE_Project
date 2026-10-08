package br.com.gymgante.gymgante_api.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

/** Um exercício marcado como concluído por um usuário em uma data. */
@Entity
@Table(name = "tb_conclusao_exercicio",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "data", "dia_treino", "exercicio"}))
public class ConclusaoExercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "dia_treino", nullable = false, length = 120)
    private String diaTreino;

    @Column(nullable = false, length = 200)
    private String exercicio;

    public ConclusaoExercicio() {
    }

    public ConclusaoExercicio(Long usuarioId, LocalDate data, String diaTreino, String exercicio) {
        this.usuarioId = usuarioId;
        this.data = data;
        this.diaTreino = diaTreino;
        this.exercicio = exercicio;
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public LocalDate getData() { return data; }
    public String getDiaTreino() { return diaTreino; }
    public String getExercicio() { return exercicio; }
}
