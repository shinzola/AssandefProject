package br.org.assandef.assandefsystem.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.org.assandef.assandefsystem.model.Paciente;
import lombok.Data;

@Data
public class PacienteDTO {
    private Integer idPaciente;
    private String nomeCompleto;
    private String cpf;
    private String rg;

    @JsonProperty("nSus")
    private String nSus;

    private LocalDate dataNascimento;
    private String sexo;
    private String endereco;
    private String nomeResponsavel;
    private String contatoResponsavel;
    private List<TelefoneDTO> telefones;

    public static PacienteDTO from(Paciente paciente) {
        PacienteDTO dto = new PacienteDTO();
        dto.setIdPaciente(paciente.getIdPaciente());
        dto.setNomeCompleto(paciente.getNomeCompleto());
        dto.setCpf(paciente.getCpf());
        dto.setRg(paciente.getRg());
        dto.setNSus(paciente.getNSus());
        dto.setDataNascimento(paciente.getDataNascimento());
        dto.setSexo(paciente.getSexo());
        dto.setEndereco(paciente.getEndereco());
        dto.setNomeResponsavel(paciente.getNomeResponsavel());
        dto.setContatoResponsavel(paciente.getContatoResponsavel());

        if (paciente.getTelefones() != null) {
            dto.setTelefones(
                paciente.getTelefones().stream()
                    .map(TelefoneDTO::from)
                    .collect(Collectors.toList())
            );
        }

        return dto;
    }

    @Data
    public static class TelefoneDTO {
        private Integer id;
        private String numero;
        private String descricao;

        public static TelefoneDTO from(br.org.assandef.assandefsystem.model.Telefone telefone) {
            TelefoneDTO dto = new TelefoneDTO();
            dto.setId(telefone.getId());
            dto.setNumero(telefone.getNumero());
            dto.setDescricao(telefone.getDescricao());
            return dto;
        }
    }
}

