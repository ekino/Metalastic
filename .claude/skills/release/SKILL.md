---
name: release
description: Prepare a Metalastic release — analyze commits, generate release notes, update version references, commit, tag, and push
disable-model-invocation: true
argument-hint: "[version]"
---

# Release Skill

You are preparing a new Metalastic release. Follow each step below carefully and in order. This process has **side effects** (commits, tags, pushes) — always confirm with the user before destructive or irreversible actions.

## Step 1 — Validate Prerequisites

1. **Parse version** from the argument `$ARGUMENTS`.
   - If empty or missing, show the latest tag (`git tag --sort=-v:refname | grep '^v' | head -1`) and the number of commits since it (`git rev-list {LAST_TAG}..HEAD --count`), then ask the user for the version number.
   - Strip any leading `v` prefix. Validate it matches `MAJOR.MINOR.PATCH` (all numeric).
   - Set `NEW_VERSION` to the validated version string (without `v`).

2. **Check tag doesn't exist**: `git tag -l "v{NEW_VERSION}"` — if output is non-empty, abort with a message.

3. **Check branch**: `git branch --show-current` — must be `main` or `master`. If not, warn and ask user whether to continue.

4. **Check clean working directory**: `git status --porcelain` — if non-empty, warn the user, show the dirty files, and ask whether to continue.

5. **Check up to date with remote**: Run `git fetch origin` then `git status -sb` to check ahead/behind. If behind, warn and suggest pulling first.

## Step 2 — Analyze Commits Since Last Release

1. Find the latest tag: `git tag --sort=-v:refname | grep '^v' | head -1` → `LAST_TAG`
2. Derive `OLD_VERSION` = `LAST_TAG` without the `v` prefix.
3. List commits since last release: `git log {LAST_TAG}..HEAD --oneline --no-merges`
4. Categorize each commit by its conventional commit prefix:
   - `feat` → Features
   - `fix` → Bug Fixes
   - `chore(deps)` → Dependency Updates
   - `chore` (other) → Maintenance
   - `refactor` → Refactoring
   - `docs` → Documentation
   - `test` → Tests
   - Other → Miscellaneous
5. Display a summary table with counts per category and the full categorized list.

## Step 3 — Determine Version Values

- `NEW_VERSION`: from Step 1
- `OLD_VERSION`: from Step 2
- `NEXT_SNAPSHOT`: propose `{NEW_VERSION}-SNAPSHOT` (matching the pattern in `gradle.properties`). Confirm with user.

## Step 4 — Generate Release Notes

Create `release-notes/RELEASE_NOTES_v{NEW_VERSION}.md` following the structure of existing release notes. Use the commit analysis from Step 2 to populate the content.

