package br.org.assandef.assandefsystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacoes_material")
@Data
public class SolicitacoesMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSolicitacao;

    @ManyToOne
    @JoinColumn(name = "id_funcionario_solicitante")
    private Funcionario funcionarioSolicitante;

    @ManyToOne
    @JoinColumn(name = "id_material")
    private Material material;

    private Integer quantidadeSolicitada;

    @Column(length = 120)
    private String tipoSaida;

    private LocalDateTime dataSolicitacao;

    @Enumerated(EnumType.STRING)
    private StatusSolicitacao status;

    @Column(columnDefinition = "TEXT")
    private String descricao;
}
