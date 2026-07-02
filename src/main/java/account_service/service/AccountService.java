package account_service.service;

import account_service.model.Account;
import account_service.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
        private final AccountRepository accountRepository;

        public AccountService(AccountRepository accountRepository) {
            this.accountRepository = accountRepository;
        }

        public List<Account> findAll() {
            return accountRepository.findAll();
        }

        public Optional<Account> findById(Long id) {
            return accountRepository.findById(id);
        }

        public Account create(Account account) {
            account.setAccount(generateAccount());
            return accountRepository.save(account);
        }

        public Account credit(Long id, BigDecimal amount) {
            Account account = accountRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Compte introuvable : " + id));
            account.setBalance(account.getBalance().add(amount));
            return accountRepository.save(account);
        }

        public Account debit(Long id, BigDecimal amount) {
            Account account = accountRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Compte introuvable : " + id));

            if (account.getBalance().compareTo(amount) < 0) {
                throw new RuntimeException("Solde insuffisant");
            }

            account.setBalance(account.getBalance().subtract(amount));
            return accountRepository.save(account);
        }

        public void delete(Long id) {
            accountRepository.deleteById(id);
        }

        private String generateAccount() {
            long random = (long) (Math.random() * 1_000_000_000L);
            return "ACC" + String.format("%09d", random);
        }
}



