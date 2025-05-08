# Changelog

## v1.2.0 - 08/05/2025
- Added `/task rename <old_name> <new_name>` command to rename tasks.
- Added `/task change <name> <new_description>` command to change the description of a task.
- Added `/follow online` command to see all players that you are following and are online.
- Added `/follow notification <staff/player> <on/off>` to hide/show notifications.
- `/follow color` command fixed.
- You will get notified now when a staff member or followed player joins/leaves the server. Staff members have their role mentioned.
- Partly fixed problem where color of the followed player overwrote badges.
- Fixed minor bugs.

## v1.1.0 - 07/05/2025
- Grouped all commands (no more `/seefollow` and `/removefollow`, but `/follow list` and `/follow remove`).
- Changed `/tools` to `/follow show <on/off>`.
- Unblocked all commands from the `/tools` infrastructure. `/follow hide` will just hide/show the colors of the followed players.
- Follow and todo data is properly saved now, so will not reset when you close minecraft.
- Fixed some minor bugs in the todo list.
- Updated command.md to view all commands.
- Changed the old name `EmcMod` to `Mod Toolkit` in the entire codebase.
- Added icon to the mod.

## v1.0.0 - 06/05/2025
Initial release of the project. Includes the following features:
- `Follow` system to follow other players.
- `Task` system to create and manage tasks.