package account_service.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void shouldSetAndGetId() {
        Account account = new Account();
        account.setId(1L);
        assertEquals(1L, account.getId());
    }

    @Test
    void shouldSetAndGetAccountNumber() {
        Account account = new Account();
        account.setAccount("ACC000000001");
        assertEquals("ACC000000001", account.getAccount());
    }

    @Test
    void shouldSetAndGetCustomerName() {
        Account account = new Account();
        account.setCustomerName("Alice Dupont");
        assertEquals("Alice Dupont", account.getCustomerName());
    }

    @Test
    void shouldSetAndGetTypeAccount() {
        Account account = new Account();
        account.setTypeAccount("CURRENT");
        assertEquals("CURRENT", account.getTypeAccount());
    }

    @Test
    void shouldSetAndGetBalance() {
        Account account = new Account();
        account.setBalance(new BigDecimal("1234.56"));
        assertEquals(new BigDecimal("1234.56"), account.getBalance());
    }

    @Test
    void shouldInitializeWithNullValues() {
        Account account = new Account();
        assertNull(account.getId());
        assertNull(account.getAccount());
        assertNull(account.getCustomerName());
        assertNull(account.getTypeAccount());
        assertNull(account.getBalance());
    }
}
