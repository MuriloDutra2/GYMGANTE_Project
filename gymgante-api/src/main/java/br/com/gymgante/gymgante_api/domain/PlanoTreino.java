package br.com.gymgante.gymgante_api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "tb_plano_treino")
public class PlanoTreino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- OS CAMPOS DE "CONSULTA" ---
    // Estes campos são a "chave" que usaremos para
    // encontrar o treino certo com base nas respostas da Anamnese.

    @Column(name = "objetivo_principal", nullable = false)
    private String objetivoPrincipal; // "Ganho de Massa Magra", "Perda de Gordura", etc.

    @Column(name = "dias_por_semana", nullable = false)
    private String diasPorSemana; // "3x por semana", "5x por semana", etc.

    @Column(nullable = false)
    private String nivel; // "Iniciante", "Intermediário", "Avançado"

    // --- O TREINO EM SI (A MÁGICA) ---

    // Esta coluna vai guardar o JSON completo do treino
    @Column(name = "treino_json", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String treinoJson;

  
}