package br.org.assandef.assandefsystem.dto;

import br.org.assandef.assandefsystem.model.Atendimento;
import br.org.assandef.assandefsystem.model.Evolucao;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class AtendimentoDetalhesDTO {
    private Integer idAtendimento;
    private String status;
    private String tipoEncaminhamento;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;
    private PacienteSimplificadoDTO paciente;
    private FuncionarioSimplificadoDTO funcionario;
    private List<EvolucaoDTO> evolucoes = new ArrayList<>();

    @Data
    public static class PacienteSimplificadoDTO {
        private Integer idPaciente;
        private String nomeCompleto;
        private String cpf;
    }

    @Data
    public static class FuncionarioSimplificadoDTO {
        private Integer idFuncionario;
        private String nomeCompleto;
    }

    @Data
    public static class EvolucaoDTO {
        private Integer idEvolucao;
        private String descricao;
        private LocalDateTime dataHoraRegistro;
    }

    public static AtendimentoDetalhesDTO from(Atendimento atendimento) {
        AtendimentoDetalhesDTO dto = new AtendimentoDetalhesDTO();
        dto.setIdAtendimento(atendimento.getIdAtendimento());
        dto.setStatus(atendimento.getStatus());
        dto.setTipoEncaminhamento(atendimento.getTipoEncaminhamento());
        dto.setDataHoraInicio(atendimento.getDataHoraInicio());
        dto.setDataHoraFim(atendimento.getDataHoraFim());

        if (atendimento.getPaciente() != null) {
            PacienteSimplificadoDTO pacienteDTO = new PacienteSimplificadoDTO();
            pacienteDTO.setIdPaciente(atendimento.getPaciente().getIdPaciente());
            pacienteDTO.setNomeCompleto(atendimento.getPaciente().getNomeCompleto());
            pacienteDTO.setCpf(atendimento.getPaciente().getCpf());
            dto.setPaciente(pacienteDTO);
        }

        if (atendimento.getFuncionario() != null) {
            FuncionarioSimplificadoDTO funcionarioDTO = new FuncionarioSimplificadoDTO();
            funcionarioDTO.setIdFuncionario(atendimento.getFuncionario().getIdFuncionario());
            funcionarioDTO.setNomeCompleto(atendimento.getFuncionario().getNomeCompleto());
            dto.setFuncionario(funcionarioDTO);
        }

        if (atendimento.getEvolucoes() != null) {
            for (Evolucao evolucao : atendimento.getEvolucoes()) {
                EvolucaoDTO evolucaoDTO = new EvolucaoDTO();
                evolucaoDTO.setIdEvolucao(evolucao.getIdEvolucao());
                evolucaoDTO.setDescricao(evolucao.getDescricao());
                evolucaoDTO.setDataHoraRegistro(evolucao.getDataHoraRegistro());
                dto.getEvolucoes().add(evolucaoDTO);
            }
        }

        return dto;
    }
}

