# Metalastic v2.0.7 - GitHub Migration Release

**Release Date**: January 18, 2025
**Type**: Infrastructure / Migration Release

---

## 🚀 Overview

This release marks the **official migration from GitLab to GitHub**! Metalastic is now hosted on GitHub with GitHub Actions for CI/CD and GitHub Packages for distribution.

### What's Changed

**No functional changes** - All Metalastic features remain exactly the same. This is purely an infrastructure migration.

---

## 🏗️ Infrastructure Changes

### GitHub Actions CI/CD
- ✅ **Build and Test Workflow** - Automatic build and test on PRs and master branch
- ✅ **Publish Workflow** - Automatic publishing to GitHub Packages on tag pushes
- ✅ **Manual Publish Workflow** - Manual snapshot publishing for testing

### GitHub Packages Distribution
- ✅ **Primary Distribution**: GitHub Packages (https://github.com/ekino/Metalastic/packages)
- ✅ **Dual Publishing**: Both GitLab and GitHub during transition period
- ✅ **Authentication**: GitHub token authentication for package consumption

### Build Configuration Updates
- ✅ Support for both GitLab CI and GitHub Actions environment variables
- ✅ Updated POM metadata to point to GitHub repository
- ✅ GitHub Packages repository configuration
- ✅ Maintained backward compatibility with GitLab CI

---

## 📦 Migration Guide for Consumers

### New Repository Configuration

**Update your `build.gradle.kts` or `build.gradle`:**

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/ekino/Metalastic")
        credentials {
            username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
            password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.token") as String?
        }
    }
}
```

### Authentication Setup

GitHub Packages requires authentication even for public packages:

1. **Create a GitHub Personal Access Token (PAT)**:
   - Go to: https://github.com/settings/tokens
   - Generate token with `read:packages` scope

2. **Configure credentials**:
   ```bash
   # Option 1: Environment variables
   export GITHUB_ACTOR=your-username
   export GITHUB_TOKEN=ghp_your_token

   # Option 2: gradle.properties
   gpr.user=your-username
   gpr.token=ghp_your_token
   ```

---

## 📅 Migration Timeline

### Current Status (v2.0.7)
- ✅ **GitHub**: Primary platform (https://github.com/ekino/Metalastic)
- ✅ **GitLab**: Still available (read-only, deprecated)
- ✅ **Dual Publishing**: Active during transition period

### Future Deprecation
- **2025-02-18** (30 days): GitLab packages will be marked as deprecated
- **2025-03-18** (60 days): GitLab repository will be archived
- **After 2025-03-18**: GitHub Packages is the only distribution channel

---

## 🔗 New Links

| Resource | Old (GitLab) | New (GitHub) |
|----------|-------------|--------------|
| **Repository** | gitlab.ekino.com/iperia/metalastic | **github.com/ekino/Metalastic** |
| **CI/CD** | GitLab CI | **GitHub Actions** |
| **Packages** | GitLab Maven Registry | **GitHub Packages** |
| **Issues** | GitLab Issues | **GitHub Issues** |
| **Pull Requests** | GitLab Merge Requests | **GitHub Pull Requests** |

---

## ⚠️ Breaking Changes

**None** - This is a drop-in replacement. All APIs, functionality, and version numbering remain unchanged.

---

## 📚 Documentation Updates

All documentation has been updated to reflect the new GitHub URLs:
- README.md
- TAG_MANAGEMENT.md
- CLAUDE.md
- Build configuration examples

---

## 🆘 Support

### Getting Help
- **Documentation**: https://github.com/ekino/Metalastic/blob/master/README.md
- **Issues**: https://github.com/ekino/Metalastic/issues
- **Discussions**: https://github.com/ekino/Metalastic/discussions

### Migration Issues?
If you encounter any issues during migration:
1. Check the [Migration Guide](#-migration-guide-for-consumers) above
2. Open an issue: https://github.com/ekino/Metalastic/issues
3. Include your build configuration and error messages

---

## 🎯 What's Next

Future releases will be published exclusively to GitHub Packages. Stay tuned for:
- Enhanced documentation
- Improved CI/CD workflows
- Community contributions via GitHub

---

## 📦 Published Artifacts

This release publishes to **both** repositories during the transition:

### GitHub Packages (Primary)
- `com.metalastic:core:2.0.7`
- `com.metalastic:processor:2.0.7`
- `com.metalastic:gradle-plugin:2.0.7`

### GitLab Maven Registry (Deprecated)
- Same artifacts, but marked as deprecated

---

## 🙏 Acknowledgments

Thank you to all users for your patience during this migration. We believe GitHub will provide a better experience for the Metalastic community!

---

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
