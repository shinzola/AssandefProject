package br.org.assandef.assandefsystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "funcionarios")
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_funcionario")
    private Integer id;

    @NotBlank(message = "Nome completo é obrigatório")
    @Column(name = "nome_completo", nullable = false)
    private String nomeCompleto;

    @NotBlank(message = "Login é obrigatório")
    @Column(name = "login", nullable = false, unique = true, length = 80)
    private String login;

    @NotBlank(message = "Senha é obrigatória")
    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @NotNull(message = "Hierarquia é obrigatória")
    @Column(name = "hierarquia", nullable = false)
    private Integer hierarquia;

  
    public Funcionario() {
    }

    public Funcionario(String nomeCompleto, String login, String senhaHash, Integer hierarquia) {
        this.nomeCompleto = nomeCompleto;
        this.login = login;
        this.senhaHash = senhaHash;
        this.hierarquia = hierarquia;
    }

   
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public Integer getHierarquia() {
        return hierarquia;
    }

    public void setHierarquia(Integer hierarquia) {
        this.hierarquia = hierarquia;
    }
}
