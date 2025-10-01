package tarrciodev.com.reconcile.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import tarrciodev.com.reconcile.entities.BankStatement;

public interface BankStatementRepository extends JpaRepository<BankStatement, UUID> {}
