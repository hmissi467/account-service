package account_service.controller;

import account_service.model.Account;
import account_service.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnAllAccounts() throws Exception {
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

        when(accountService.findAll()).thenReturn(Arrays.asList(a1, a2));

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].customerName").value("Alice"))
                .andExpect(jsonPath("$[1].customerName").value("Bob"));

        verify(accountService, times(1)).findAll();
    }

    @Test
    void shouldReturnAccountById() throws Exception {
        Account account = new Account();
        account.setId(1L);
        account.setAccount("ACC000000001");
        account.setCustomerName("Alice");
        account.setBalance(new BigDecimal("1000.00"));

        when(accountService.findById(1L)).thenReturn(Optional.of(account));

        mockMvc.perform(get("/api/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.account").value("ACC000000001"))
                .andExpect(jsonPath("$.customerName").value("Alice"))
                .andExpect(jsonPath("$.balance").value(1000.00));

        verify(accountService, times(1)).findById(1L);
    }

    @Test
    void shouldReturn404WhenAccountNotFound() throws Exception {
        when(accountService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/accounts/99"))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).findById(99L);
    }

    @Test
    void shouldCreateAccount() throws Exception {
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

        when(accountService.create(any(Account.class))).thenReturn(savedAccount);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(account)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.account").value("ACC123456789"))
                .andExpect(jsonPath("$.customerName").value("Alice"))
                .andExpect(jsonPath("$.balance").value(500.00));

        verify(accountService, times(1)).create(any(Account.class));
    }

    @Test
    void shouldCreditAccount() throws Exception {
        Account account = new Account();
        account.setId(1L);
        account.setAccount("ACC000000001");
        account.setBalance(new BigDecimal("1500.00"));

        when(accountService.credit(eq(1L), any(BigDecimal.class))).thenReturn(account);

        mockMvc.perform(post("/api/accounts/1/credit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("amount", new BigDecimal("500.00")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.balance").value(1500.00));

        verify(accountService, times(1)).credit(eq(1L), any(BigDecimal.class));
    }

    @Test
    void shouldDebitAccount() throws Exception {
        Account account = new Account();
        account.setId(1L);
        account.setAccount("ACC000000001");
        account.setBalance(new BigDecimal("700.00"));

        when(accountService.debit(eq(1L), any(BigDecimal.class))).thenReturn(account);

        mockMvc.perform(post("/api/accounts/1/debit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("amount", new BigDecimal("300.00")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.balance").value(700.00));

        verify(accountService, times(1)).debit(eq(1L), any(BigDecimal.class));
    }

    @Test
    void shouldThrowWhenCreditAccountNotFound() throws Exception {
        when(accountService.credit(eq(99L), any(BigDecimal.class)))
                .thenThrow(new RuntimeException("Compte introuvable : 99"));

        try {
            mockMvc.perform(post("/api/accounts/99/credit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("amount", new BigDecimal("500.00")))));
            fail("Expected RuntimeException");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof RuntimeException);
            assertEquals("Compte introuvable : 99", e.getCause().getMessage());
        }
    }

    @Test
    void shouldThrowWhenDebitInsufficientBalance() throws Exception {
        when(accountService.debit(eq(1L), any(BigDecimal.class)))
                .thenThrow(new RuntimeException("Solde insuffisant"));

        try {
            mockMvc.perform(post("/api/accounts/1/debit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("amount", new BigDecimal("5000.00")))));
            fail("Expected RuntimeException");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof RuntimeException);
            assertEquals("Solde insuffisant", e.getCause().getMessage());
        }
    }

    @Test
    void shouldDeleteAccount() throws Exception {
        doNothing().when(accountService).delete(1L);

        mockMvc.perform(delete("/api/accounts/1"))
                .andExpect(status().isNoContent());

        verify(accountService, times(1)).delete(1L);
    }
}
