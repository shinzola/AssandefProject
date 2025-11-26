package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.Atendimento;
import br.org.assandef.assandefsystem.model.Doador;
import br.org.assandef.assandefsystem.model.Material;
import br.org.assandef.assandefsystem.model.SolicitacoesMaterial;
import br.org.assandef.assandefsystem.service.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import br.org.assandef.assandefsystem.model.Paciente;
import br.org.assandef.assandefsystem.model.Evolucao;
import br.org.assandef.assandefsystem.model.Prescricao;
import java.util.stream.Collectors;
import br.org.assandef.assandefsystem.model.Telefone;
import java.time.LocalDateTime;
@RestController
@RequestMapping({"/almoxarifado/relatorio", "/doadores/relatorio", "/atendimento/relatorio"})
public class RelatorioController {

    private final MaterialService materialService;
    private final SolicitacoesMaterialService solicitacoesMaterialService;
    private final DoadorService doadorService;
    private final AtendimentoService atendimentoService;
    private final PacienteService pacienteService; // <-- ADICIONE
    public RelatorioController(MaterialService materialService,
                               SolicitacoesMaterialService solicitacoesMaterialService,
                               DoadorService doadorService,
                               AtendimentoService atendimentoService, PacienteService pacienteService) {

        this.materialService = materialService;
        this.solicitacoesMaterialService = solicitacoesMaterialService;
        this.doadorService = doadorService;
        this.atendimentoService = atendimentoService;
        this.pacienteService = pacienteService;
    }

    // ----------------------------
    // GET dados (para consumo via JS)
    // ----------------------------
    @GetMapping("/dados")
    public Object getDadosRelatorio(
            @RequestParam String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        if ("estoque".equalsIgnoreCase(tipo) || "Estoque".equalsIgnoreCase(tipo)) {
            return materialService.findAll();
        }

        if ("solicitações".equalsIgnoreCase(tipo) || "Solicitações".equalsIgnoreCase(tipo)) {
            return solicitacoesMaterialService.findByPeriodo(dataInicio, dataFim);
        }

        if ("doadores".equalsIgnoreCase(tipo) || "Doadores".equalsIgnoreCase(tipo)) {
            return doadorService.findByDataCadastroBetween(dataInicio, dataFim);
        }

        if ("atendimentos".equalsIgnoreCase(tipo) || "Atendimentos".equalsIgnoreCase(tipo)) {
            if (dataInicio != null && dataFim != null) {
                return atendimentoService.findByDataHoraInicioBetween(dataInicio.atStartOfDay(), dataFim.plusDays(1).atStartOfDay());
            }
            return atendimentoService.findAll();
        }

        return List.of();
    }

    // ----------------------------
    // POST gerar: gera CSV ou PDF e retorna como download
    // ----------------------------
    @PostMapping("/gerar")
    public void gerarRelatorio(
            @RequestParam(required = false) String tipo,
            @RequestParam String formato,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            HttpServletResponse response,
            jakarta.servlet.http.HttpServletRequest request) throws IOException {

        // Define tipo padrão conforme o prefixo da URL caso não tenha vindo no form
        if (tipo == null || tipo.isBlank()) {
            String uri = request.getRequestURI();
            if (uri.startsWith("/atendimento/")) tipo = "atendimentos";
            else if (uri.startsWith("/almoxarifado/")) tipo = "estoque";
            else if (uri.startsWith("/doadores/")) tipo = "doadores";
        }

        if ("csv".equalsIgnoreCase(formato)) {
            String csv;
            String filename = "relatorio_" + tipo + "_" + LocalDate.now() + ".csv";

            if ("estoque".equalsIgnoreCase(tipo)) {
                List<Material> materiais = materialService.findAll();
                csv = buildCsvEstoque(materiais);
            } else if ("solicitacoes".equalsIgnoreCase(tipo)) {
                List<SolicitacoesMaterial> solicitacoes = solicitacoesMaterialService.findByPeriodo(dataInicio, dataFim);
                csv = buildCsvSolicitacoes(solicitacoes);
            } else if ("doadores".equalsIgnoreCase(tipo)) {
                List<Doador> doadores = doadorService.findByDataCadastroBetween(dataInicio, dataFim);
                csv = buildCsvDoadores(doadores);
            } else if ("atendimentos".equalsIgnoreCase(tipo)) {
                List<Atendimento> atendimentos;
                if (dataInicio != null && dataFim != null) {
                    atendimentos = atendimentoService.findByDataHoraInicioBetween(dataInicio.atStartOfDay(), dataFim.plusDays(1).atStartOfDay());
                } else {
                    atendimentos = atendimentoService.findAll();
                }
                csv = buildCsvAtendimentos(atendimentos);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tipo inválido");
                return;
            }

            byte[] bytes = ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8); // BOM para Excel
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
            return;
        }

