# UI Development Checklists (BookShelf)

Use these checklists before opening or approving UI-related PRs.

## New Screen Checklist

- [ ] Wrapped in `BookShelfTheme` via app flow.
- [ ] Uses Material 3 components by default.
- [ ] Uses `Scaffold` + proper insets (`WindowInsets.systemBars`).
- [ ] Includes loading, success, empty, and error states.
- [ ] Uses state hoisting for reusable composables.
- [ ] Supports dark mode and light mode.
- [ ] No text/image overlap at common font scales.
- [ ] Interactive elements are at least `48.dp`.
- [ ] Content descriptions are defined for interactive elements.

## List/Grid Section Checklist

- [ ] Uses `LazyColumn` / `LazyRow` / `LazyVerticalGrid` (not large `Column` loops).
- [ ] Uses stable keys for list items.
- [ ] Shows shimmer placeholders only during loading.
- [ ] Uses section header + action pattern consistently.
- [ ] Error in one section does not break all sections.

## Search and App Bar Checklist

- [ ] Search bar colors come from theme (no default purple mismatch).
- [ ] Search behavior is resilient to API failure/rate limits.
- [ ] Search close/open transitions preserve expected text state.
- [ ] Snackbar and error copy are human-readable.

## Details and Reader Checklist

- [ ] CTA hierarchy is clear (`Read`, `Preview`, `Buy`, `Favorite`).
- [ ] Favorite action is visible and sized for touch.
- [ ] Buttons do not overlap bottom controls.
- [ ] PDF/EPUB reader controls remain reachable in portrait and landscape.

## Accessibility + UX Checklist

- [ ] Contrast uses theme semantic colors.
- [ ] Important actions do not rely on color alone.
- [ ] Typography hierarchy is clear (title/subtitle/meta).
- [ ] Empty states include action guidance.
- [ ] Animations and shimmer are subtle and non-blocking.

## PR Review Checklist (UI)

- [ ] Screenshots/video attached for light and dark themes.
- [ ] Behavior verified on narrow and wide screens.
- [ ] No hardcoded colors except tokens in theme files.
- [ ] No unnecessary Material 2 imports for new code.
- [ ] Existing tests updated or added when behavior changes.

