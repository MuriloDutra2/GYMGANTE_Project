package br.com.gymgante.gymgante_api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id") // Usa só o ID para comparações, evitando o loop
@Entity
@Table(name = "tb_anamnese")
public class Anamnese {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- O RELACIONAMENTO-CHAVE ---
    // Esta é a ligação entre o formulário e o usuário
    @OneToOne(fetch = FetchType.LAZY) // Um-para-Um: 1 Usuário tem 1 Anamnese
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", unique = true)
    // Cria uma coluna 'usuario_id' que é uma Chave Estrangeira (FK)
    // para a coluna 'id' da 'tb_usuario'.
    private Usuario usuario;

    // --- AS RESPOSTAS DO FORMULÁRIO ---
    // (Com base nas perguntas que você definiu)

    @Column(name = "objetivo_principal", nullable = false)
    private String objetivoPrincipal; // "Ganho de Massa Magra", "Perda de Gordura", etc.

    @Column(name = "dias_por_semana", nullable = false)
    private String diasPorSemana; // "3x por semana", "5x por semana", etc.

    @Column(nullable = false)
    private String nivel; // "Iniciante", "Intermediário", "Avançado"

    @Column(name = "tem_restricao", nullable = false)
    private boolean temRestricao; // true ou false
    
   
}