package tarrciodev.com.reconcile.DTO.Transaction;

import lombok.Data;

@Data
public class TransactionItemDTO {

    private String data;
    private String description;
    private Double credito;
    private Double debito;
}

