package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.Paciente;
import br.org.assandef.assandefsystem.model.Telefone;
import br.org.assandef.assandefsystem.service.PacienteService;
import br.org.assandef.assandefsystem.service.TelefoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;
    private final TelefoneService telefoneService;

    @GetMapping
    public String listarPacientes(Model model,
                                  @RequestParam(required = false) String busca,
                                  @ModelAttribute("msg") String msg,
                                  @ModelAttribute("erro") String erro) {
        List<Paciente> pacientes;

        if (busca != null && !busca.isBlank()) {
            pacientes = pacienteService.buscarPorNomeOuCpf(busca);
        } else {
            pacientes = pacienteService.findAll();
        }

        model.addAttribute("pacientes", pacientes);
        model.addAttribute("paciente", new Paciente());

        if (msg != null && !msg.isBlank()) model.addAttribute("msg", msg);
        if (erro != null && !erro.isBlank()) model.addAttribute("erro", erro);

        return "pacientes/gestao-pacientes";
    }

    @PostMapping("/salvar")
    public String salvarPaciente(
            @Valid @ModelAttribute Paciente paciente,
            BindingResult result,
            @RequestParam(name = "nSus", required = false) String nSus,
            RedirectAttributes ra) {

        // Garante binding do Número SUS
        if (nSus != null) {
            paciente.setNSus(nSus);
        }

        boolean isEdicao = paciente.getIdPaciente() != null;

        // Validação de CPF duplicado
        if (paciente.getCpf() != null && !paciente.getCpf().isBlank()) {
            try {
                Paciente existente = pacienteService.findByCpf(paciente.getCpf());
                if (!isEdicao && existente != null) {
                    ra.addFlashAttribute("erro", "CPF já cadastrado");
                    return "redirect:/pacientes";
                } else if (isEdicao && existente != null &&
                          !existente.getIdPaciente().equals(paciente.getIdPaciente())) {
                    ra.addFlashAttribute("erro", "CPF já cadastrado por outro paciente");
                    return "redirect:/pacientes";
                }
            } catch (RuntimeException ex) {
                // CPF não encontrado -> ok
            }
        }

        if (result.hasErrors()) {
            ra.addFlashAttribute("erro", "Erro ao validar dados do paciente");
            return "redirect:/pacientes";
        }

        try {
            pacienteService.save(paciente);
            ra.addFlashAttribute("msg", isEdicao
                ? "Paciente atualizado com sucesso!"
                : "Paciente cadastrado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao salvar paciente: " + e.getMessage());
        }

        return "redirect:/pacientes";
    }

    @GetMapping("/deletar/{id}")
    public String excluirPaciente(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Paciente paciente = pacienteService.findById(id);

            // Verifica se há atendimentos vinculados
            if (paciente.getAtendimentos() != null && !paciente.getAtendimentos().isEmpty()) {
                ra.addFlashAttribute("erro", "Não é possível excluir este paciente pois ele possui " +
                    paciente.getAtendimentos().size() + " atendimento(s) vinculado(s). " +
                    "Exclua os atendimentos primeiro.");
                return "redirect:/pacientes";
            }

            pacienteService.deleteById(id);
            ra.addFlashAttribute("msg", "Paciente excluído com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao excluir paciente. " +
                "Verifique se não há registros vinculados a este paciente.");
        }
        return "redirect:/pacientes";
    }

    // API JSON para buscar paciente
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<br.org.assandef.assandefsystem.dto.PacienteDTO> obterPaciente(@PathVariable Integer id) {
        try {
            Paciente paciente = pacienteService.findById(id);
            br.org.assandef.assandefsystem.dto.PacienteDTO dto = br.org.assandef.assandefsystem.dto.PacienteDTO.from(paciente);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Gestão de telefones
    @PostMapping("/{id}/telefone/adicionar")
    public String adicionarTelefone(
            @PathVariable Integer id,
            @RequestParam String numero,
            @RequestParam(required = false) String descricao,
            RedirectAttributes ra) {
        try {
            Paciente paciente = pacienteService.findById(id);
            Telefone telefone = new Telefone(numero, descricao);
            telefone.setPaciente(paciente);
            telefoneService.save(telefone);
            ra.addFlashAttribute("msg", "Telefone adicionado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao adicionar telefone: " + e.getMessage());
        }
        return "redirect:/pacientes";
    }

    @PostMapping("/telefone/{id}/remover")
    public String removerTelefone(
            @PathVariable Integer id,
            RedirectAttributes ra) {
        try {
            telefoneService.deleteById(id);
            ra.addFlashAttribute("msg", "Telefone removido com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao remover telefone: " + e.getMessage());
        }
        return "redirect:/pacientes";
    }
}
