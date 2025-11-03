package br.org.assandef.assandefsystem.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "atendimentos")
@Data
public class Atendimento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_atendimento")
    private Integer idAtendimento;

    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    @NotNull(message = "Paciente é obrigatório")
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "id_funcionario", nullable = false)
    @NotNull(message = "Profissional é obrigatório")
    private Funcionario funcionario;

    @Column(name = "data_hora_inicio")
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_fim")
    private LocalDateTime dataHoraFim;

    @Column(name = "status", length = 50, nullable = false)
    @NotBlank(message = "Status é obrigatório")
    private String status = "Aguardando";

    @Column(name = "tipo_encaminhamento", length = 50, nullable = false)
    @NotBlank(message = "Tipo de encaminhamento é obrigatório")
    private String tipoEncaminhamento;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @OneToMany(mappedBy = "atendimento", cascade = CascadeType.ALL)
    private List<Evolucao> evolucoes;
}
