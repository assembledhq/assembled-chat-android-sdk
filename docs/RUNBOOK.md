# AssembledChat Android SDK Runbook

## Overview

The AssembledChat Android SDK is a library that enables Android apps to integrate Assembled's chat widget. This runbook covers building, testing, and releasing new versions.

**Repository:** https://github.com/assembledhq/assembled-chat-android-sdk

## Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or later
- **JDK 17** (required by the project's compile options)
- **Android SDK** with API 34 (compileSdk)
- **Git** for version control

## Quick Reference

| Task | Command |
|------|---------|
| Build SDK | `./gradlew assembledchat:assembleRelease` |
| Run unit tests | `./gradlew assembledchat:test` |
| Run example app | `./gradlew example:installDebug` |
| Run lint | `./gradlew assembledchat:lint` |
| Clean build | `./gradlew clean` |
| Full build + test | `./gradlew assembledchat:build` |

## Building the SDK

### Via Android Studio

1. Open the project root directory in Android Studio
2. Wait for Gradle sync to complete (watch the status bar)
3. Build via **Build > Make Project** (Cmd+F9 / Ctrl+F9)

### Via Command Line

```bash
# Debug build (for development)
./gradlew assembledchat:assembleDebug

# Release build (what gets published)
./gradlew assembledchat:assembleRelease

# Full build with lint
./gradlew assembledchat:build
```

## Testing Changes

### Running Unit Tests

```bash
./gradlew assembledchat:test
```

Test files are located at `assembledchat/src/test/kotlin/com/assembled/chat/`.

### Running the Example App

The example app uses a local project dependency, so changes to the SDK are immediately reflected.

```bash
# Install on connected device/emulator
./gradlew example:installDebug
```

Or in Android Studio: select the `example` run configuration and run it.

The example app provides four integration demos:
- **Activity-based** - Full-screen chat via `AssembledChatActivity`
- **View-based** - Embed `AssembledChatView` in XML layout
- **Fragment-based** - Embed `AssembledChatFragment`
- **Compose-based** - Use `AssembledChatComposable`

### Manual Testing Checklist

After making changes, verify these scenarios:

- [ ] Chat loads successfully in all 4 integration modes
- [ ] User data is passed correctly
- [ ] JWT token authentication works
- [ ] Chat open/close events fire correctly
- [ ] Error handling works (try invalid companyId)
- [ ] `disableLauncher=true` works (auto-opens without launcher button)
- [ ] Debug mode shows console logs
- [ ] Release build works (ProGuard/R8 doesn't strip required classes)

## Making Changes

### Development Workflow

1. Create a feature branch from `master`:
   ```bash
   git checkout master
   git pull origin master
   git checkout -b feature/your-feature-name
   ```

2. Make changes in `assembledchat/src/main/kotlin/`

3. Run unit tests:
   ```bash
   ./gradlew assembledchat:test
   ```

4. Test with the example app:
   ```bash
   ./gradlew example:installDebug
   ```

5. Commit and push:
   ```bash
   git add .
   git commit -m "Description of changes"
   git push -u origin feature/your-feature-name
   ```

6. Open a pull request on GitHub against `master`

### Project Structure

```
assembled-chat-android-sdk/
  assembledchat/                    # Library module (published to Maven Central)
    src/main/kotlin/com/assembled/chat/
      AssembledChat.kt              # Core SDK class with WebView management
      AssembledChatListener.kt      # Event listener interface
      AssembledChatView.kt          # XML layout-embeddable view
      models/
        AssembledChatConfiguration.kt  # Configuration data class
        ChatError.kt                   # Sealed error hierarchy
        UserData.kt                    # User data model
      network/
        MessageBridge.kt              # JavaScript-to-native bridge
      ui/
        AssembledChatActivity.kt      # Full-screen Activity
        AssembledChatComposable.kt    # Jetpack Compose integration
        AssembledChatFragment.kt      # Fragment integration
    src/test/kotlin/                  # Unit tests
  example/                           # Demo app (not published)
  .github/workflows/                 # CI/CD workflows
    ci.yml                           # Tests on PRs
    publish.yml                      # Publish on release
```

## Releasing a New Version

Releases are automated via GitHub Actions. The CI workflow publishes to Maven Central when you create a GitHub Release.

### Pre-Release Checklist

- [ ] All changes are merged to `master`
- [ ] All tests pass: `./gradlew assembledchat:test`
- [ ] Example app works with the changes
- [ ] You've decided on the new version number (semantic versioning)

### Release Process

1. **Update the version** in `gradle.properties`:
   ```bash
   # Edit gradle.properties
   VERSION_NAME=1.0.7  # or whatever version you're releasing
   ```

2. **Commit and push** to master:
   ```bash
   git add gradle.properties
   git commit -m "Bump version to 1.0.7"
   git push origin master
   ```

3. **Create a GitHub Release**:
   - Go to https://github.com/assembledhq/assembled-chat-android-sdk/releases
   - Click **"Draft a new release"**
   - **Tag:** `v1.0.7` (must match VERSION_NAME with a `v` prefix)
   - **Title:** `v1.0.7`
   - Click **"Generate release notes"** for auto-generated changelog
   - Click **"Publish release"**

4. **Monitor the CI workflow**:
   - Go to **Actions** tab to watch the publish workflow
   - It will run tests, build, and publish to Maven Central

5. **Verify the release**:
   - Check Maven Central: https://central.sonatype.com/artifact/io.github.assembledhq/assembledchat
   - Test installation in a fresh Android project:
     ```kotlin
     implementation("io.github.assembledhq:assembledchat:1.0.7")
     ```

### Versioning Scheme

We use **Semantic Versioning** (SemVer): `MAJOR.MINOR.PATCH`

- **MAJOR** - Breaking changes (e.g., 1.0.0 -> 2.0.0)
- **MINOR** - New features, backward compatible (e.g., 1.0.0 -> 1.1.0)
- **PATCH** - Bug fixes, backward compatible (e.g., 1.0.0 -> 1.0.1)

### Tag Format

This repo uses **v-prefixed tags** (e.g., `v1.0.7`, not `1.0.7`).

## CI/CD Workflows

### CI Workflow (`ci.yml`)

Runs on every push to `master` and every pull request:
- Runs unit tests
- Builds the release AAR
- Runs lint checks

### Publish Workflow (`publish.yml`)

Runs when a GitHub Release is published:
- Validates the tag format (`vX.Y.Z`)
- Verifies `VERSION_NAME` in `gradle.properties` matches the tag
- Runs tests
- Builds the AAR
- Publishes to Maven Central via Sonatype Central Portal
- Updates the GitHub Release with Maven Central link

### Debugging CI Failures

If a workflow fails:
1. Go to the **Actions** tab in GitHub
2. Click on the failed workflow run
3. Expand the failed step to see logs
4. Common issues:
   - **Version mismatch:** Ensure `VERSION_NAME` in `gradle.properties` matches the release tag (without the `v` prefix)
   - **Test failures:** Fix the failing tests and create a new release
   - **Signing errors:** Check that GitHub Secrets are configured correctly

## GitHub Secrets (One-Time Setup)

The publish workflow requires these secrets in the repository settings (`Settings > Secrets and variables > Actions`):

| Secret Name | Description |
|-------------|-------------|
| `MAVEN_CENTRAL_USERNAME` | Sonatype Central Portal username |
| `MAVEN_CENTRAL_PASSWORD` | Sonatype Central Portal password/token |
| `SIGNING_KEY_ID` | Last 8 characters of GPG key ID |
| `SIGNING_PASSWORD` | Passphrase for the GPG key |
| `SIGNING_SECRET_KEY` | Base64-encoded GPG private key |

**How to get these values:**

```bash
# Get the GPG key ID (last 8 chars of the long ID)
gpg --list-keys --keyid-format SHORT

# Export and base64-encode the private key
gpg --export-secret-keys YOUR_KEY_ID | base64
```

## Troubleshooting

### Gradle Sync Fails

- Verify JDK 17 is installed and configured in Android Studio
- Check that `JAVA_HOME` points to JDK 17
- Try **File > Invalidate Caches and Restart**

### Tests Fail with Log Errors

The SDK uses `try/catch` around Android `Log` calls for unit test compatibility. If adding new tests that use Android classes, mock them or catch `RuntimeException`.

### Release Tag Already Exists

Maven Central does not allow re-publishing the same version. You must use a new version number. If you need to re-release:
1. Delete the GitHub Release (but the tag may remain)
2. Increment the version (e.g., 1.0.7 -> 1.0.8)
3. Create a new release with the new tag

### Version Mismatch Error in CI

The publish workflow checks that `VERSION_NAME` in `gradle.properties` matches the release tag. If you see this error:
1. Update `VERSION_NAME` in `gradle.properties` to match your intended version
2. Commit and push to `master`
3. Delete the failed GitHub Release
4. Create a new release with the correct tag

## FAQ

**Q: How do I test against the published artifact instead of local source?**

In `example/build.gradle.kts`, temporarily replace:
```kotlin
implementation(project(":assembledchat"))
```
with:
```kotlin
implementation("io.github.assembledhq:assembledchat:1.0.7")
```

**Q: Where is the version defined?**

In `gradle.properties` as `VERSION_NAME`. This is read by `assembledchat/build.gradle.kts` for Maven publishing.

**Q: Can I undo a release?**

It's difficult and not recommended. While you can delete the GitHub Release, Maven Central artifacts cannot be removed once published. It's better to release a new version with fixes.

**Q: What if CI fails after I create a release?**

The release and tag will exist on GitHub, but nothing is published to Maven Central. Fix the issue, delete the failed release, bump the version, and create a new release.

**Q: What Gradle task does the actual publishing?**

`./gradlew publishAllPublicationsToMavenCentralRepository` - provided by the Vanniktech Maven Publish plugin configured with Sonatype Central Portal.
