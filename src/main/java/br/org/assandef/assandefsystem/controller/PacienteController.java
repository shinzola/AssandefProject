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
            @RequestParam(name = "telefonePrincipal", required = false) String telefonePrincipal,
            RedirectAttributes ra) {

        System.out.println("=== SALVANDO PACIENTE ===");
        System.out.println("ID Paciente: " + paciente.getIdPaciente());
        System.out.println("Nome: " + paciente.getNomeCompleto());
        System.out.println("nSus recebido: [" + nSus + "]");
        System.out.println("telefonePrincipal recebido: [" + telefonePrincipal + "]");

        // Garante binding do Número SUS (sempre atribui, mesmo se vazio)
        paciente.setNSus(nSus);
        System.out.println("nSus após set: [" + paciente.getNSus() + "]");

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
            // Salva o paciente primeiro
            Paciente pacienteSalvo = pacienteService.save(paciente);
            System.out.println("Paciente salvo com ID: " + pacienteSalvo.getIdPaciente());
            System.out.println("nSus no banco: [" + pacienteSalvo.getNSus() + "]");

            // Salva telefone se fornecido (tanto em criação quanto em edição)
            if (telefonePrincipal != null && !telefonePrincipal.isBlank()) {
                // Verifica se já existe um telefone "Principal" para este paciente
                List<Telefone> telefonesExistentes = telefoneService.findByPaciente(pacienteSalvo.getIdPaciente());
                boolean jaTemTelefonePrincipal = telefonesExistentes.stream()
                    .anyMatch(t -> "Principal".equals(t.getDescricao()));

                if (!jaTemTelefonePrincipal) {
                    System.out.println("Criando telefone principal: " + telefonePrincipal);
                    Telefone telefone = new Telefone();
                    telefone.setNumero(telefonePrincipal);
                    telefone.setDescricao("Principal");
                    telefone.setPaciente(pacienteSalvo);
                    telefoneService.save(telefone);
                    System.out.println("Telefone salvo com sucesso!");
                } else {
                    System.out.println("Paciente já possui telefone principal, não criando novo");
                }
            } else {
                System.out.println("Telefone não fornecido ou vazio");
            }

            ra.addFlashAttribute("msg", isEdicao
                ? "Paciente atualizado com sucesso!"
                : "Paciente cadastrado com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao salvar paciente: " + e.getMessage());
            e.printStackTrace();
            ra.addFlashAttribute("erro", "Erro ao salvar paciente: " + e.getMessage());
        }

        return "redirect:/pacientes";
    }

    @GetMapping("/deletar/{id}")
    public String excluirPaciente(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            // Verifica se há atendimentos vinculados antes de tentar excluir
            if (pacienteService.temAtendimentosVinculados(id)) {
                int qtdAtendimentos = pacienteService.contarAtendimentos(id);
                ra.addFlashAttribute("erro", "Não é possível excluir este paciente pois ele possui " +
                    qtdAtendimentos + " atendimento(s) vinculado(s). " +
                    "Os atendimentos devem ser mantidos para histórico médico.");
                return "redirect:/pacientes";
            }

            pacienteService.deleteById(id);
            ra.addFlashAttribute("msg", "Paciente excluído com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao excluir paciente: " + e.getMessage());
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