        if ("pdf".equalsIgnoreCase(formato)) {
            final String tipoFinal = tipo != null ? tipo : "relatório";
            String filename = "relatorio_" + tipoFinal + "_" + LocalDate.now() + ".pdf";
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            try {
                Document document = new Document(PageSize.A4.rotate()); // Paisagem para relatórios tabulares
                PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

                // Cabeçalho/Rodapé genérico
                writer.setPageEvent(new PdfPageEventHelper() {
                    public void onEndPage(PdfWriter writer, Document document) {
                        try {
                            String logoPath = "static/img/assandef-logo.png";
                            java.net.URL logoUrl = getClass().getClassLoader().getResource(logoPath);
                            if (logoUrl != null) {
                                com.lowagie.text.Image logo = com.lowagie.text.Image.getInstance(logoUrl);
                                logo.scaleToFit(60, 30);
                                logo.setAbsolutePosition(40, document.getPageSize().getHeight() - 50);
                                writer.getDirectContent().addImage(logo);
                            }

                            ColumnText.showTextAligned(
                                    writer.getDirectContent(),
                                    Element.ALIGN_CENTER,
                                    new Phrase("Relatório de " + tipoFinal.toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)),
                                    (document.right() + document.left()) / 2,
                                    document.getPageSize().getHeight() - 40,
                                    0
                            );

                            ColumnText.showTextAligned(
                                    writer.getDirectContent(),
                                    Element.ALIGN_CENTER,
                                    new Phrase("Página " + writer.getPageNumber(), FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY)),
                                    (document.right() + document.left()) / 2,
                                    20,
                                    0
                            );
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                document.open();

                if (dataInicio != null || dataFim != null) {
                    String periodo = "Período: " +
                            (dataInicio != null ? dataInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "início") +
                            " até " +
                            (dataFim != null ? dataFim.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "hoje");
                    Paragraph periodoPar = new Paragraph(periodo, FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY));
                    periodoPar.setAlignment(Element.ALIGN_CENTER);
                    document.add(periodoPar);
                    document.add(Chunk.NEWLINE);
                }

                DateTimeFormatter dtfData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter dtfDataHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                if ("estoque".equalsIgnoreCase(tipo)) {
                    List<Material> materiais = materialService.findAll();
                    PdfPTable table = new PdfPTable(new float[]{3, 2, 1, 2, 2});
                    table.setWidthPercentage(100);
                    addTableHeader(table, new String[]{"Nome", "Categoria", "Quantidade", "Validade", "Fornecedor"});

                    for (Material m : materiais) {
                        table.addCell(createBodyCell(orEmpty(m.getNome())));
                        table.addCell(createBodyCell(orEmpty(m.getCategoria() != null ? m.getCategoria().getNome() : null)));
                        table.addCell(createBodyCell(String.valueOf(m.getQuantidadeAtual() != null ? m.getQuantidadeAtual() : 0)));
                        table.addCell(createBodyCell(m.getDataValidade() != null ? m.getDataValidade().format(dtfData) : "Sem validade"));
                        table.addCell(createBodyCell(orEmpty(m.getFornecedor())));
                    }
                    document.add(table);

                } else if ("Solicitações".equalsIgnoreCase(tipo)) {
                    List<SolicitacoesMaterial> solicitacoes = solicitacoesMaterialService.findByPeriodo(dataInicio, dataFim);
                    PdfPTable table = new PdfPTable(new float[]{2, 1, 2, 2, 1, 2, 2});
                    table.setWidthPercentage(100);
                    addTableHeader(table, new String[]{"Material", "Quantidade", "Solicitante", "Data", "Tipo Saída", "Status", "Justificativa"});

                    for (SolicitacoesMaterial s : solicitacoes) {
                        table.addCell(createBodyCell(orEmpty(s.getMaterial() != null ? s.getMaterial().getNome() : null)));
                        table.addCell(createBodyCell(String.valueOf(s.getQuantidadeSolicitada() != null ? s.getQuantidadeSolicitada() : 0)));
                        table.addCell(createBodyCell(orEmpty(s.getFuncionarioSolicitante() != null ? s.getFuncionarioSolicitante().getNomeCompleto() : null)));
                        table.addCell(createBodyCell(s.getDataSolicitacao() != null ? s.getDataSolicitacao().format(dtfDataHora) : ""));
                        table.addCell(createBodyCell(orEmpty(s.getTipoSaida())));
                        table.addCell(createBodyCell(s.getStatus() != null ? s.getStatus().name() : ""));
                        table.addCell(createBodyCell(orEmpty(s.getDescricao())));
                    }
                    document.add(table);

                } else if ("doadores".equalsIgnoreCase(tipo)) {
                    List<Doador> doadores = doadorService.findByDataCadastroBetween(dataInicio, dataFim);
                    PdfPTable table = new PdfPTable(new float[]{2, 2, 2, 3, 1, 2, 2, 2});
                    table.setWidthPercentage(100);
                    addTableHeader(table, new String[]{"Nome", "CPF/CNPJ", "Telefone", "Email", "Mensalidade", "Data Nascimento", "Sexo", "Data cadastro"});

                    DateTimeFormatter dtfNascimento = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    DateTimeFormatter dtfCadastro = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                    for (Doador d : doadores) {
                        table.addCell(createBodyCell(orEmpty(d.getNome())));
                        table.addCell(createBodyCell(orEmpty(d.getCpfCnpj())));
                        table.addCell(createBodyCell(orEmpty(d.getTelefone())));
                        table.addCell(createBodyCell(orEmpty(d.getEmail())));
                        table.addCell(createBodyCell(d.getMensalidade() != null ? String.format("R$ %.2f", d.getMensalidade()) : ""));
                        table.addCell(createBodyCell(d.getDataNascimento() != null ? d.getDataNascimento().format(dtfNascimento) : ""));
                        table.addCell(createBodyCell(orEmpty(d.getSexo())));
                        table.addCell(createBodyCell(orEmpty(d.getDataCadastro() != null ? d.getDataCadastro().format(dtfCadastro) : "")));
                    }
                    document.add(table);

                } else if ("atendimentos".equalsIgnoreCase(tipo)) {
                    List<Atendimento> atendimentos;
                    if (dataInicio != null && dataFim != null) {
                        atendimentos = atendimentoService.findByDataHoraInicioBetween(dataInicio.atStartOfDay(), dataFim.plusDays(1).atStartOfDay());
                    } else {
                        atendimentos = atendimentoService.findAll();
                    }
                    PdfPTable table = new PdfPTable(new float[]{1, 3, 3, 2, 2, 1.5f});
                    table.setWidthPercentage(100);
                    addTableHeader(table, new String[]{"ID", "Paciente", "Profissional", "Tipo", "Data/Hora", "Status"});

                    for (Atendimento a : atendimentos) {
                        table.addCell(createBodyCell(String.valueOf(a.getIdAtendimento())));
                        table.addCell(createBodyCell(orEmpty(a.getPaciente() != null ? a.getPaciente().getNomeCompleto() : null)));
                        table.addCell(createBodyCell(orEmpty(a.getFuncionario() != null ? a.getFuncionario().getNomeCompleto() : null)));
                        table.addCell(createBodyCell(orEmpty(a.getTipoEncaminhamento())));
                        table.addCell(createBodyCell(a.getDataHoraInicio() != null ? a.getDataHoraInicio().format(dtfDataHora) : ""));
                        table.addCell(createBodyCell(orEmpty(a.getStatus())));
                    }
                    document.add(table);

                } else {
                    document.add(new Paragraph("Tipo de relatório inválido."));
                }

                document.close();
            } catch (Exception e) {
                response.reset();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao gerar PDF: " + e.getMessage());
            }
            return;
        }

        // formato inválido
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato inválido (use csv ou pdf)");
    }

