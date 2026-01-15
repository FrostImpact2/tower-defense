# Tower Defense Mod

A Minecraft tower defense mod using NeoForge 1.21.1.

## Features

### Towers
Towers are entities that look like armor stands with specific armor and held items. They can:
- **Attack enemies** within their range with particle animations
- **Be moved** by clicking the "Move" button in the GUI and then clicking a destination block
- **Aggro enemies** - when an enemy's path is blocked by a tower, the enemy will attack it
- **Be upgraded** to improve stats
- **Be sold** for currency
- **Use abilities** - special skills with cooldowns

### Tower Stats
- **Damage**: Amount of damage dealt per attack
- **Range**: Attack range in blocks
- **Attack Speed**: Attacks per second
- **Health**: Tower's health points
- **Aggro Limit**: Maximum number of enemies that can attack this tower at once (role-dependent)

### Enemies
Enemies follow a predefined path and will:
- Move along waypoints
- Attack towers that block their path
- Award currency when defeated

### Example Implementations

#### Archer Tower (DPS Role)
- High attack speed
- Medium damage
- Low aggro limit (1)
- Special Ability: Multi-Shot - fires multiple arrows at once

#### Zombie Enemy
- Basic melee enemy
- Slow movement speed
- Medium health
- Follows the path and attacks blocking towers

## Project Structure

```
src/main/java/com/towerdefense/
├── TowerDefenseMod.java          # Main mod class
├── ability/                       # Ability system
│   ├── TowerAbility.java         # Ability interface
│   ├── AbstractTowerAbility.java # Base ability implementation
│   └── MultiShotAbility.java     # Example ability
├── client/                        # Client-side code
│   ├── ClientEvents.java         # Screen registration
│   └── TowerMoveHandler.java     # Move mode handling
├── entity/
│   ├── enemy/
│   │   ├── BaseEnemyEntity.java  # Base enemy class
│   │   └── ZombieEnemyEntity.java # Example enemy
│   └── tower/
│       ├── BaseTowerEntity.java  # Base tower class
│       ├── TowerStats.java       # Tower stats data
│       └── ArcherTowerEntity.java # Example tower
├── event/
│   └── ModEventHandlers.java     # Entity attribute registration
├── gui/
│   ├── TowerMenu.java            # Server-side menu
│   ├── TowerMenuProvider.java    # Menu provider
│   └── TowerScreen.java          # Client-side GUI
├── network/
│   ├── ModNetwork.java           # Network registration
│   ├── TowerActionPacket.java    # Upgrade/Sell/Ability packets
│   └── TowerMovePacket.java      # Tower movement packets
└── registry/
    ├── ModEntities.java          # Entity registration
    ├── ModItems.java             # Item registration
    └── ModMenuTypes.java         # Menu type registration
```

## Building

Requirements:
- Java 21
- Gradle 8.9+

```bash
./gradlew build
```

## Running

```bash
./gradlew runClient
```

## License

MIT