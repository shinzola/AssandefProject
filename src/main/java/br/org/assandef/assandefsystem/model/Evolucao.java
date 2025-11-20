package br.org.assandef.assandefsystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "evolucoes")
@Data
public class Evolucao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEvolucao;

    @ManyToOne
    @JoinColumn(name = "id_atendimento")
    private Atendimento atendimento;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private LocalDateTime dataHoraRegistro;

    @OneToMany(mappedBy = "evolucao",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Prescricao> prescricoes;
}