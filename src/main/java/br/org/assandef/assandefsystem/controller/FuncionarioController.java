package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.Funcionario;
import br.org.assandef.assandefsystem.service.FuncionarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/funcionarios")
@RequiredArgsConstructor
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    @GetMapping
    public String listarFuncionarios(Model model) {
        model.addAttribute("funcionarios", funcionarioService.findAll());
        model.addAttribute("funcionarioForm", new Funcionario());
        model.addAttribute("isEdicao", false);
        return "funcionarios/gestaofuncionarios";
    }

    @GetMapping("/editar/{id}")
    public String editarFuncionario(@PathVariable Integer id, Model model) {
        Funcionario funcionario = funcionarioService.findById(id);
        model.addAttribute("funcionarios", funcionarioService.findAll());
        model.addAttribute("funcionarioForm", funcionario);
        model.addAttribute("isEdicao", true);
        return "funcionarios/gestaofuncionarios";
    }

    @PostMapping("/salvar")
    public String salvarFuncionario(
            @Valid @ModelAttribute("funcionarioForm") Funcionario funcionario,
            BindingResult result,
            @RequestParam(value = "senha", required = false) String senhaPlana,
            @RequestParam(value = "confirmarSenha", required = false) String confirmarSenha,
            Model model,
            RedirectAttributes redirectAttributes) {

        boolean isEdicao = funcionario.getIdFuncionario() != null;

        // Validação de duplicidade de login
        if (funcionario.getLogin() != null && !funcionario.getLogin().isBlank()) {
            try {
                Funcionario existente = funcionarioService.findByLogin(funcionario.getLogin());
                if (!isEdicao && existente != null) {
                    result.rejectValue("login", "error.funcionario", "Login já cadastrado");
                } else if (isEdicao && existente != null && !existente.getIdFuncionario().equals(funcionario.getIdFuncionario())) {
                    result.rejectValue("login", "error.funcionario", "Login já cadastrado por outro usuário");
                }
            } catch (RuntimeException ex) {
                // não encontrado -> ok
            }
        }

        // Validação de senha: criação obrigatória; edição => opcional (mas se informada, confirmar obrigatório)
        if (!isEdicao) {
            if (senhaPlana == null || senhaPlana.trim().isEmpty()) {
                result.rejectValue("senhaHash", "error.funcionario", "Senha é obrigatória");
            } else if (!senhaPlana.equals(confirmarSenha)) {
                result.rejectValue("senhaHash", "error.funcionario", "As senhas não coincidem");
            }
        } else {
            if (senhaPlana != null && !senhaPlana.trim().isEmpty()) {
                if (!senhaPlana.equals(confirmarSenha)) {
                    result.rejectValue("senhaHash", "error.funcionario", "As senhas não coincidem");
                }
            }
        }

        if (result.hasErrors()) {
            model.addAttribute("funcionarios", funcionarioService.findAll());
            model.addAttribute("funcionarioForm", funcionario);
            model.addAttribute("isEdicao", isEdicao);
            return "funcionarios/gestaofuncionarios";
        }

        try {
            funcionarioService.save(funcionario, senhaPlana);
            redirectAttributes.addFlashAttribute("mensagem",
                    isEdicao ? "Funcionário atualizado com sucesso!" : "Funcionário cadastrado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar funcionário: " + e.getMessage());
            return "redirect:/funcionarios";
        }

        return "redirect:/funcionarios";
    }

    // Exclusão de funcionários desabilitada para manter histórico e integridade do sistema
    /*
    @PostMapping("/excluir/{id}")
    public String excluirFuncionario(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            funcionarioService.deleteById(id);
            redirectAttributes.addFlashAttribute("mensagem", "Funcionário excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao excluir funcionário: " + e.getMessage());
        }
        return "redirect:/funcionarios";
    }
    */
}