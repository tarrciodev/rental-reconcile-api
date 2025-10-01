package tarrciodev.com.reconcile.DTO;

import java.util.List;

import lombok.Data;
import tarrciodev.com.reconcile.DTO.Transaction.TransactionItemDTO;

@Data
public class DataWrapperDTO {
   private List<TransactionItemDTO> extrato;
    private List<TransactionItemDTO> excel; 
}
