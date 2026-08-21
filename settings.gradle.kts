// Copyright (c) 2026 EmaraLabs. All Rights Reserved.
// EmaraLeague — proprietary monorepo settings.

rootProject.name = "EmaraLeague"

include(
    "core",
    "api",
    "bootstrap",
    "editor",

    "modules:duels",
    "modules:spleef",
    "modules:sumo",
    "modules:tnt-run",

    "integrations:vault",
    "integrations:placeholderapi",
    "integrations:luckperms",
    "integrations:playerpoints",

    "infrastructure:database",
    "infrastructure:cache",
    "infrastructure:config",
    "infrastructure:logging",
    "infrastructure:security",

    "addons:ranked",
    "addons:web-dashboard",
    "addons:discord",
    "addons:spectator-tools",
)