    // ----------------------------
    // GERA DOCUMENTAÇÃO: FICHA DE PARCEIRO (USANDO APENAS CAMPOS DO MODEL DOADOR)
    // ----------------------------
    @GetMapping("/documentacao/{id}")
    public void gerarDocumentacaoDoador(@PathVariable("id") Integer idDoador,
                                        HttpServletResponse response) throws IOException {
        Doador d = doadorService.findById(idDoador);
        if (d == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Doador não encontrado");
            return;
        }

        response.setContentType("application/pdf");
        String filename = "ficha_parceiro_" + (d.getNome() != null ? d.getNome().replaceAll("\\s+", "_") : idDoador) + ".pdf";
        response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");

        Document document = new Document(PageSize.A4, 50, 50, 70, 50);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            PdfContentByte canvas = writer.getDirectContent();

            // ---------- CABEÇALHO: logo | título | número ----------
            PdfPTable headerTable = new PdfPTable(3);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1f, 4f, 1f});
            headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            headerTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

            // Logo (esquerda)
            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            String logoPath = "static/img/assandef-logo.png";
            java.net.URL logoUrl = getClass().getClassLoader().getResource(logoPath);
            if (logoUrl != null) {
                com.lowagie.text.Image logo = com.lowagie.text.Image.getInstance(logoUrl);
                logo.scaleToFit(90, 45);
                logoCell.addElement(logo);
            }
            headerTable.addCell(logoCell);

            // Título (centro)
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
            Paragraph titlePar = new Paragraph("FICHA DE PARCEIRO DA PROJETO SOS ASSANDEF", titleFont);
            titlePar.setAlignment(Element.ALIGN_CENTER);
            PdfPCell titleCell = new PdfPCell();
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            titleCell.addElement(titlePar);
            headerTable.addCell(titleCell);

            // Número do documento (direita) — evito sobrepor título
            Font numFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Paragraph numPar = new Paragraph("Nº: " + (34 + (idDoador != null ? idDoador : 0)), numFont);
            numPar.setAlignment(Element.ALIGN_RIGHT);
            PdfPCell numCell = new PdfPCell();
            numCell.setBorder(Rectangle.NO_BORDER);
            numCell.setVerticalAlignment(Element.ALIGN_TOP);
            numCell.addElement(numPar);
            headerTable.addCell(numCell);

            document.add(headerTable);

            // pequeno separador
            document.add(Chunk.NEWLINE);

            // ---------- TABELA DE DADOS (labels à esquerda, valores à direita) ----------
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.DARK_GRAY);
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(90);
            table.setWidths(new float[]{3f, 7f});
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            table.getDefaultCell().setPaddingBottom(6f);

            // helper inline: adiciona par de células
            java.util.function.BiConsumer<String, String> addRowLocal = (label, value) -> {
                PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
                c1.setBorder(Rectangle.NO_BORDER);
                c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
                c1.setPaddingBottom(8f);
                PdfPCell c2 = new PdfPCell(new Phrase(value != null ? value : "", valueFont));
                c2.setBorder(Rectangle.NO_BORDER);
                c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
                c2.setPaddingBottom(8f);
                table.addCell(c1);
                table.addCell(c2);
            };

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            addRowLocal.accept("Nome:", orEmpty(d.getNome()));
            addRowLocal.accept("CPF/CNPJ:", orEmpty(d.getCpfCnpj()));
            addRowLocal.accept("E-mail:", orEmpty(d.getEmail()));
            addRowLocal.accept("Telefone:", orEmpty(d.getTelefone()));
            addRowLocal.accept("Sexo:", orEmpty(d.getSexo()));
            addRowLocal.accept("Endereço:", orEmpty(d.getEndereco()));
            addRowLocal.accept("Data de Nascimento:", d.getDataNascimento() != null ? d.getDataNascimento().format(dtf) : "");
            addRowLocal.accept("Data de Cadastro:", d.getDataCadastro() != null ? d.getDataCadastro().format(dtf) : "");
            addRowLocal.accept("Valor inicial da contribuição:", formatCurrency(d.getMensalidade()));
            addRowLocal.accept("Dia para cobrança:", d.getDiaVencimento() != null ? String.valueOf(d.getDiaVencimento()) : "");

            document.add(table);

            // ---------- Data de emissão e espaço para assinatura ----------
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            PdfPTable bottomTable = new PdfPTable(2);
            bottomTable.setWidthPercentage(90);
            bottomTable.setWidths(new float[]{1f, 1f});
            bottomTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            // Data de Emissão (esquerda)
            PdfPCell dateCell = new PdfPCell(new Phrase("Data de Emissão: " + LocalDate.now().format(dtf), valueFont));
            dateCell.setBorder(Rectangle.NO_BORDER);
            dateCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            bottomTable.addCell(dateCell);

            // Espaço para assinatura (direita) — deixo linha grande
            PdfPCell signCell = new PdfPCell();
            signCell.setBorder(Rectangle.NO_BORDER);
            signCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            // assinatura label centralizado e linha abaixo
            Paragraph signPar = new Paragraph();
            signPar.setAlignment(Element.ALIGN_CENTER);
            signPar.add(new Phrase("\n\n_______________________________\n", valueFont)); // linha
            signPar.add(new Phrase("Assinatura do Contribuinte", labelFont));
            signCell.addElement(signPar);
            bottomTable.addCell(signCell);

            document.add(bottomTable);

            // ---------- Rodapé missão ----------
            document.add(Chunk.NEWLINE);
            Paragraph footer = new Paragraph();
            footer.setFont(FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.add("Missão: Promover ações e serviços que mobilizem a sociedade, visando a inclusão das\n");
            footer.add("Pessoas Com Deficiência na conquista de sua plena cidadania.\n");
            footer.add("\"Seja um parceiro associado da Assandef.\"");
            document.add(footer);

            document.close();
        } catch (Exception e) {
            document.close();
            response.reset();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao gerar documento: " + e.getMessage());
        }
    }   

    // ----------------------------
    // Helpers CSV / PDF tables
    // ----------------------------
    private String buildCsvEstoque(List<Material> materiais) {
        StringBuilder sb = new StringBuilder();
        sb.append("Nome do Material;Categoria;Quantidade;Validade;Fornecedor\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Material m : materiais) {
            String validade = m.getDataValidade() != null ? m.getDataValidade().format(dtf) : "";
            sb.append(escapeCsv(m.getNome())).append(';')
                    .append(escapeCsv(m.getCategoria() != null ? m.getCategoria().getNome() : "")).append(';')
                    .append(m.getQuantidadeAtual() != null ? m.getQuantidadeAtual() : 0).append(';')
                    .append(escapeCsv(validade)).append(';')
                    .append(escapeCsv(m.getFornecedor())).append('\n');
        }
        return sb.toString();
    }

    private String buildCsvSolicitacoes(List<SolicitacoesMaterial> solicitacoes) {
        StringBuilder sb = new StringBuilder();
        sb.append("Material;Quantidade;Solicitante;Data;Tipo de Saída;Status;Justificativa\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (SolicitacoesMaterial s : solicitacoes) {
            String data = s.getDataSolicitacao() != null ? s.getDataSolicitacao().format(dtf) : "";
            sb.append(escapeCsv(s.getMaterial() != null ? s.getMaterial().getNome() : "")).append(';')
                    .append(s.getQuantidadeSolicitada() != null ? s.getQuantidadeSolicitada() : 0).append(';')
                    .append(escapeCsv(s.getFuncionarioSolicitante() != null ? s.getFuncionarioSolicitante().getNomeCompleto() : "")).append(';')
                    .append(escapeCsv(data)).append(';')
                    .append(escapeCsv(s.getTipoSaida())).append(';')
                    .append(escapeCsv(s.getStatus() != null ? s.getStatus().name() : "")).append(';')
                    .append(escapeCsv(s.getDescricao())).append('\n');
        }
        return sb.toString();
    }

    private String buildCsvDoadores(List<Doador> doadores) {
        StringBuilder sb = new StringBuilder();
        sb.append("Nome;CPF/CNPJ;Telefone;Email;Mensalidade;Data de Nascimento;Sexo;Data Cadastrada\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Doador d : doadores) {
            sb.append(escapeCsv(d.getNome())).append(';')
                    .append(escapeCsv(d.getCpfCnpj())).append(';')
                    .append(escapeCsv(d.getTelefone())).append(';')
                    .append(escapeCsv(d.getEmail())).append(';')
                    .append(d.getMensalidade() != null ? String.format("%.2f", d.getMensalidade()) : "").append(';')
                    .append(d.getDataNascimento() != null ? d.getDataNascimento().format(dtf) : "").append(';')
                    .append(escapeCsv(d.getSexo())).append(';')
                    .append(d.getDataCadastro() != null ? d.getDataCadastro().format(dtf) : "").append('\n');
        }
        return sb.toString();
    }

    private String buildCsvAtendimentos(List<Atendimento> atendimentos) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID;Paciente;CPF Paciente;Profissional;Tipo Encaminhamento;Data/Hora Início;Data/Hora Fim;Status\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Atendimento a : atendimentos) {
            sb.append(a.getIdAtendimento()).append(';')
                    .append(escapeCsv(a.getPaciente() != null ? a.getPaciente().getNomeCompleto() : "")).append(';')
                    .append(escapeCsv(a.getPaciente() != null ? a.getPaciente().getCpf() : "")).append(';')
                    .append(escapeCsv(a.getFuncionario() != null ? a.getFuncionario().getNomeCompleto() : "")).append(';')
                    .append(escapeCsv(a.getTipoEncaminhamento())).append(';')
                    .append(a.getDataHoraInicio() != null ? a.getDataHoraInicio().format(dtf) : "").append(';')
                    .append(a.getDataHoraFim() != null ? a.getDataHoraFim().format(dtf) : "").append(';')
                    .append(escapeCsv(a.getStatus())).append('\n');
        }
        return sb.toString();
    }
    // Helper para tabela de dados do paciente
    private void addRowPaciente(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(5f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "", valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(5f);
        table.addCell(valueCell);
    }

    // Helper para tabela de atendimento
    private void addRowAtendimento(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(5f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "", valueFont));
        valueCell.setPadding(5f);
        table.addCell(valueCell);
    }
    private String formatTelefonesPaciente(Paciente paciente) {
        if (paciente.getTelefones() == null || paciente.getTelefones().isEmpty()) {
            return "";
        }

        return paciente.getTelefones().stream()
                .filter(t -> t != null && t.getNumero() != null && !t.getNumero().isBlank())
                .map(t -> {
                    String numero = t.getNumero();
                    String desc = t.getDescricao(); // ajuste o nome do getter se for diferente
                    if (desc != null && !desc.isBlank()) {
                        return numero + " (" + desc + ")";
                    }
                    return numero;
                })
                .collect(Collectors.joining(" | "));  // separador entre telefones
    }

    // -------- CSV do relatório do paciente --------
    private String buildCsvRelatorioPaciente(Paciente paciente, List<Atendimento> atendimentos) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter dtfData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dtfDataHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Cabeçalho de dados do paciente
        sb.append("RELATÓRIO DO PACIENTE").append("\n\n");
        sb.append("Nome;").append(escapeCsv(paciente.getNomeCompleto())).append('\n');
        sb.append("CPF;").append(escapeCsv(paciente.getCpf())).append('\n');

        // monta string com TODOS os telefones do paciente
        String telefonesStr = "";
        if (paciente.getTelefones() != null && !paciente.getTelefones().isEmpty()) {
            telefonesStr = paciente.getTelefones().stream()
                    .map(Telefone::getNumero)                    // <-- ajuste aqui se o getter tiver outro nome
                    .filter(t -> t != null && !t.isBlank())
                    .collect(Collectors.joining(", "));
        }
        sb.append("Telefones;").append(escapeCsv(telefonesStr)).append('\n');

        sb.append("Endereço;").append(escapeCsv(paciente.getEndereco())).append('\n');
        sb.append("\n");

        // Cabeçalho de atendimentos + evoluções + prescrições
        sb.append("ID Atendimento;Profissional;Tipo Encaminhamento;Data/Hora Início;Data/Hora Fim;Status;")
                .append("Evolução;Data Evolução;Prescrição Tipo;Prescrição Descrição\n");

        for (Atendimento a : atendimentos) {
            String idAt = a.getIdAtendimento() != null ? a.getIdAtendimento().toString() : "";
            String prof = escapeCsv(a.getFuncionario() != null ? a.getFuncionario().getNomeCompleto() : "");
            String tipoEnc = escapeCsv(a.getTipoEncaminhamento());
            String dtIni = a.getDataHoraInicio() != null ? a.getDataHoraInicio().format(dtfDataHora) : "";
            String dtFim = a.getDataHoraFim() != null ? a.getDataHoraFim().format(dtfDataHora) : "";
            String status = escapeCsv(a.getStatus());

            if (a.getEvolucoes() == null || a.getEvolucoes().isEmpty()) {
                // Linha simples sem evolução
                sb.append(idAt).append(';')
                        .append(prof).append(';')
                        .append(tipoEnc).append(';')
                        .append(dtIni).append(';')
                        .append(dtFim).append(';')
                        .append(status).append(';')
                        .append("Sem evoluções;;;;")
                        .append('\n');
            } else {
                for (Evolucao ev : a.getEvolucoes()) {
                    String descEv = escapeCsv(ev.getDescricao());
                    String dtEv = ev.getDataHoraRegistro() != null ? ev.getDataHoraRegistro().format(dtfDataHora) : "";

                    if (ev.getPrescricoes() == null || ev.getPrescricoes().isEmpty()) {
                        // Evolução sem prescrição
                        sb.append(idAt).append(';')
                                .append(prof).append(';')
                                .append(tipoEnc).append(';')
                                .append(dtIni).append(';')
                                .append(dtFim).append(';')
                                .append(status).append(';')
                                .append(descEv).append(';')
                                .append(dtEv).append(';')
                                .append("Sem prescrições;;")
                                .append('\n');
                    } else {
                        for (Prescricao p : ev.getPrescricoes()) {
                            String tipoPr = escapeCsv(p.getTipo());
                            String descPr = escapeCsv(p.getDescricao());

                            sb.append(idAt).append(';')
                                    .append(prof).append(';')
                                    .append(tipoEnc).append(';')
                                    .append(dtIni).append(';')
                                    .append(dtFim).append(';')
                                    .append(status).append(';')
                                    .append(descEv).append(';')
                                    .append(dtEv).append(';')
                                    .append(tipoPr).append(';')
                                    .append(descPr)
                                    .append('\n');
                        }
                    }
                }
            }
        }

        return sb.toString();
    }
    // ----------------------------
