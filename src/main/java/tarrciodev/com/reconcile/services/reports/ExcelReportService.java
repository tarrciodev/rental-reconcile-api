package tarrciodev.com.reconcile.services.reports;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import tarrciodev.com.reconcile.DTO.ReconciliationReportDTO;

@Service
public class ExcelReportService {

    // ✅ FORMATADOR PARA TIMESTAMP
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // ✅ MÉTODO ORIGINAL (mantido para compatibilidade)
    public String generateExcelReport(ReconciliationReportDTO report) throws IOException {
        // Gera nome único com timestamp
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String fileName = "reconciliation_report_" + timestamp + ".xlsx";
        return generateExcelReport(report, fileName);
    }

    // ✅ NOVO MÉTODO QUE ACEITA NOME PERSONALIZADO
    public String generateExcelReport(ReconciliationReportDTO report, String fileName) throws IOException {
        // Criar diretório upload se não existir
        Path uploadDir = Paths.get("upload");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path filePath = uploadDir.resolve(fileName);

        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(filePath.toFile())) {
            
            Sheet sheet = workbook.createSheet("Reconciliação Bancária");
            
            // Estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle boldStyle = createBoldStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            
            int rowNum = 0;
            
            // Título
            rowNum = createTitle(sheet, rowNum, titleStyle, report);
            rowNum++;
            
            // Informações da Empresa
            rowNum = createCompanyInfo(sheet, rowNum, report);
            rowNum += 2;
            
            // Saldo do Extrato Bancário
            rowNum = createBankBalance(sheet, rowNum, boldStyle, currencyStyle, report);
            rowNum += 2;
            
            // Movimentos não conciliados
            rowNum = createUnreconciledMovements(sheet, rowNum, headerStyle, boldStyle, currencyStyle, report);
            rowNum += 2;
            
            // Saldos Finais
            rowNum = createFinalBalances(sheet, rowNum, boldStyle, currencyStyle, report);
            rowNum += 2;
            
            // Estatísticas
            createStatistics(sheet, rowNum, boldStyle, report);
            
            // Auto-size columns
            for (int i = 0; i < 7; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            System.out.println("✅ Relatório Excel salvo em: " + filePath.toAbsolutePath());
            return filePath.toAbsolutePath().toString();
        }
    }

