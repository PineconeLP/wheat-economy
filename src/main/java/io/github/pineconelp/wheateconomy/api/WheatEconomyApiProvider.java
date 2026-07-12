package io.github.pineconelp.wheateconomy.api;

import java.sql.SQLException;
import java.util.List;

import io.github.pineconelp.wheateconomy.bank.BankAccount;
import io.github.pineconelp.wheateconomy.bank.BankRepository;

public class WheatEconomyApiProvider implements WheatEconomyApi {
  private final BankRepository bankRepository;

  public WheatEconomyApiProvider(BankRepository bankRepository) {
    this.bankRepository = bankRepository;
  }

  @Override
  public List<BankAccount> getTopAccounts(int limit) throws SQLException {
    return bankRepository.getTopAccounts(limit);
  }
}
