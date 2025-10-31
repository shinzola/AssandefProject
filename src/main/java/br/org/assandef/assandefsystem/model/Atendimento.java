package br.org.assandef.assandefsystem.model;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "atendimentos")
@Data
public class Atendimento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAtendimento;

    @ManyToOne
    @JoinColumn(name = "id_paciente")
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "id_funcionario")
    private Funcionario funcionario;

    private LocalDateTime dataHoraInicio;

    private LocalDateTime dataHoraFim;

    @Column(length = 50)
    private String status;

    @Column(length = 50)
    private String tipoEncaminhamento;

    @OneToMany(mappedBy = "atendimento")
    private List<Evolucao> evolucoes;
}