    // ... (todos os outros métodos permanecem EXATAMENTE iguais)
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createBoldStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("###,##0.00"));
        return style;
    }

    private int createTitle(Sheet sheet, int rowNum, CellStyle titleStyle, ReconciliationReportDTO report) {
        Row titleRow = sheet.createRow(rowNum);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("DOC.DO.RECB." + report.getDataReconciliacao().getYear() + "." + 
                               String.format("%02d", report.getDataReconciliacao().getMonthValue()));
        titleCell.setCellStyle(titleStyle);
        
        // Merge cells para o título
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 6));
        
        rowNum++;
        Row reconRow = sheet.createRow(rowNum);
        Cell reconCell = reconRow.createCell(0);
        reconCell.setCellValue("Reconciliação Bancária em " + 
            report.getDataReconciliacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 6));
        
        return rowNum + 2;
    }

    private int createCompanyInfo(Sheet sheet, int rowNum, ReconciliationReportDTO report) {
        Row empresaRow = sheet.createRow(rowNum++);
        empresaRow.createCell(0).setCellValue("Empresa: " + report.getEmpresa());
        
        Row bancoRow = sheet.createRow(rowNum++);
        bancoRow.createCell(0).setCellValue("Banco: " + report.getBanco());
        
        Row contaRow = sheet.createRow(rowNum++);
        contaRow.createCell(0).setCellValue("Conta: " + report.getConta());
        
        return rowNum;
    }

    private int createBankBalance(Sheet sheet, int rowNum, CellStyle boldStyle, CellStyle currencyStyle, ReconciliationReportDTO report) {
        Row balanceRow = sheet.createRow(rowNum++);
        Cell balanceCell = balanceRow.createCell(0);
        balanceCell.setCellValue("0 - Saldo do Extrato Bancário (se devedor considerar -) .......................................................................................................");
        balanceCell.setCellStyle(boldStyle);
        
        Row valueRow = sheet.createRow(rowNum++);
        Cell valueCell = valueRow.createCell(6);
        valueCell.setCellValue(report.getSaldoExtratoBancario());
        valueCell.setCellStyle(currencyStyle);
        
        return rowNum;
    }

    private int createUnreconciledMovements(Sheet sheet, int rowNum, CellStyle headerStyle, CellStyle boldStyle, CellStyle currencyStyle, ReconciliationReportDTO report) {
        // 1 - Débitos Banco não contabilizados
        rowNum = createMovementSection(sheet, rowNum, headerStyle, boldStyle, currencyStyle, 
            "1 - Movimentos a débito no Banco que ainda não foram contabilizados pela Empresa :", 
            report.getDebitosBancoNaoContabilizados(), 
            report.getTotalDebitosBancoNaoContabilizados(), "(+)");

        // 2 - Créditos Banco não contabilizados
        rowNum = createMovementSection(sheet, rowNum, headerStyle, boldStyle, currencyStyle, 
            "2 - Movimentos a crédito no Banco que ainda não foram contabilizados pela Empresa :", 
            report.getCreditosBancoNaoContabilizados(), 
            report.getTotalCreditosBancoNaoContabilizados(), "(-)");

        // 3 - Débitos Empresa não contabilizados
        rowNum = createMovementSection(sheet, rowNum, headerStyle, boldStyle, currencyStyle, 
            "3 - Movimentos a débito na Empresa que ainda não foram contabilizados pelo Banco :", 
            report.getDebitosEmpresaNaoContabilizados(), 
            report.getTotalDebitosEmpresaNaoContabilizados(), "(+)");

        // 4 - Créditos Empresa não contabilizados
        rowNum = createMovementSection(sheet, rowNum, headerStyle, boldStyle, currencyStyle, 
            "4 - Movimentos a crédito na Empresa que ainda não foram contabilizados pelo Banco :", 
            report.getCreditosEmpresaNaoContabilizados(), 
            report.getTotalCreditosEmpresaNaoContabilizados(), "(-)");

        return rowNum;
    }

    private int createMovementSection(Sheet sheet, int rowNum, CellStyle headerStyle, CellStyle boldStyle, CellStyle currencyStyle, 
                                     String title, List<ReconciliationReportDTO.MovimentoNaoConciliado> movements, Double total, String signal) {
        // Título da seção
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(boldStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 6));

        // Cabeçalho da tabela
        if (!movements.isEmpty()) {
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Data", "Tipo Doc.", "N.º Doc.", "Descrição / Terceiro", "Valor", "", signal};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Dados dos movimentos
            for (ReconciliationReportDTO.MovimentoNaoConciliado movimento : movements) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(movimento.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                dataRow.createCell(1).setCellValue(movimento.getTipoDoc() != null ? movimento.getTipoDoc() : "");
                dataRow.createCell(2).setCellValue(movimento.getNumeroDoc() != null ? movimento.getNumeroDoc() : "");
                dataRow.createCell(3).setCellValue(movimento.getDescricaoTerceiro());
                
                Cell valueCell = dataRow.createCell(4);
                valueCell.setCellValue(movimento.getValor());
                valueCell.setCellStyle(currencyStyle);
                
                dataRow.createCell(5).setCellValue("");
                dataRow.createCell(6).setCellValue(signal);
            }
        } else {
            // Se não há movimentos, mostrar "0"
            Row emptyRow = sheet.createRow(rowNum++);
            emptyRow.createCell(4).setCellValue(0);
            emptyRow.createCell(6).setCellValue(signal);
        }

        // Total da seção
        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(5).setCellValue("Total:");
        
        Cell totalCell = totalRow.createCell(6);
        totalCell.setCellValue(total);
        totalCell.setCellStyle(currencyStyle);

        return rowNum + 1;
    }

    private int createFinalBalances(Sheet sheet, int rowNum, CellStyle boldStyle, CellStyle currencyStyle, ReconciliationReportDTO report) {
        // 5 - Saldo do Banco Conciliado
        Row conciliatedRow = sheet.createRow(rowNum++);
        Cell conciliatedCell = conciliatedRow.createCell(0);
        conciliatedCell.setCellValue("5 - Saldo do Banco Conciliado (0+1-2+3-4) .......................................................................................................");
        conciliatedCell.setCellStyle(boldStyle);
        
        Cell conciliatedValue = conciliatedRow.createCell(6);
        conciliatedValue.setCellValue(report.getSaldoBancoConciliado());
        conciliatedValue.setCellStyle(currencyStyle);

        // 6 - Saldo da Conta Corrente na Empresa
        Row empresaRow = sheet.createRow(rowNum++);
        Cell empresaCell = empresaRow.createCell(0);
        empresaCell.setCellValue("6 - Saldo da Conta Corrente na Empresa (se credor considerar -) .......................................................................................................");
        empresaCell.setCellStyle(boldStyle);
        
        Cell empresaValue = empresaRow.createCell(6);
        empresaValue.setCellValue(report.getSaldoContaCorrenteEmpresa());
        empresaValue.setCellStyle(currencyStyle);

        // 7 - Diferença
        Row diffRow = sheet.createRow(rowNum++);
        Cell diffCell = diffRow.createCell(0);
        diffCell.setCellValue("7 - Diferença (5-6) .............................................................................................................................................................................");
        diffCell.setCellStyle(boldStyle);
        
        Cell diffValue = diffRow.createCell(6);
        diffValue.setCellValue(report.getDiferenca());
        diffValue.setCellStyle(currencyStyle);

        return rowNum;
    }

    private void createStatistics(Sheet sheet, int rowNum, CellStyle boldStyle, ReconciliationReportDTO report) {
        Row statsTitle = sheet.createRow(rowNum++);
        statsTitle.createCell(0).setCellValue("ESTATÍSTICAS DA RECONCILIAÇÃO");
        statsTitle.getCell(0).setCellStyle(boldStyle);

        Row stats1 = sheet.createRow(rowNum++);
        stats1.createCell(0).setCellValue("Total de Transações Bancárias:");
        stats1.createCell(1).setCellValue(report.getTotalBankStatements());

        Row stats2 = sheet.createRow(rowNum++);
        stats2.createCell(0).setCellValue("Total de Transações da Empresa:");
        stats2.createCell(1).setCellValue(report.getTotalExcelTransactions());

        Row stats3 = sheet.createRow(rowNum++);
        stats3.createCell(0).setCellValue("Matches Encontrados:");
        stats3.createCell(1).setCellValue(report.getTotalMatches());

        Row stats4 = sheet.createRow(rowNum++);
        stats4.createCell(0).setCellValue("Taxa de Reconciliação:");
        stats4.createCell(1).setCellValue(report.getTaxaReconciliacao() + "%");
    }
}