// RELATÓRIO INDIVIDUAL DO PACIENTE (PDF ou CSV)
// ----------------------------
    @GetMapping("/paciente/{idPaciente}")
    public void gerarRelatorioPaciente(
            @PathVariable Integer idPaciente,
            @RequestParam(defaultValue = "pdf") String formato,
            HttpServletResponse response) throws IOException {

        // Buscar paciente
        Paciente paciente = pacienteService.findById(idPaciente);
        if (paciente == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Paciente não encontrado");
            return;
        }

        // Buscar todos os atendimentos desse paciente
        List<Atendimento> atendimentos = atendimentoService.findAll().stream()
                .filter(a -> a.getPaciente() != null && a.getPaciente().getIdPaciente().equals(idPaciente))
                .sorted((a1, a2) -> {
                    if (a1.getDataHoraInicio() == null) return 1;
                    if (a2.getDataHoraInicio() == null) return -1;
                    return a2.getDataHoraInicio().compareTo(a1.getDataHoraInicio()); // mais recente primeiro
                })
                .toList();

        // ---------------- CSV ----------------
        if ("csv".equalsIgnoreCase(formato)) {
            String csv = buildCsvRelatorioPaciente(paciente, atendimentos);
            String filename = "relatorio_paciente_" +
                    (paciente.getNomeCompleto() != null ? paciente.getNomeCompleto().replaceAll("\\s+", "_") : idPaciente) +
                    "_" + LocalDate.now() + ".csv";

            byte[] bytes = ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8); // BOM para Excel
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
            return;
        }

        // ---------------- PDF ----------------
        if ("pdf".equalsIgnoreCase(formato)) {
            String filename = "relatorio_paciente_" +
                    (paciente.getNomeCompleto() != null ? paciente.getNomeCompleto().replaceAll("\\s+", "_") : idPaciente) +
                    "_" + LocalDate.now() + ".pdf";

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");

            Document document = new Document(PageSize.A4, 40, 40, 60, 50);
            try {
                PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());

                // Cabeçalho/Rodapé
                writer.setPageEvent(new PdfPageEventHelper() {
                    public void onEndPage(PdfWriter writer, Document document) {
                        try {
                            String logoPath = "static/img/assandef-logo.png";
                            java.net.URL logoUrl = getClass().getClassLoader().getResource(logoPath);
                            if (logoUrl != null) {
                                com.lowagie.text.Image logo = com.lowagie.text.Image.getInstance(logoUrl);
                                logo.scaleToFit(60, 30);
                                logo.setAbsolutePosition(40, document.getPageSize().getHeight() - 50);
                                writer.getDirectContent().addImage(logo);
                            }

                            ColumnText.showTextAligned(
                                    writer.getDirectContent(),
                                    Element.ALIGN_CENTER,
                                    new Phrase("RELATÓRIO DE ATENDIMENTOS DO PACIENTE",
                                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)),
                                    (document.right() + document.left()) / 2,
                                    document.getPageSize().getHeight() - 40,
                                    0
                            );

                            ColumnText.showTextAligned(
                                    writer.getDirectContent(),
                                    Element.ALIGN_CENTER,
                                    new Phrase("Página " + writer.getPageNumber(),
                                            FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY)),
                                    (document.right() + document.left()) / 2,
                                    20,
                                    0
                            );
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                document.open();

                DateTimeFormatter dtfData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter dtfDataHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                // ===== DADOS DO PACIENTE =====
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(70, 130, 180));
                Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
                Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);

                Paragraph titulo = new Paragraph("Dados do Paciente", titleFont);
                titulo.setSpacingAfter(10f);
                document.add(titulo);

                PdfPTable dadosPaciente = new PdfPTable(2);
                dadosPaciente.setWidthPercentage(100);
                dadosPaciente.setWidths(new float[]{1.5f, 3f});
                dadosPaciente.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                dadosPaciente.getDefaultCell().setPaddingBottom(5f);

                addRowPaciente(dadosPaciente, "Nome:", orEmpty(paciente.getNomeCompleto()), labelFont, valueFont);
                addRowPaciente(dadosPaciente, "CPF:", orEmpty(paciente.getCpf()), labelFont, valueFont);
                addRowPaciente(dadosPaciente, "Data de Nascimento:",
                        paciente.getDataNascimento() != null ? paciente.getDataNascimento().format(dtfData) : "",
                        labelFont, valueFont);
                addRowPaciente(dadosPaciente, "Telefone(s):", formatTelefonesPaciente(paciente), labelFont, valueFont);
                addRowPaciente(dadosPaciente, "Endereço:", orEmpty(paciente.getEndereco()), labelFont, valueFont);

                document.add(dadosPaciente);
                document.add(Chunk.NEWLINE);

                // ===== RESUMO =====
                Paragraph tituloResumo = new Paragraph("Resumo de Atendimentos", titleFont);
                tituloResumo.setSpacingAfter(10f);
                document.add(tituloResumo);

                long totalAtendimentos = atendimentos.size();
                long atendFinal = atendimentos.stream().filter(a -> "FINALIZADO".equals(a.getStatus())).count();
                long atendAndam = atendimentos.stream().filter(a -> "EM_ANDAMENTO".equals(a.getStatus())).count();

                Paragraph resumo = new Paragraph();
                resumo.setFont(valueFont);
                resumo.add("Total de atendimentos: " + totalAtendimentos + "\n");
                resumo.add("Finalizados: " + atendFinal + "\n");
                resumo.add("Em andamento: " + atendAndam + "\n");
                resumo.setSpacingAfter(10f);
                document.add(resumo);

                document.add(Chunk.NEWLINE);

                // ===== HISTÓRICO DETALHADO =====
                Paragraph tituloDet = new Paragraph("Histórico Detalhado de Atendimentos", titleFont);
                tituloDet.setSpacingAfter(10f);
                document.add(tituloDet);

                if (atendimentos.isEmpty()) {
                    Paragraph sem = new Paragraph("Nenhum atendimento registrado para este paciente.", valueFont);
                    sem.setSpacingAfter(10f);
                    document.add(sem);
                } else {
                    int i = 1;
                    for (Atendimento a : atendimentos) {
                        // Cabeçalho do atendimento
                        PdfPTable headerAt = new PdfPTable(1);
                        headerAt.setWidthPercentage(100);
                        PdfPCell hCell = new PdfPCell(new Phrase(
                                "Atendimento #" + i + " - ID: " + a.getIdAtendimento(),
                                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
                        hCell.setBackgroundColor(new Color(70, 130, 180));
                        hCell.setPadding(8f);
                        headerAt.addCell(hCell);
                        document.add(headerAt);

                        // Tabela com dados
                        PdfPTable tabelaAt = new PdfPTable(2);
                        tabelaAt.setWidthPercentage(100);
                        tabelaAt.setWidths(new float[]{1.5f, 3f});
                        tabelaAt.getDefaultCell().setPadding(5f);

                        addRowAtendimento(tabelaAt, "Profissional:",
                                orEmpty(a.getFuncionario() != null ? a.getFuncionario().getNomeCompleto() : null),
                                labelFont, valueFont);
                        addRowAtendimento(tabelaAt, "Tipo de Encaminhamento:", orEmpty(a.getTipoEncaminhamento()),
                                labelFont, valueFont);
                        addRowAtendimento(tabelaAt, "Data/Hora Início:",
                                a.getDataHoraInicio() != null ? a.getDataHoraInicio().format(dtfDataHora) : "",
                                labelFont, valueFont);
                        addRowAtendimento(tabelaAt, "Data/Hora Fim:",
                                a.getDataHoraFim() != null ? a.getDataHoraFim().format(dtfDataHora) : "Em andamento",
                                labelFont, valueFont);
                        addRowAtendimento(tabelaAt, "Status:", orEmpty(a.getStatus()), labelFont, valueFont);

                        document.add(tabelaAt);

                        // Evoluções + prescrições
                        if (a.getEvolucoes() != null && !a.getEvolucoes().isEmpty()) {
                            document.add(Chunk.NEWLINE);
                            Paragraph tEvol = new Paragraph("   Evoluções e Prescrições:",
                                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(50, 100, 150)));
                            tEvol.setSpacingBefore(5f);
                            tEvol.setSpacingAfter(5f);
                            document.add(tEvol);

                            int j = 1;
                            for (Evolucao ev : a.getEvolucoes()) {
                                PdfPTable tabEv = new PdfPTable(1);
                                tabEv.setWidthPercentage(95);
                                tabEv.setSpacingBefore(5f);

                                PdfPCell evHead = new PdfPCell(new Phrase(
                                        "Evolução #" + j + " - " +
                                                (ev.getDataHoraRegistro() != null ? ev.getDataHoraRegistro().format(dtfDataHora) : ""),
                                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
                                evHead.setBackgroundColor(new Color(100, 150, 200));
                                evHead.setPadding(5f);
                                tabEv.addCell(evHead);

                                PdfPCell evBody = new PdfPCell(new Phrase(orEmpty(ev.getDescricao()),
                                        FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK)));
                                evBody.setPadding(8f);
                                evBody.setBackgroundColor(new Color(240, 248, 255));
                                tabEv.addCell(evBody);

                                document.add(tabEv);

                                // Prescrições
                                if (ev.getPrescricoes() != null && !ev.getPrescricoes().isEmpty()) {
                                    PdfPTable tabPr = new PdfPTable(2);
                                    tabPr.setWidthPercentage(90);
                                    tabPr.setWidths(new float[]{1f, 4f});
                                    tabPr.setSpacingBefore(3f);

                                    for (Prescricao p : ev.getPrescricoes()) {
                                        PdfPCell tipoCell = new PdfPCell(new Phrase(
                                                orEmpty(p.getTipo()),
                                                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE)));
                                        tipoCell.setBackgroundColor(new Color(34, 139, 34));
                                        tipoCell.setPadding(5f);
                                        tipoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                        tabPr.addCell(tipoCell);

                                        PdfPCell descCell = new PdfPCell(new Phrase(orEmpty(p.getDescricao()),
                                                FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK)));
                                        descCell.setPadding(5f);
                                        descCell.setBackgroundColor(new Color(240, 255, 240));
                                        tabPr.addCell(descCell);
                                    }

                                    document.add(tabPr);
                                }

                                j++;
                            }
                        } else {
                            Paragraph semEv = new Paragraph("   Nenhuma evolução registrada para este atendimento.",
                                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY));
                            semEv.setSpacingBefore(5f);
                            document.add(semEv);
                        }

                        document.add(Chunk.NEWLINE);
                        document.add(Chunk.NEWLINE);
                        i++;
                    }
                }

                Paragraph rodape = new Paragraph();
                rodape.setFont(FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY));
                rodape.setAlignment(Element.ALIGN_CENTER);
                rodape.add("\n\nRelatório gerado em: " + LocalDate.now().format(dtfData));
                rodape.add("\nAssandef - Associação Santanense do Deficiente Físico");
                document.add(rodape);

                document.close();
            } catch (Exception e) {
                document.close();
                response.reset();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao gerar relatório: " + e.getMessage());
            }
            return;
        }

        // Se não for pdf nem csv
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato inválido (use pdf ou csv)");
    }

    @GetMapping("/ficha-atendimento")
    public void gerarFichaAtendimento(
            @RequestParam(required = false) String medico,
            @RequestParam(required = false) String posto,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            HttpServletResponse response) throws IOException {

        LocalDate dataRef = (data != null) ? data : LocalDate.now();
        String postoFinal = (posto != null && !posto.isBlank()) ? posto : "ASSANDEF";

        // Busca atendimentos do dia (00:00 até 23:59 do mesmo dia)
        List<Atendimento> atendimentosDoDia = atendimentoService
                .findByDataHoraInicioBetween(
                        dataRef.atStartOfDay(),
                        dataRef.plusDays(1).atStartOfDay()
                );

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dataStr = dataRef.format(dtf);

        response.setContentType("application/pdf");
        String filename = "ficha_atendimento_" + dataRef + ".pdf";
        response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");

        Document document = new Document(PageSize.A4, 40, 40, 60, 50);
        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            // ===== LOGO ASSANDEF NO TOPO =====
            String logoPath = "static/img/brasao.png";
            java.net.URL logoUrl = getClass().getClassLoader().getResource(logoPath);
            if (logoUrl != null) {
                Image logo = Image.getInstance(logoUrl);
                logo.scaleToFit(90, 45);
                logo.setAlignment(Image.ALIGN_CENTER);
                document.add(logo);
            }

            // ===== TÍTULOS =====
            Font fontCabecalho = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);

            Paragraph prefeitura = new Paragraph("PREFEITURA MUNICIPAL DE SANT'ANA DO LIVRAMENTO", fontCabecalho);
            prefeitura.setAlignment(Element.ALIGN_CENTER);
            document.add(prefeitura);

            Paragraph folha = new Paragraph("FOLHA DE PRODUÇÃO", fontTitulo);
            folha.setSpacingBefore(5f);
            folha.setSpacingAfter(15f);
            folha.setAlignment(Element.ALIGN_CENTER);
            document.add(folha);

            // ===== LINHA MÉDICO / DATA / POSTO =====
            Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);

            PdfPTable infoTable = new PdfPTable(new float[]{3f, 2f, 3f});
            infoTable.setWidthPercentage(100);
            infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell medicoCell = new PdfPCell(new Phrase("Médico: " +
                    (medico != null && !medico.isBlank() ? medico : "____________________"), fontLabel));
            medicoCell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(medicoCell);

            PdfPCell dataCell = new PdfPCell(new Phrase("Data: " + dataStr, fontLabel));
            dataCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            dataCell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(dataCell);

            PdfPCell postoCell = new PdfPCell(new Phrase("Posto: " + postoFinal, fontLabel));
            postoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            postoCell.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(postoCell);

            document.add(infoTable);

            document.add(Chunk.NEWLINE);

            // ===== TABELA PRINCIPAL (Nº, Nome do Paciente, Endereço, Assinatura) =====
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            PdfPTable tabela = new PdfPTable(new float[]{0.7f, 3f, 3f, 3f});
            tabela.setWidthPercentage(100);

            String[] headers = {"Nº", "Nome do Paciente", "Endereço", "Assinatura"};
            for (String h : headers) {
                PdfPCell hCell = new PdfPCell(new Phrase(h, headerFont));
                hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                hCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                hCell.setPadding(5f);
                tabela.addCell(hCell);
            }

            // Vamos preencher até 16 linhas: uma por atendimento (se existir), o resto vazio
            int maxLinhas = 16;
            for (int i = 1; i <= maxLinhas; i++) {
                // Atendimento correspondente à linha (índice i-1 na lista)
                Atendimento atendimento = (i <= atendimentosDoDia.size()) ? atendimentosDoDia.get(i - 1) : null;

                String nomePaciente = "";
                String enderecoPaciente = "";

                if (atendimento != null && atendimento.getPaciente() != null) {
                    nomePaciente = orEmpty(atendimento.getPaciente().getNomeCompleto());
                    enderecoPaciente = orEmpty(atendimento.getPaciente().getEndereco());
                }

                // Coluna Nº
                PdfPCell numCell = new PdfPCell(new Phrase(String.valueOf(i), cellFont));
                numCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                numCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                numCell.setMinimumHeight(20f);
                tabela.addCell(numCell);

                // Coluna Nome do Paciente
                PdfPCell nomeCell = new PdfPCell(new Phrase(nomePaciente, cellFont));
                nomeCell.setMinimumHeight(20f);
                tabela.addCell(nomeCell);

                // Coluna Endereço
                PdfPCell endCell = new PdfPCell(new Phrase(enderecoPaciente, cellFont));
                endCell.setMinimumHeight(20f);
                tabela.addCell(endCell);

                // Coluna Assinatura (sempre vazia)
                PdfPCell assCell = new PdfPCell(new Phrase("", cellFont));
                assCell.setMinimumHeight(20f);
                tabela.addCell(assCell);
            }

            document.add(tabela);

            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            // ===== RODAPÉ COM ASSINATURAS =====
            PdfPTable assinaturaTable = new PdfPTable(2);
            assinaturaTable.setWidthPercentage(100);
            assinaturaTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            PdfPCell funcCell = new PdfPCell();
            funcCell.setBorder(Rectangle.NO_BORDER);
            funcCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            Paragraph funcPar = new Paragraph("_______________________________\nFuncionário Responsável", footerFont);
            funcPar.setAlignment(Element.ALIGN_CENTER);
            funcCell.addElement(funcPar);
            assinaturaTable.addCell(funcCell);

            PdfPCell medAssCell = new PdfPCell();
            medAssCell.setBorder(Rectangle.NO_BORDER);
            medAssCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            Paragraph medPar = new Paragraph("_______________________________\nAssinatura do Médico", footerFont);
            medPar.setAlignment(Element.ALIGN_CENTER);
            medAssCell.addElement(medPar);
            assinaturaTable.addCell(medAssCell);

            document.add(assinaturaTable);

            document.close();
        } catch (Exception e) {
            document.close();
            response.reset();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Erro ao gerar ficha de atendimento: " + e.getMessage());
        }
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private String orEmpty(Object o) {
        return o == null ? "" : o.toString();
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "";
        return "R$ " + value.setScale(2, RoundingMode.HALF_UP).toString().replace(".", ",");
    }

    // --- Helpers PDF table header ---
    private void addTableHeader(PdfPTable table, String[] headers) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(new Color(70, 130, 180)); // azul moderno
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPaddingTop(8f);
            cell.setPaddingBottom(8f);
            cell.setMinimumHeight(26f);
            table.addCell(cell);
        }
        table.setHeaderRows(1);
    }

    // helper para criar células do corpo com mais espaço e wrap amigável
    private PdfPCell createBodyCell(String text) {
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        Phrase phrase = new Phrase();
        phrase.setLeading(14f);
        phrase.add(new Chunk(text == null ? "" : text, bodyFont));
        PdfPCell cell = new PdfPCell(phrase);
        cell.setPaddingTop(6f);
        cell.setPaddingBottom(6f);
        cell.setPaddingLeft(4f);
        cell.setPaddingRight(4f);
        cell.setMinimumHeight(20f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setNoWrap(false);
        return cell;
    }
}
