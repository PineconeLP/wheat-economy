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

wheat:
  offline-growth-enabled: true
```

| Key | Description |
| --- | --- |
| `database.jdbc-url` | JDBC URL for the SQLite database backing all balances |
| `wheat.offline-growth-enabled` | Whether wheat keeps growing in chunks while they are unloaded. Read once at startup |

## Usage

### Bank Commands

Require the `wheateconomy.bank` permission.

| Command | Description |
| --- | --- |
| `/bank balance` | Show your balance and clickable Deposit/Withdraw/Send buttons |
| `/bank leaderboard` | Show the server's top wheat balances |
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

### Bank Admin Commands

Require the `wheateconomy.bank.admin` permission.

| Command | Description |
| --- | --- |
| `/bank get <player>` | Check a player's balance |
| `/bank set <player> <amount>` | Set a player's balance to an exact amount |
| `/bank add <player> <amount>` | Add wheat to a player's balance |

### Wheat Admin Commands

Require operator status.

| Command | Description |
| --- | --- |
| `/wheat simulate <seconds>` | Simulate the given number of seconds of offline wheat growth in your current chunk |

### WorldGuard Flags

If your server has WorldGuard, WheatEconomy registers the following WorldGuard region flags. 

| Flag | Default | Description |
| --- | --- | --- |
| `has-banker` | `allow` | Whether players can access the bank while inside the region |
| `wheat-growth` | `allow` | Whether wheat can grow in the region |

## Permissions

| Permission | Description | Default |
| --- | --- | --- |
| `wheateconomy.bank` | Use the player bank commands | all players |
| `wheateconomy.bank.admin` | Use the admin bank commands | operators |

## Contributing

All contributions are welcome! Feel free to open an issue or pull request for
anything you need.
