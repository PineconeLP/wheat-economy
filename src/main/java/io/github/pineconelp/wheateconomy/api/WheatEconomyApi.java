package io.github.pineconelp.wheateconomy.api;

import java.sql.SQLException;
import java.util.List;

import io.github.pineconelp.wheateconomy.bank.BankAccount;

public interface WheatEconomyApi {
  List<BankAccount> getTopAccounts(int limit) throws SQLException;
}
