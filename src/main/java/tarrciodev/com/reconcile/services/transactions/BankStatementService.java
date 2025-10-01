package tarrciodev.com.reconcile.services.transactions;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tarrciodev.com.reconcile.entities.BankStatement;
import tarrciodev.com.reconcile.repositories.BankStatementRepository;

@Service
@RequiredArgsConstructor
public class BankStatementService {

    private final BankStatementRepository bankStatementRepository;

    public List<BankStatement> saveAll(List<BankStatement> bankStatements) {
        return bankStatementRepository.saveAll(bankStatements);
    }
}