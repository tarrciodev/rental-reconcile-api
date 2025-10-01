package tarrciodev.com.reconcile.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReconciliationSummaryDTO {
    private int totalBankTransactions;
    private int totalCompanyTransactions;
    private int reconciledTransactions;
    private int pendingTransactions;
    private String reportName;
  
}
