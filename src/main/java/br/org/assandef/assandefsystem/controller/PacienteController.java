package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.Paciente;
import br.org.assandef.assandefsystem.model.Telefone;
import br.org.assandef.assandefsystem.service.PacienteService;
import br.org.assandef.assandefsystem.service.TelefoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private static final String REDIRECT_ATENDIMENTO = "redirect:/atendimento";

    private final PacienteService pacienteService;
    private final TelefoneService telefoneService;

    @GetMapping
    public String listar(Model model,
                         @ModelAttribute("msg") String msg,
                         @ModelAttribute("erro") String erro) {
        List<Paciente> pacientes = pacienteService.findAll();
        model.addAttribute("pacientes", pacientes);
        model.addAttribute("paciente", new Paciente());

        if (msg != null && !msg.isBlank()) model.addAttribute("msg", msg);
        if (erro != null && !erro.isBlank()) model.addAttribute("erro", erro);

        return "atendimento/atendimento";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Paciente paciente, RedirectAttributes ra) {
        try {
            if (!validarCpfUnico(paciente, ra)) {
                return REDIRECT_ATENDIMENTO;
            }

            boolean isNew = (paciente.getIdPaciente() == null);
            Paciente pacienteSalvo = pacienteService.save(paciente);

            salvarTelefones(paciente, pacienteSalvo);

            ra.addFlashAttribute("msg", isNew
                    ? "Paciente cadastrado com sucesso!"
                    : "Paciente atualizado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao salvar paciente: " + e.getMessage());
        }
        return REDIRECT_ATENDIMENTO;
    }

    private boolean validarCpfUnico(Paciente paciente, RedirectAttributes ra) {
        if (paciente.getCpf() != null && !paciente.getCpf().isBlank()) {
            try {
                Paciente existente = pacienteService.findByCpf(paciente.getCpf());
                if (existente != null && !existente.getIdPaciente().equals(paciente.getIdPaciente())) {
                    ra.addFlashAttribute("erro", "Já existe um paciente cadastrado com este CPF.");
                    return false;
                }
            } catch (RuntimeException e) {
                // CPF não encontrado, pode continuar
            }
        }
        return true;
    }

    private void salvarTelefones(Paciente paciente, Paciente pacienteSalvo) {
        if (paciente.getTelefones() != null) {
            for (Telefone telefone : paciente.getTelefones()) {
                if (telefone.getNumero() != null && !telefone.getNumero().isBlank()) {
                    telefone.setPaciente(pacienteSalvo);
                    telefoneService.save(telefone);
                }
            }
        }
    }

    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            pacienteService.deleteById(id);
            ra.addFlashAttribute("msg", "Paciente excluído com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao excluir paciente. Pode haver atendimentos vinculados.");
        }
        return REDIRECT_ATENDIMENTO;
    }

    @ResponseBody
    @GetMapping("/{id}")
    public ResponseEntity<Paciente> obter(@PathVariable("id") Integer id) {
        try {
            Paciente paciente = pacienteService.findById(id);
            return ResponseEntity.ok(paciente);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @ResponseBody
    @GetMapping("/buscar-cpf")
    public ResponseEntity<Paciente> buscarPorCpf(@RequestParam String cpf) {
        try {
            Paciente paciente = pacienteService.findByCpf(cpf);
            return ResponseEntity.ok(paciente);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}