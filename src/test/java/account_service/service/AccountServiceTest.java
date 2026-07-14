package account_service.service;

import account_service.model.Account;
import account_service.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldReturnAllAccounts() {
        Account a1 = new Account();
        a1.setId(1L);
        a1.setAccount("ACC000000001");
        a1.setCustomerName("Alice");
        a1.setBalance(new BigDecimal("1000.00"));

        Account a2 = new Account();
        a2.setId(2L);
        a2.setAccount("ACC000000002");
        a2.setCustomerName("Bob");
        a2.setBalance(new BigDecimal("500.00"));

        when(accountRepository.findAll()).thenReturn(Arrays.asList(a1, a2));

        List<Account> result = accountService.findAll();

        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getCustomerName());
        assertEquals("Bob", result.get(1).getCustomerName());
        verify(accountRepository, times(1)).findAll();
    }

    @Test
    void shouldFindAccountById() {
        Account account = new Account();
        account.setId(1L);
        account.setAccount("ACC000000001");
        account.setCustomerName("Alice");
        account.setBalance(new BigDecimal("1000.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        Optional<Account> result = accountService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getCustomerName());
        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    void shouldReturnEmptyWhenAccountNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Account> result = accountService.findById(99L);

        assertFalse(result.isPresent());
        verify(accountRepository, times(1)).findById(99L);
    }

    @Test
    void shouldCreateAccount() {
        Account account = new Account();
        account.setCustomerName("Alice");
        account.setTypeAccount("CURRENT");
        account.setBalance(new BigDecimal("500.00"));

        Account savedAccount = new Account();
        savedAccount.setId(1L);
        savedAccount.setAccount("ACC123456789");
        savedAccount.setCustomerName("Alice");
        savedAccount.setTypeAccount("CURRENT");
        savedAccount.setBalance(new BigDecimal("500.00"));

        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        Account result = accountService.create(account);

        assertNotNull(result.getAccount());
        assertTrue(result.getAccount().startsWith("ACC"));
        assertEquals(12, result.getAccount().length());
        assertEquals("Alice", result.getCustomerName());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void shouldCreditAccount() {
        Account account = new Account();
        account.setId(1L);
        account.setAccount("ACC000000001");
        account.setBalance(new BigDecimal("1000.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.credit(1L, new BigDecimal("500.00"));

        assertEquals(new BigDecimal("1500.00"), result.getBalance());
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void shouldThrowExceptionWhenCreditAccountNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> accountService.credit(99L, new BigDecimal("500.00")));

        assertEquals("Compte introuvable : 99", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldDebitAccount() {
        Account account = new Account();
        account.setId(1L);
        account.setAccount("ACC000000001");
        account.setBalance(new BigDecimal("1000.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.debit(1L, new BigDecimal("300.00"));

        assertEquals(new BigDecimal("700.00"), result.getBalance());
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void shouldThrowExceptionWhenDebitAccountNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> accountService.debit(99L, new BigDecimal("100.00")));

        assertEquals("Compte introuvable : 99", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldThrowExceptionWhenInsufficientBalance() {
        Account account = new Account();
        account.setId(1L);
        account.setAccount("ACC000000001");
        account.setBalance(new BigDecimal("100.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> accountService.debit(1L, new BigDecimal("500.00")));

        assertEquals("Solde insuffisant", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldDeleteAccount() {
        doNothing().when(accountRepository).deleteById(1L);

        accountService.delete(1L);

        verify(accountRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldDebitExactBalance() {
        Account account = new Account();
        account.setId(1L);
        account.setAccount("ACC000000001");
        account.setBalance(new BigDecimal("500.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account result = accountService.debit(1L, new BigDecimal("500.00"));

        assertEquals(new BigDecimal("0.00"), result.getBalance());
    }
}
