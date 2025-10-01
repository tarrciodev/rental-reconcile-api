package tarrciodev.com.reconcile.DTO;

import tarrciodev.com.reconcile.entities.BankStatement;
import tarrciodev.com.reconcile.entities.ExcelTransaction;

public class ReconciliationResultDTO {
    private BankStatement bank;
    private ExcelTransaction excel;
    private double similarity;

    public ReconciliationResultDTO(BankStatement bank, ExcelTransaction excel, double similarity) {
        this.bank = bank;
        this.excel = excel;
        this.similarity = similarity;
    }

    // Getters and setters
    public BankStatement getBank() {
        return bank;
    }

    public void setBank(BankStatement bank) {
        this.bank = bank;
    }

    public ExcelTransaction getExcel() {
        return excel;
    }

    public void setExcel(ExcelTransaction excel) {
        this.excel = excel;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }
}