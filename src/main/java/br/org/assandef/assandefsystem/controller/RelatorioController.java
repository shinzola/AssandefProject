package br.org.assandef.assandefsystem.controller;

import br.org.assandef.assandefsystem.model.Doador;
import br.org.assandef.assandefsystem.model.Material;
import br.org.assandef.assandefsystem.model.SolicitacoesMaterial;
import br.org.assandef.assandefsystem.service.DoadorService;
import br.org.assandef.assandefsystem.service.MaterialService;
import br.org.assandef.assandefsystem.service.SolicitacoesMaterialService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.util.function.BiConsumer;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping({"/almoxarifado/relatorio", "/doadores/relatorio"})
public class RelatorioController {

    private final MaterialService materialService;
    private final SolicitacoesMaterialService solicitacoesMaterialService;
    private final DoadorService doadorService;

    public RelatorioController(MaterialService materialService,
                               SolicitacoesMaterialService solicitacoesMaterialService,
                               DoadorService doadorService) {
        this.materialService = materialService;
        this.solicitacoesMaterialService = solicitacoesMaterialService;
        this.doadorService = doadorService;
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

        return List.of();
    }

    // ----------------------------
    // POST gerar: gera CSV ou PDF e retorna como download
    // ----------------------------
    @PostMapping("/gerar")
    public void gerarRelatorio(
            @RequestParam String tipo,
            @RequestParam String formato,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            HttpServletResponse response) throws IOException {

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
            String filename = "relatorio_" + tipo + "_" + LocalDate.now() + ".pdf";
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
                                    new Phrase("Relatório de " + tipo.toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)),
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