package tarrciodev.com.reconcile.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import tarrciodev.com.reconcile.entities.ExcelTransaction;

public interface ExcelTransactionRepository extends JpaRepository<ExcelTransaction, UUID> {}