**Required structure** (adapt sections based on what's actually in the commits):

```markdown
# Metalastic v{NEW_VERSION} Release Notes

## [Category sections based on commit analysis]
- Group related changes under descriptive headers
- Include links to external release notes when upgrading dependencies
- Provide migration notes if relevant

## ✅ No Breaking Changes / ⚠️ Breaking Changes
- State clearly whether there are breaking changes
- If breaking, provide migration guide

## 🔗 Links
- **Documentation:** https://ekino.github.io/Metalastic/
- **Maven Central:** https://central.sonatype.com/search?q=g:com.ekino.oss
- **Gradle Plugin Portal:** https://plugins.gradle.org/plugin/com.ekino.oss.metalastic

## ⬆️ Upgrade Guide

### Using Gradle Plugin
[code block with NEW_VERSION, read current KSP version from versions.data.ts]

### Using Manual Dependencies
[code block with NEW_VERSION]

## 🙏 Credits
Thank you for using Metalastic! Report issues at https://github.com/ekino/Metalastic/issues
```

**Show the generated release notes to the user and ask for approval before proceeding.** The user may want to edit the content — if so, apply their changes.

## Step 5 — Update All Version References

Update the following 7 files. **Important:** For each file, search for the ACTUAL old version string present (versions may be inconsistent across files from past partial releases). Do not blindly assume `OLD_VERSION` is used everywhere — read each file first and replace what's actually there.

### 5.1 — Release notes file
Already created in Step 4: `release-notes/RELEASE_NOTES_v{NEW_VERSION}.md`

### 5.2 — CHANGELOG.md
- Add a new version section below the header, above the previous version entry:
  ```
  ## [{NEW_VERSION}] - {TODAY's DATE in YYYY-MM-DD format}
  ```
  with the categorized changes from Step 2.
- Update the `[Unreleased]` comparison link at the bottom to compare from `v{NEW_VERSION}`:
  ```
  [Unreleased]: https://github.com/ekino/Metalastic/compare/v{NEW_VERSION}...HEAD
  ```
- Add a new comparison link for the new version:
  ```
  [{NEW_VERSION}]: https://github.com/ekino/Metalastic/compare/v{OLD_VERSION}...v{NEW_VERSION}
  ```

### 5.3 — README.md
Replace ALL occurrences of the old Metalastic version with `{NEW_VERSION}`. This includes:
- Plugin version in code blocks (e.g., `version "1.2.1"` → `version "{NEW_VERSION}"`)
- Dependency coordinates (e.g., `metalastic-core:1.2.1` → `metalastic-core:{NEW_VERSION}`)
- Version compatibility table entries
- Any other version references

### 5.4 — docs/.vitepress/versions.data.ts
Update the version values:
- `metalastic: '{NEW_VERSION}'`
- `dsl.rolling: '{NEW_VERSION}'`
- `dsl.frozen55: '{NEW_VERSION}'`
- `dsl.frozen53: '{NEW_VERSION}'`

### 5.5 — gradle.properties
Update `localVersion` to `{NEXT_SNAPSHOT}`:
```
localVersion={NEXT_SNAPSHOT}
```

### 5.6 — build.gradle.kts
Update the fallback version string in the `else` branch (local development version) to `{NEXT_SNAPSHOT}`:
```kotlin
else -> project.findProperty("localVersion") as String? ?: "{NEXT_SNAPSHOT}"
```

### 5.7 — CLAUDE.md
Replace ALL Metalastic version references throughout the file. Search for the old version string and replace with `{NEW_VERSION}`. This appears in:
- Dependency coordinates
- Plugin version examples
- Version compatibility tables
- Published artifacts section
- Configuration examples
- Any other version references

After all updates, show a summary of files modified and key changes made.

## Step 6 — Run Build Checks

1. Run `./gradlew spotlessApply` to format any changed files.
2. Run `./gradlew clean build` to verify the full build passes.
3. If the build fails, display the error output and **stop**. Tell the user to fix the issue and re-run `/release {NEW_VERSION}`.

## Step 7 — Review and Confirm

Show the user a summary:
- Version: `v{OLD_VERSION}` → `v{NEW_VERSION}`
- Tag to create: `v{NEW_VERSION}`
- Snapshot version: `{NEXT_SNAPSHOT}`
- Files modified (list all 7)
- Build status: passed/failed
- Offer to show the full diff: `git diff`

Ask the user to confirm before committing. If they want changes, go back and apply them.

## Step 8 — Commit, Tag, and Push

1. **Stage** only the specific modified files (list them explicitly, no `git add .`):
   ```
   git add release-notes/RELEASE_NOTES_v{NEW_VERSION}.md CHANGELOG.md README.md docs/.vitepress/versions.data.ts gradle.properties build.gradle.kts CLAUDE.md
   ```

2. **Commit**:
   ```
   git commit -m "chore: prepare v{NEW_VERSION} release"
   ```

3. **Tag**:
   ```
   git tag v{NEW_VERSION}
   ```

4. **Push** (ask for final confirmation before this step — it's irreversible):
   ```
   git push origin {CURRENT_BRANCH} && git push origin v{NEW_VERSION}
   ```

5. **Display success message**:
   - Monitor GitHub Actions: `https://github.com/ekino/Metalastic/actions`
   - Verify Maven Central (15-30 min): `https://central.sonatype.com/search?q=com.ekino.oss`
   - Verify Gradle Plugin Portal: `https://plugins.gradle.org/plugin/com.ekino.oss.metalastic`
   - GitHub Release: `https://github.com/ekino/Metalastic/releases/tag/v{NEW_VERSION}`

## Error Handling

- **Version already tagged** → Abort with message showing existing tag
- **Not on main/master branch** → Warn and ask user whether to continue
- **Dirty working directory** → Warn, show dirty files, offer to continue
- **Build failure** → Stop, show error, suggest fixing and re-running
- **Push failure** → Note that commit and tag are local only. Suggest:
  - `git reset HEAD~1` to undo the commit
  - `git tag -d v{NEW_VERSION}` to remove the local tag
  - Fix the issue and re-run `/release {NEW_VERSION}`
