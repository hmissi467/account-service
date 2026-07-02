package account_service.controller;

import account_service.model.Account;
import account_service.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<Account> getAll() {
        return accountService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getById(@PathVariable Long id) {
        return accountService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Account create(@RequestBody Account account) {
        return accountService.create(account);
    }

    @PostMapping("/{id}/credit")
    public Account credit(@PathVariable Long id,
                          @RequestBody Map<String, BigDecimal> body) {
        return accountService.credit(id, body.get("amount"));
    }

    @PostMapping("/{id}/debit")
    public Account debit(@PathVariable Long id,
                         @RequestBody Map<String, BigDecimal> body) {
        return accountService.debit(id, body.get("amount"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        accountService.delete(id);
    }
}