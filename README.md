# WheatEconomy

Turn wheat into your Minecraft server's currency!

Integrates with [Vault](https://www.spigotmc.org/resources/vault.34315/) and
[VaultUnlocked](https://modrinth.com/plugin/vaultunlocked) APIs.

## Requirements

- Paper 1.21+
- Java 21+

## Configuration

```yaml
# plugins/WheatEconomy/config.yml
database:
  jdbc-url: "jdbc:sqlite:plugins/WheatEconomy/wheateconomy.db"
```

| Key | Description |
| --- | --- |
| `database.jdbc-url` | JDBC URL for the SQLite database backing all balances |

## Usage

### Player Commands

Require the `wheateconomy.bank` permission.

| Command | Description |
| --- | --- |
| `/bank balance` | Show your balance and clickable Deposit/Withdraw/Send buttons |
| `/bank deposit all` | Deposit all wheat and hay bales from your inventory |
| `/bank deposit wheat <amount>` | Deposit a specific amount of wheat |
| `/bank deposit wheat all` | Deposit all wheat |
| `/bank deposit haybale <amount>` | Deposit a specific number of hay bales (9 wheat each) |
| `/bank deposit haybale all` | Deposit all hay bales |
| `/bank withdraw wheat <amount>` | Withdraw wheat into your inventory |
| `/bank withdraw wheat all` | Withdraw as much wheat as fits |
| `/bank withdraw haybale <amount>` | Withdraw wheat as hay bales |
| `/bank withdraw haybale all` | Withdraw as many hay bales as fit |
| `/bank send <player> <amount>` | Transfer wheat to another online player |

### Admin Commands

Require the `wheateconomy.bank.admin` permission.

| Command | Description |
| --- | --- |
| `/bank get <player>` | Check a player's balance |
| `/bank set <player> <amount>` | Set a player's balance to an exact amount |
| `/bank add <player> <amount>` | Add wheat to a player's balance |

## Permissions

| Permission | Description | Default |
| --- | --- | --- |
| `wheateconomy.bank` | Use the player bank commands | all players |
| `wheateconomy.bank.admin` | Use the admin bank commands | operators |

## Contributing

All contributions are welcome! Feel free to open an issue or pull request for
anything you need.
