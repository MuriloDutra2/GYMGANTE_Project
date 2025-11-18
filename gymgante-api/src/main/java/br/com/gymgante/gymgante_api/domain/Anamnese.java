package br.com.gymgante.gymgante_api.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id") // ✅ Já está correto
@ToString(exclude = "usuario") // ⭐ ADICIONAR ESTA LINHA
@Entity
@Table(name = "tb_anamnese")
public class Anamnese {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", unique = true)
    private Usuario usuario;

    @Column(name = "objetivo_principal", nullable = false)
    private String objetivoPrincipal;

    @Column(name = "dias_por_semana", nullable = false)
    private String diasPorSemana;

    @Column(nullable = false)
    private String nivel;

    @Column(name = "tem_restricao", nullable = false)
    private boolean temRestricao;
}