package tarrciodev.com.reconcile.DTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReconciliationReportDTO {
    private LocalDate dataReconciliacao;
    private String empresa;
    private String banco;
    private String conta;
    private Double saldoExtratoBancario;
    private Double saldoContaCorrenteEmpresa;
    private Double diferenca;
    
    // Movimentos não conciliados
    private List<MovimentoNaoConciliado> debitosBancoNaoContabilizados = new ArrayList<>();
    private List<MovimentoNaoConciliado> creditosBancoNaoContabilizados = new ArrayList<>();
    private List<MovimentoNaoConciliado> debitosEmpresaNaoContabilizados = new ArrayList<>();
    private List<MovimentoNaoConciliado> creditosEmpresaNaoContabilizados = new ArrayList<>();
    
    // Totais
    private Double totalDebitosBancoNaoContabilizados = 0.0;
    private Double totalCreditosBancoNaoContabilizados = 0.0;
    private Double totalDebitosEmpresaNaoContabilizados = 0.0;
    private Double totalCreditosEmpresaNaoContabilizados = 0.0;
    private Double saldoBancoConciliado = 0.0;

    // Estatísticas
    private Integer totalBankStatements = 0;
    private Integer totalExcelTransactions = 0;
    private Integer totalMatches = 0;
    private Double taxaReconciliacao = 0.0;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MovimentoNaoConciliado {
        private LocalDate data;
        private String tipoDoc;
        private String numeroDoc;
        private String descricaoTerceiro;
        private Double valor;
    }
}