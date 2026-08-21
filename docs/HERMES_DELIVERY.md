# EmaraLeague — HDS Delivery Tracker

**Project:** EmaraLeague  
**Package:** `com.emaralabs.emaraleague`  
**Started:** 2026-08-21  
**Current Phase:** 0 — Planning & Discovery

---

## 18-Fasa HDS Checklist

| # | Fasa | Status | Nota |
|---|------|--------|------|
| 1 | **Initiate** — tentukan goal, scope, users, value prop | [x] | Tournament engine, worldwide premium plugin |
| 2 | **Discover** — research pasaran, pesaing, pricing | [x] | XLTournaments, zTournament analysed; pricing $24.99–$39.99 |
| 3 | **Define** — tulis requirements & acceptance criteria | [x] | REQUIREMENTS.md drafted |
| 4 | **Design** — architecture, data model, API contract | [x] | DESIGN_AND_PLAN.md completed |
| 5 | **Plan** — implementation plan with bite-sized tasks | [x] | DESIGN_AND_PLAN.md completed |
| 6 | **Setup** — repo, build system, dev environment | [x] | Monorepo structure + root build initialized |
| 7 | **Prototype** — proof-of-concept core loop | [ ] | State machine + arena reset |
| 8 | **Build Core** — implement engine foundation | [ ] | Arena, team, scheduler, data |
| 9 | **Build Modules** — built-in minigame modes | [ ] | Duels, Spleef, Sumo, TNT Run |
| 10 | **Integrate** — third-party plugins & APIs | [ ] | Vault, PAPI, LuckPerms |
| 11 | **Polish** — UI/UX, messages, config, docs | [ ] | GUI editor, multi-language |
| 12 | **Test** — unit, integration, server tests | [ ] | MockBukkit + paperweight dev server |
| 13 | **Secure** — audit permissions, SQL, configs | [ ] | Security review |
| 14 | **Optimize** — performance, Folia compatibility | [ ] | Async ops, profiling |
| 15 | **Package** — build, shadow jar, versioning | [ ] | Gradle shadow task |
| 16 | **Beta** — closed beta with real servers | [ ] | 5–10 beta testers |
| 17 | **Launch** — marketplace listings + website | [ ] | BuiltByBit, Spigot, Polymart, direct |
| 18 | **Post-Launch** — support, updates, add-ons | [ ] | Ranked addon, web dashboard roadmap |

---

## Current State

Semua skills dah diload. CONTEXT.md, REQUIREMENTS.md, dan HERMES_DELIVERY.md telah dipindahkan ke `EmaraLeague/docs/`. 

**Task aktif sekarang:** Per-module build.gradle.kts + Gradle wrapper + bootstrap plugin scaffold.

## Active Blockers

- Tiada.

## XP Tracking

| Sesi | Task | Complexity | XP |
|------|------|------------|----|
| 2026-08-21 | Brainstorm & idea validation | COMPLEX | 50 |
| 2026-08-21 | Initialize HDS documents | SIMPLE | 25 |
| TBD | Design architecture | HIGH-RISK | 100 |
| TBD | Write implementation plan | COMPLEX | 50 |

---

## Notes

- Nama plugin: **EmaraLeague**
- Root package: **com.emaralabs.emaraleague**
- Project parent folder: `C:\Users\haris\Minecraft Plugin\`
- HDS docs sekarang berada dalam `EmaraLeague/docs/` untuk elak protected-path issue.
- Jangan tulis code sehingga design doc + implementation plan diluluskan.
