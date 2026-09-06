# ROF_LoyaltyRewards

A Minecraft (Spigot/Paper) plugin that rewards players for their loyalty - however they choose to be active. Reward players for coming back after time away, for staying online, and for simply logging in on a schedule.

![GitHub all releases](https://img.shields.io/github/downloads/RageOfFire/ROF_LoyaltyRewards/total)
![Discord](https://img.shields.io/discord/752171524919918672)
![GitHub commit activity](https://img.shields.io/github/commit-activity/m/RageOfFire/ROF_LoyaltyRewards)
![GitHub last commit](https://img.shields.io/github/last-commit/RageOfFire/ROF_LoyaltyRewards)
![GitHub repo size](https://img.shields.io/github/repo-size/RageOfFire/ROF_LoyaltyRewards)
![GitHub followers](https://img.shields.io/github/followers/RageOfFire)
![GitHub Repo stars](https://img.shields.io/github/stars/RageOfFire/ROF_LoyaltyRewards)

## Features

- **Offline rewards** - reward a player for how long they were away, claimed manually through an in-game GUI shown when they return.
- **Online rewards** - reward a player for continuous time spent in their current session (e.g. "play 1 hour, get something"), granted automatically the instant it's earned - no delay, no polling.
- **Login rewards** - reward a player just for logging in on a schedule: daily, weekly, monthly, or any custom interval you set. Granted automatically on join.
- **Reward GUI** - `/rofloyaltyrewards gui` shows every configured reward and its live status (claimable, locked with a countdown, on cooldown, or no permission). Automatically opens on join when a player has something to claim.
- **PlaceholderAPI support** - both consumes placeholders in your reward messages/commands and exposes its own (`%rofloyaltyrewards_*%`) for other plugins/GUIs to use.
- Per-reward permissions, custom messages, and any number of commands executed as console or as the player.
- All reward types are cooldown-tracked per player, so nothing can be claimed twice before its own interval passes again.

## Reward types at a glance

| Type | Config section | Trigger | Claimed by |
|---|---|---|---|
| Offline | `rewards` | Time since the player's last session | Clicking it in the GUI |
| Online | `online-rewards` | Continuous time in the *current* session | Automatic, instantly |
| Login | `login-reward` | Time since last claim, regardless of session length | Automatic, on join |

## Time format

Every reward's `time` value accepts any combination of:

| Unit | Meaning |
|---|---|
| `mo` | Month (30 days) |
| `d` | Day |
| `h` | Hour |
| `m` | Minute |
| `s` | Second |

Example: `time: "1mo 5d 6h 30m 20s"`

For `login-reward` entries, `time` is the *interval* between claims rather than a wait - `time: "1d"` means "once every 24 hours", not "wait 24 hours online".

## Configuration example

```yaml
rewards:
  vip:
    permission: "group.vip"
    time: "10d 12h"
    message: "&eYou earned a VIP welcome-back reward!"
    execute: 'CONSOLE'
    commands:
      - "give %player% gold_ingot 5"

online-rewards:
  dedicated:
    permission: ""
    time: "5h"
    message: "&6You're a dedicated player! Enjoy this reward."
    execute: 'CONSOLE'
    commands:
      - "give %player% diamond 1"

login-reward:
  daily:
    permission: ""
    time: "1d"
    message: "&aThanks for logging in today!"
    execute: 'CONSOLE'
    commands:
      - "give %player% golden_apple 1"
```

See `config.yml` for the full set of options and more examples (including a punishment-style reward, since nothing says a "reward" has to be positive).

## Commands

| Command | Description | Permission |
|---|---|---|
| `/rofloyaltyrewards gui` | Open the reward GUI | `rofow.gui` (default: true) |
| `/rofloyaltyrewards reload` | Reload the config | `rofow.admin` (default: op) |

Aliases: `/rofow`, `/loyaltyrewards`, `/lr`

## Permissions

| Permission | Description | Default |
|---|---|---|
| `rofow.admin` | Reload the plugin config | op |
| `rofow.exempt` | Excludes a player from all reward types | false |
| `rofow.alert` | Receive broadcast messages when someone earns a reward | op |
| `rofow.gui` | Open the reward GUI | true |

## PlaceholderAPI

Requires [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) 2.11.5+.

| Placeholder | Returns |
|---|---|
| `%rofloyaltyrewards_offline_time%` | Formatted time since the player's last session |
| `%rofloyaltyrewards_online_time%` | Formatted time in the player's current session |
| `%rofloyaltyrewards_eligible_offline%` | Count of unclaimed offline rewards the player currently qualifies for |
| `%rofloyaltyrewards_eligible_online%` | Count of online rewards the player currently qualifies for |
| `%rofloyaltyrewards_next_offline%` | ID of the next unclaimed offline reward |
| `%rofloyaltyrewards_next_offline_time%` | Time requirement of that next reward |

## Getting started

Take a look at my [wiki](https://github.com/RageOfFire/ROF_LoyaltyRewards/wiki) to know what to do next.

## Support

![GitHub issues](https://img.shields.io/github/issues/RageOfFire/ROF_LoyaltyRewards)
![GitHub pull requests](https://img.shields.io/github/issues-pr/RageOfFire/ROF_LoyaltyRewards)

## Author

* [RageOfFire](https://github.com/RageOfFire)
