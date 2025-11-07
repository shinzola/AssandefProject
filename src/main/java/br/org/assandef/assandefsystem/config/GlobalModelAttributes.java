package br.org.assandef.assandefsystem.config;

import br.org.assandef.assandefsystem.model.Funcionario;
import br.org.assandef.assandefsystem.service.FuncionarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final FuncionarioService funcionarioService;

    /**
     * Adiciona 'hierarquia' ao model (Integer) quando o usuário estiver autenticado.
     * Presume que o username do Authentication é o login (email) do Funcionario.
     */
    @ModelAttribute("hierarquia")
    public Integer addHierarquia(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        try {
            String username = authentication.getName();
            Funcionario f = funcionarioService.findByLogin(username);
            if (f != null) {
                return f.getHierarquia();
            }
        } catch (Exception e) {
            // se não encontrado, retorna null (sem matar a request)
        }
        return null;
    }
}