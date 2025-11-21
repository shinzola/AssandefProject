package br.org.assandef.assandefsystem.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "pacientes")
@Data
@ToString(exclude = "telefones")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paciente")
    private Integer idPaciente;

    @NotBlank(message = "Nome completo é obrigatório")
    @Column(name = "nome_completo", nullable = false)
    private String nomeCompleto;

    @Size(max = 11)
    @Column(name = "cpf", unique = true, length = 11)
    private String cpf;

    @Size(max = 20)
    @Column(name = "rg", length = 20)
    private String rg;

    @Size(max = 15)
    @Column(name = "n_sus", length = 15)
    private String nSus;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Size(max = 15)
    @Column(name = "sexo", length = 15)
    private String sexo;

    @Column(name = "endereco", columnDefinition = "TEXT")
    private String endereco;

    @Column(name = "nome_responsavel")
    private String nomeResponsavel;

    @Column(name = "contato_responsavel")
    private String contatoResponsavel;

    @OneToMany(mappedBy = "paciente", cascade = {CascadeType.MERGE, CascadeType.REFRESH}, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Telefone> telefones = new ArrayList<>();

    @Transient
    private String telefonePrincipal; // preenchido em runtime para exibição

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Atendimento> atendimentos = new ArrayList<>();

}