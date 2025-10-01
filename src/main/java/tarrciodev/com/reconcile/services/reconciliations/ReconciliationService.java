package tarrciodev.com.reconcile.services.reconciliations;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tarrciodev.com.reconcile.DTO.ReconciliationReportDTO;
import tarrciodev.com.reconcile.DTO.ReconciliationResultDTO;
import tarrciodev.com.reconcile.DTO.ReconciliationSummaryDTO;
import tarrciodev.com.reconcile.entities.BankStatement;
import tarrciodev.com.reconcile.entities.ExcelTransaction;
import tarrciodev.com.reconcile.repositories.BankStatementRepository;
import tarrciodev.com.reconcile.repositories.ExcelTransactionRepository;
import tarrciodev.com.reconcile.services.reports.ExcelReportService;

@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final BankStatementRepository bankRepo;
    private final ExcelTransactionRepository excelRepo;
    private final DescriptionSimilarityService similarityService;
    private final ExcelReportService excelReportService;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private String generateUniqueReportName() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        return "reconciliation_report_" + timestamp + ".xlsx";
    }

    // === PRINCIPAL: gera reconciliação + relatório em Excel ===
    public ReconciliationResult generateReconciliation() {
        System.out.println("=== INICIANDO RECONCILIAÇÃO COMPLETA ===");

        List<BankStatement> bankStatements = bankRepo.findAll();
        List<ExcelTransaction> excelTransactions = excelRepo.findAll();

        if (bankStatements.isEmpty() || excelTransactions.isEmpty()) {
            throw new RuntimeException("Dados insuficientes para reconciliação.");
        }

        // 1 - Reconciliação
        List<ReconciliationResultDTO> matches = performReconciliation(bankStatements, excelTransactions);

        // 2 - Relatório
        ReconciliationReportDTO report = generateReconciliationReport(
            LocalDate.now(), "Minha Empresa", "Meu Banco", "12345-6",
            bankStatements, excelTransactions, matches
        );

        // 3 - Excel (tratando IOException)
        String excelFilePath;
        try {
            excelFilePath = excelReportService.generateExcelReport(report, generateUniqueReportName());
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar o relatório em Excel", e);
        }

        return new ReconciliationResult(matches, report, excelFilePath);
    }

    // === Gera resumo da reconciliação (1 item em lista) ===
    public List<ReconciliationSummaryDTO> reconcile() {
        List<BankStatement> bankStatements = bankRepo.findAll();
        List<ExcelTransaction> excelTransactions = excelRepo.findAll();

        List<ReconciliationResultDTO> matches = performReconciliation(bankStatements, excelTransactions);

        ReconciliationReportDTO report = generateReconciliationReport(
            LocalDate.now(), "Minha Empresa", "Meu Banco", "12345-6",
            bankStatements, excelTransactions, matches
        );

        String excelFilePath;
        try {
            excelFilePath = excelReportService.generateExcelReport(report, generateUniqueReportName());
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar o relatório em Excel", e);
        }
        String reportName = excelFilePath.substring(excelFilePath.lastIndexOf("/") + 1);

        int pendingTransactions = (bankStatements.size() + excelTransactions.size()) - (matches.size() * 2);

        ReconciliationSummaryDTO summary = new ReconciliationSummaryDTO(
            bankStatements.size(),
            excelTransactions.size(),
            matches.size(),
            pendingTransactions,
            reportName
        );

        return List.of(summary);
    }

    // === Reconciliação múltipla (lista de resumos) ===
    public List<ReconciliationSummaryDTO> reconcileMultiple() {
        List<BankStatement> bankStatements = bankRepo.findAll();
        List<ExcelTransaction> excelTransactions = excelRepo.findAll();

        List<ReconciliationResultDTO> matches = performReconciliation(bankStatements, excelTransactions);

        ReconciliationReportDTO report = generateReconciliationReport(
            LocalDate.now(), "Minha Empresa", "Meu Banco", "12345-6",
            bankStatements, excelTransactions, matches
        );

        String excelFilePath;
        try {
            excelFilePath = excelReportService.generateExcelReport(report, generateUniqueReportName());
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar o relatório em Excel", e);
        }
        String reportName = excelFilePath.substring(excelFilePath.lastIndexOf("/") + 1);

        int pendingTransactions = (bankStatements.size() + excelTransactions.size()) - (matches.size() * 2);

        ReconciliationSummaryDTO summary = new ReconciliationSummaryDTO(
            bankStatements.size(),
            excelTransactions.size(),
            matches.size(),
            pendingTransactions,
            reportName
        );

        List<ReconciliationSummaryDTO> summaries = new ArrayList<>();
        summaries.add(summary);
        return summaries;
    }

    // === Gera apenas o resumo ===
    public ReconciliationSummaryDTO generateReconciliationSummary() {
        List<BankStatement> bankStatements = bankRepo.findAll();
        List<ExcelTransaction> excelTransactions = excelRepo.findAll();

        List<ReconciliationResultDTO> matches = performReconciliation(bankStatements, excelTransactions);

        ReconciliationReportDTO report = generateReconciliationReport(
            LocalDate.now(), "Minha Empresa", "Meu Banco", "12345-6",
            bankStatements, excelTransactions, matches
        );

        String excelFilePath;
        try {
            excelFilePath = excelReportService.generateExcelReport(report, generateUniqueReportName());
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar o relatório em Excel", e);
        }
        String reportName = excelFilePath.substring(excelFilePath.lastIndexOf("/") + 1);

        int pendingTransactions = (bankStatements.size() + excelTransactions.size()) - (matches.size() * 2);

        return new ReconciliationSummaryDTO(
            bankStatements.size(),
            excelTransactions.size(),
            matches.size(),
            pendingTransactions,
            reportName
        );
    }

    // === Algoritmo de reconciliação ATUALIZADO ===
    private List<ReconciliationResultDTO> performReconciliation(
            List<BankStatement> bankStatements,
            List<ExcelTransaction> excelTransactions) {

        List<ReconciliationResultDTO> results = new ArrayList<>();

        System.out.println("=== EXECUTANDO RECONCILIAÇÃO ===");
        System.out.println("Transações bancárias: " + bankStatements.size());
        System.out.println("Transações Excel: " + excelTransactions.size());

        for (BankStatement bank : bankStatements) {
            for (ExcelTransaction excel : excelTransactions) {
                Double bankAmount = bank.getAmount();
                Double excelAmount = excel.getAmount();

                if (bankAmount == null || excelAmount == null) continue;

                long days = ChronoUnit.DAYS.between(bank.getData(), excel.getData());
                double similarity = similarityService.calculateSimilarity(
                        bank.getDescription(), excel.getDescription());

                // valor: igual em módulo (tolerância)
                boolean valueMatch = Math.abs(Math.abs(bankAmount) - Math.abs(excelAmount)) < 0.01;

                // Determina se as tags são opostas.
                // Se tags existirem, compara por texto (credito <-> debito).
                // Se tags faltarem, faz fallback por sinal dos amounts (positivo vs negativo).
                boolean tagOppositeFinal;
                if (bank.getTag() != null && excel.getTag() != null) {
                    tagOppositeFinal = areTagsOpposite(bank.getTag(), excel.getTag());
                } else {
                    // fallback por sinal: crédito (positivo) ↔ débito (negativo)
                    tagOppositeFinal = (bankAmount * excelAmount) < 0;
                }

                boolean dateMatch = Math.abs(days) <= 3;
                boolean sameDate = days == 0;
                boolean descriptionMatch = similarity > 0.6;

                // DEBUG
                System.out.printf("Comparando: Banco=%.2f Excel=%.2f | Dias=%d | ValueMatch=%s | TagOpposite=%s | SameDate=%s | Similaridade=%.2f%n",
                        bankAmount, excelAmount, days, valueMatch, tagOppositeFinal, sameDate, similarity);

                // NOVA LÓGICA: 
                // 1º - Se valor igual + tags opostas + mesma data => RECONCILIA (ignora descrição)
                if (valueMatch && tagOppositeFinal && sameDate) {
                    results.add(new ReconciliationResultDTO(bank, excel, similarity));
                    System.out.printf("🎯 MATCH (valor+data exatos) similaridade=%.2f | bancoTag=%s | excelTag=%s | valorBanco=%.2f | valorExcel=%.2f%n",
                            similarity, safeTag(bank.getTag()), safeTag(excel.getTag()), bankAmount, excelAmount);
                }
                // 2º - Se valor igual + tags opostas + data NÃO bate => CHECA DESCRIÇÃO
                else if (valueMatch && tagOppositeFinal && !sameDate) {
                    if (descriptionMatch && dateMatch) {
                        results.add(new ReconciliationResultDTO(bank, excel, similarity));
                        System.out.printf("🎯 MATCH (valor igual + descrição parecida) similaridade=%.2f | bancoTag=%s | excelTag=%s | dias=%d | valor=%.2f%n",
                                similarity, safeTag(bank.getTag()), safeTag(excel.getTag()), days, bankAmount);
                    }
                }
            }
        }

        System.out.println("=== RECONCILIAÇÃO FINALIZADA ===");
        System.out.println("Total de matches: " + results.size());

        return results;
    }

    // Helper: considera "credito" <-> "debito" como opostos.
    private boolean areTagsOpposite(String bankTag, String excelTag) {
        if (bankTag == null || excelTag == null) return false;
        boolean bCred = "credito".equalsIgnoreCase(bankTag);
        boolean bDeb = "debito".equalsIgnoreCase(bankTag);
        boolean eCred = "credito".equalsIgnoreCase(excelTag);
        boolean eDeb = "debito".equalsIgnoreCase(excelTag);
        return (bCred && eDeb) || (bDeb && eCred);
    }

    private String safeTag(String t) {
        return t == null ? "null" : t;
    }

    // === Relatório detalhado ===
    public ReconciliationReportDTO generateReconciliationReport(LocalDate dataReconciliacao,
                                                                String empresa,
                                                                String banco,
                                                                String conta,
                                                                List<BankStatement> bankStatements,
                                                                List<ExcelTransaction> excelTransactions,
                                                                List<ReconciliationResultDTO> matches) {

        ReconciliationReportDTO report = new ReconciliationReportDTO();
        report.setDataReconciliacao(dataReconciliacao);
        report.setEmpresa(empresa);
        report.setBanco(banco);
        report.setConta(conta);

        identifyUnreconciledTransactions(report, bankStatements, excelTransactions, matches);
        calculateBalances(report, bankStatements, excelTransactions);
        addStatistics(report, bankStatements, excelTransactions, matches);

        System.out.println("=== RELATÓRIO GERADO ===");
        System.out.println("Empresa: " + empresa);
        System.out.println("Transações bancárias: " + bankStatements.size());
        System.out.println("Transações Excel: " + excelTransactions.size());
        System.out.println("Matches encontrados: " + matches.size());
        System.out.println("Diferença: " + report.getDiferenca());

        return report;
    }

    public ReconciliationReportDTO generateReconciliationReport(LocalDate dataReconciliacao,
                                                                String empresa,
                                                                String banco,
                                                                String conta) {
        List<BankStatement> bankStatements = bankRepo.findAll();
        List<ExcelTransaction> excelTransactions = excelRepo.findAll();
        List<ReconciliationResultDTO> matches = performReconciliation(bankStatements, excelTransactions);

        return generateReconciliationReport(dataReconciliacao, empresa, banco, conta,
                bankStatements, excelTransactions, matches);
    }

    private void identifyUnreconciledTransactions(ReconciliationReportDTO report,
                                                  List<BankStatement> bankStatements,
                                                  List<ExcelTransaction> excelTransactions,
                                                  List<ReconciliationResultDTO> matches) {

        Set<UUID> reconciledBankIds = matches.stream()
                .map(match -> match.getBank().getId())
                .collect(Collectors.toSet());

        Set<UUID> reconciledExcelIds = matches.stream()
                .map(match -> match.getExcel().getId())
                .collect(Collectors.toSet());

        // 1 - Débitos no Banco não contabilizados pela Empresa (usa tag quando disponível)
        for (BankStatement bank : bankStatements) {
            if (!reconciledBankIds.contains(bank.getId())) {
                String tag = bank.getTag();
                Double amount = bank.getAmount();
                if (tag != null) {
                    if ("debito".equalsIgnoreCase(tag)) {
                        ReconciliationReportDTO.MovimentoNaoConciliado mov = new ReconciliationReportDTO.MovimentoNaoConciliado();
                        mov.setData(bank.getData());
                        mov.setDescricaoTerceiro(bank.getDescription());
                        mov.setValor(amount != null ? Math.abs(amount) : 0.0);
                        report.getDebitosBancoNaoContabilizados().add(mov);
                        report.setTotalDebitosBancoNaoContabilizados(
                                report.getTotalDebitosBancoNaoContabilizados() + (amount != null ? Math.abs(amount) : 0.0));
                        continue;
                    }
                    if ("credito".equalsIgnoreCase(tag)) {
                        ReconciliationReportDTO.MovimentoNaoConciliado mov = new ReconciliationReportDTO.MovimentoNaoConciliado();
                        mov.setData(bank.getData());
                        mov.setDescricaoTerceiro(bank.getDescription());
                        mov.setValor(amount != null ? amount : 0.0);
                        report.getCreditosBancoNaoContabilizados().add(mov);
                        report.setTotalCreditosBancoNaoContabilizados(
                                report.getTotalCreditosBancoNaoContabilizados() + (amount != null ? amount : 0.0));
                        continue;
                    }
                }
                // fallback: se tag nula ou inesperada, usa sinal do amount
                if (amount != null) {
                    if (amount < 0) {
                        ReconciliationReportDTO.MovimentoNaoConciliado mov = new ReconciliationReportDTO.MovimentoNaoConciliado();
                        mov.setData(bank.getData());
                        mov.setDescricaoTerceiro(bank.getDescription());
                        mov.setValor(Math.abs(amount));
                        report.getDebitosBancoNaoContabilizados().add(mov);
                        report.setTotalDebitosBancoNaoContabilizados(
                                report.getTotalDebitosBancoNaoContabilizados() + Math.abs(amount));
                    } else {
                        ReconciliationReportDTO.MovimentoNaoConciliado mov = new ReconciliationReportDTO.MovimentoNaoConciliado();
                        mov.setData(bank.getData());
                        mov.setDescricaoTerceiro(bank.getDescription());
                        mov.setValor(amount);
                        report.getCreditosBancoNaoContabilizados().add(mov);
                        report.setTotalCreditosBancoNaoContabilizados(
                                report.getTotalCreditosBancoNaoContabilizados() + amount);
                    }
                }
            }
        }

        // 2 - Movimentos da Empresa não contabilizados pelo Banco (usa tag quando disponível)
        for (ExcelTransaction excel : excelTransactions) {
            if (!reconciledExcelIds.contains(excel.getId())) {
                String tag = excel.getTag();
                Double amount = excel.getAmount();
                if (tag != null) {
                    if ("debito".equalsIgnoreCase(tag)) {
                        ReconciliationReportDTO.MovimentoNaoConciliado mov = new ReconciliationReportDTO.MovimentoNaoConciliado();
                        mov.setData(excel.getData());
                        mov.setDescricaoTerceiro(excel.getDescription());
                        mov.setValor(amount != null ? Math.abs(amount) : 0.0);
                        report.getDebitosEmpresaNaoContabilizados().add(mov);
                        report.setTotalDebitosEmpresaNaoContabilizados(
                                report.getTotalDebitosEmpresaNaoContabilizados() + (amount != null ? Math.abs(amount) : 0.0));
                        continue;
                    }
                    if ("credito".equalsIgnoreCase(tag)) {
                        ReconciliationReportDTO.MovimentoNaoConciliado mov = new ReconciliationReportDTO.MovimentoNaoConciliado();
                        mov.setData(excel.getData());
                        mov.setDescricaoTerceiro(excel.getDescription());
                        mov.setValor(amount != null ? amount : 0.0);
                        report.getCreditosEmpresaNaoContabilizados().add(mov);
                        report.setTotalCreditosEmpresaNaoContabilizados(
                                report.getTotalCreditosEmpresaNaoContabilizados() + (amount != null ? amount : 0.0));
                        continue;
                    }
                }
                // fallback por sinal do amount
                if (amount != null) {
                    if (amount < 0) {
                        ReconciliationReportDTO.MovimentoNaoConciliado mov = new ReconciliationReportDTO.MovimentoNaoConciliado();
                        mov.setData(excel.getData());
                        mov.setDescricaoTerceiro(excel.getDescription());
                        mov.setValor(Math.abs(amount));
                        report.getDebitosEmpresaNaoContabilizados().add(mov);
                        report.setTotalDebitosEmpresaNaoContabilizados(
                                report.getTotalDebitosEmpresaNaoContabilizados() + Math.abs(amount));
                    } else {
                        ReconciliationReportDTO.MovimentoNaoConciliado mov = new ReconciliationReportDTO.MovimentoNaoConciliado();
                        mov.setData(excel.getData());
                        mov.setDescricaoTerceiro(excel.getDescription());
                        mov.setValor(amount);
                        report.getCreditosEmpresaNaoContabilizados().add(mov);
                        report.setTotalCreditosEmpresaNaoContabilizados(
                                report.getTotalCreditosEmpresaNaoContabilizados() + amount);
                    }
                }
            }
        }
    }

    private void calculateBalances(ReconciliationReportDTO report,
                                   List<BankStatement> bankStatements,
                                   List<ExcelTransaction> excelTransactions) {
        double saldoBanco = bankStatements.stream().mapToDouble(b -> b.getAmount() != null ? b.getAmount() : 0).sum();
        double saldoEmpresa = excelTransactions.stream().mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0).sum();

        report.setSaldoExtratoBancario(saldoBanco);
        report.setSaldoContaCorrenteEmpresa(saldoEmpresa);

        double saldoConciliado = saldoBanco
                + report.getTotalDebitosBancoNaoContabilizados()
                - report.getTotalCreditosBancoNaoContabilizados()
                + report.getTotalDebitosEmpresaNaoContabilizados()
                - report.getTotalCreditosEmpresaNaoContabilizados();

        report.setSaldoBancoConciliado(saldoConciliado);
        report.setDiferenca(saldoConciliado - saldoEmpresa);
    }

    private void addStatistics(ReconciliationReportDTO report,
                               List<BankStatement> bankStatements,
                               List<ExcelTransaction> excelTransactions,
                               List<ReconciliationResultDTO> matches) {
        report.setTotalBankStatements(bankStatements.size());
        report.setTotalExcelTransactions(excelTransactions.size());
        report.setTotalMatches(matches.size());
        if (!bankStatements.isEmpty()) {
            double rate = (double) matches.size() / bankStatements.size() * 100;
            report.setTaxaReconciliacao(Math.round(rate * 100.0) / 100.0);
        }
    }

    public static class ReconciliationResult {
        private final List<ReconciliationResultDTO> matches;
        private final ReconciliationReportDTO report;
        private final String excelFilePath;

        public ReconciliationResult(List<ReconciliationResultDTO> matches, ReconciliationReportDTO report, String excelFilePath) {
            this.matches = matches;
            this.report = report;
            this.excelFilePath = excelFilePath;
        }

        public List<ReconciliationResultDTO> getMatches() { return matches; }
        public ReconciliationReportDTO getReport() { return report; }
        public String getExcelFilePath() { return excelFilePath; }
    }
}