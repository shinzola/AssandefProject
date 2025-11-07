package br.org.assandef.assandefsystem.security;

import br.org.assandef.assandefsystem.model.Funcionario;
import br.org.assandef.assandefsystem.service.FuncionarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("authService")
@RequiredArgsConstructor
public class AuthService {

    private final FuncionarioService funcionarioService;

    public boolean hasHierarquia(Authentication authentication, int nivel) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) return false;
            String username = authentication.getName();
            if (username == null) return false;
            Funcionario f = funcionarioService.findByLogin(username);
            return f != null && f.getHierarquia() != null && f.getHierarquia().intValue() == nivel;
        } catch (Exception ex) {
            // logue se quiser: logger.warn("Erro ao verificar hierarquia", ex);
            return false;
        }
    }

    public boolean hasAnyHierarquia(Authentication authentication, Integer... niveis) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) return false;
            String username = authentication.getName();
            if (username == null) return false;
            Funcionario f = funcionarioService.findByLogin(username);
            if (f == null || f.getHierarquia() == null) return false;
            for (Integer n : niveis) {
                if (f.getHierarquia().equals(n)) return true;
            }
        } catch (Exception ex) {
            // logger.warn("Erro ao verificar hierarquias", ex);
            return false;
        }
        return false;
    }
}