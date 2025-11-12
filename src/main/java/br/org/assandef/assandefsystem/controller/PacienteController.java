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
            @RequestParam(name = "telefonesExcluir", required = false) List<Integer> telefonesExcluir,
            jakarta.servlet.http.HttpServletRequest request,
            RedirectAttributes ra) {

        // Captura telefones do request
        java.util.Map<String, String[]> telefones = new java.util.HashMap<>();
        java.util.Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            if (paramName.startsWith("telefones[")) {
                telefones.put(paramName, request.getParameterValues(paramName));
            }
        }

        // Garante binding do Número SUS
        paciente.setNSus(nSus);

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
            // Limpa a lista de telefones do paciente para evitar problemas com orphanRemoval
            // Os telefones serão salvos manualmente depois
            if (paciente.getTelefones() != null) {
                paciente.getTelefones().clear();
            }

            // Salva o paciente
            Paciente pacienteSalvo = pacienteService.save(paciente);

            // Excluir telefones marcados para exclusão
            if (telefonesExcluir != null && !telefonesExcluir.isEmpty()) {
                telefonesExcluir.forEach(idTelefone -> {
                    try {
                        telefoneService.deleteById(idTelefone);
                    } catch (Exception e) {
                        System.err.println("Erro ao excluir telefone ID " + idTelefone + ": " + e.getMessage());
                    }
                });
            }

            // Processar telefones enviados pelo formulário
            if (telefones != null && !telefones.isEmpty()) {

                // Extrair números e descrições
                java.util.Map<Integer, String> numeros = new java.util.HashMap<>();
                java.util.Map<Integer, String> descricoes = new java.util.HashMap<>();

                telefones.forEach((key, values) -> {
                    if (key.matches("telefones\\[(\\d+)\\]\\.numero") && values.length > 0) {
                        int index = Integer.parseInt(key.replaceAll("[^0-9]", ""));
                        String numero = values[0];
                        if (numero != null && !numero.trim().isEmpty()) {
                            numeros.put(index, numero.trim());
                        }
                    } else if (key.matches("telefones\\[(\\d+)\\]\\.descricao") && values.length > 0) {
                        int index = Integer.parseInt(key.replaceAll("[^0-9]", ""));
                        String descricao = values[0];
                        if (descricao != null && !descricao.trim().isEmpty()) {
                            descricoes.put(index, descricao.trim());
                        }
                    }
                });

                // Combinar todos os índices
                java.util.Set<Integer> todosIndices = new java.util.HashSet<>();
                todosIndices.addAll(numeros.keySet());
                todosIndices.addAll(descricoes.keySet());

                // Salvar cada telefone
                for (Integer index : todosIndices) {
                    String numero = numeros.get(index);
                    String descricao = descricoes.get(index);

                    // Validação: AMBOS devem estar preenchidos
                    if (numero != null && !numero.trim().isEmpty() &&
                        descricao != null && !descricao.trim().isEmpty()) {
                        try {
                            Telefone telefone = new Telefone();
                            telefone.setNumero(numero);
                            telefone.setDescricao(descricao);
                            telefone.setPaciente(pacienteSalvo);
                            telefoneService.save(telefone);
                        } catch (Exception e) {
                            System.err.println("Erro ao salvar telefone: " + e.getMessage());
                        }
                    }
                }
            }

            ra.addFlashAttribute("msg", isEdicao
                ? "Paciente atualizado com sucesso!"
                : "Paciente cadastrado com sucesso!");
        } catch (Exception e) {
            ra.addFlashAttribute("erro", "Erro ao salvar paciente: " + e.getMessage());
        }

        return "redirect:/pacientes";
    }

    // API para verificar se paciente pode ser excluído
    @GetMapping("/{id}/pode-excluir")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> verificarPodeExcluir(@PathVariable Integer id) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            boolean temAtendimentos = pacienteService.temAtendimentosVinculados(id);
            int qtdAtendimentos = temAtendimentos ? pacienteService.contarAtendimentos(id) : 0;

            response.put("podeExcluir", !temAtendimentos);
            response.put("qtdAtendimentos", qtdAtendimentos);

            if (temAtendimentos) {
                response.put("mensagem", "Não é possível excluir este paciente pois ele possui " +
                    qtdAtendimentos + " atendimento(s) vinculado(s). " +
                    "Os atendimentos devem ser mantidos para histórico médico.");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("podeExcluir", false);
            response.put("mensagem", "Erro ao verificar: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
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
