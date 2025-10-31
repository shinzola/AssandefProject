package br.org.assandef.assandefsystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "prescricoes")
@Data
public class Prescricao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPrescricao;

    @ManyToOne
    @JoinColumn(name = "id_evolucao")
    private Evolucao evolucao;

    @Column(length = 50)
    private String tipo;

    @Column(columnDefinition = "TEXT")
    private String descricao;
}
