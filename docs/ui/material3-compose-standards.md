# Material 3 + Compose Standards (BookShelf)

This document defines the UI baseline for `BookShelf` so new screens stay consistent, accessible, and adaptive.

## Scope

- Apply these rules to `app/src/main/java/com/partitionsoft/bookshelf/ui/**`.
- Use `BookShelfTheme` from `app/src/main/java/com/partitionsoft/bookshelf/ui/theme/Theme.kt` as the single app theme entry point.
- Prefer Material 3 components for new code; use Material 2 only where migration is still in progress.

## Current Theme Contract

- **Color tokens** live in `app/src/main/java/com/partitionsoft/bookshelf/ui/theme/Color.kt`.
- **Theme wrapper** in `app/src/main/java/com/partitionsoft/bookshelf/ui/theme/Theme.kt` provides:
  - Material 3 `colorScheme`
  - Material 2 compatibility (`MaterialTheme` aliases) for transitional screens
  - system bar adaptation for light/dark mode
- **Typography/Shapes** currently come from Material 2 `Type.kt` and `Shape.kt` for compatibility.

## Component Guidelines

### 1) Use Material 3 first

- For new UI, prefer `androidx.compose.material3` components:
  - `Scaffold`, `TopAppBar`, `NavigationBar`, `Card`, `Button`, `TextField`, `Snackbar`, `ModalBottomSheet`
- Avoid mixing M2/M3 in the same component unless necessary during migration.
- Keep imports explicit and consistent.

### 2) Layout and spacing

- Use `LazyColumn` / `LazyVerticalGrid` for lists and grids.
- Use fixed spacing scale: `4.dp`, `8.dp`, `12.dp`, `16.dp`, `24.dp`.
- Keep horizontal padding at `16.dp` for main content unless design needs edge-to-edge media.
- Prefer `GridCells.Adaptive(...)` for book grids to improve tablet/foldable behavior.

### 3) Readability for book cards

- Prevent text/image overlap:
  - Reserve explicit image area using `aspectRatio` or fixed height.
  - Keep title/subtitle in a separate column block with min height.
  - Clamp text with `maxLines` + `TextOverflow.Ellipsis`.
- Maintain touch target >= `48.dp` for all interactive elements.

### 4) Accessibility baseline

- Every interactive icon needs `contentDescription`.
- Decorative images should use `contentDescription = null`.
- Keep contrast compliant by using theme colors (`onSurface`, `onSurfaceVariant`, etc.) instead of hardcoded colors.
- Ensure font scaling does not break card layouts.

### 5) State and architecture

- Keep screen state in ViewModel (`MVI/UDF` flow used by project).
- Hoist state from reusable composables.
- Use immutable UI models for sections (featured, categories, recommendations).
- Side effects (snackbar, navigation, one-time events) should be isolated from pure rendering.

### 6) Performance and stability

- Use stable keys in lazy lists where possible.
- Avoid expensive mapping in composables; prepare display models in ViewModel.
- Use `collectAsStateWithLifecycle()` for Flow/StateFlow in screens.
- Use shimmer placeholders only while loading; replace promptly on success/error.

## Home Screen Section Pattern

For each section (for example Featured, Categories, Popular):

1. Header row: title + "See all" action.
2. Loading: section-level shimmer.
3. Success: horizontal list/grid with stable keys.
4. Error: inline retry card (non-blocking for other sections).
5. Empty: lightweight informative state.

This keeps the home feed resilient when one API call fails or rate limits.

## API Rate-Limit UX Rules

When remote APIs are exhausted/unavailable:

- Show user-friendly message (not raw backend text).
- Keep previously loaded data visible when possible.
- Provide retry action and optional "Try again later" guidance.
- Avoid full-screen hard failure if at least one section has data.

## Migration Direction (M2 -> M3)

- Existing M2 compatibility in `BookShelfTheme` is intentional.
- New screens must be M3-only.
- When touching old M2 screens, migrate incrementally:
  1. Replace M2 components with M3 equivalents.
  2. Replace M2 typography usage with M3 typography usage.
  3. Remove obsolete aliases/imports once migration completes.

## Definition of Done (UI)

A screen/feature is done only if:

- It follows theme tokens and Material 3 styles.
- It handles loading/success/error/empty states.
- It is accessible (content descriptions, touch targets, contrast).
- It is adaptive (phone + larger widths).
- It passes visual sanity checks in light and dark mode.

