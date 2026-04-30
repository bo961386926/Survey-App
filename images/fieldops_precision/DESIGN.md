# Design System Documentation: Field Precision & Tonal Depth

## 1. Overview & Creative North Star: "The Authoritative Lens"

This design system moves beyond the "utilitarian" toward "The Authoritative Lens." In the context of field inspections, reliability isn't just about functionality; it is about providing a workspace that feels stable, intelligent, and unbreakable. 

We break the "standard app" mold by moving away from thin lines and flat grids. Instead, we embrace **Architectural Layering**. The UI is treated as a high-precision instrument, utilizing intentional asymmetry in layouts to guide the eye and high-contrast typography scales that mirror editorial design. By utilizing a "Tonal-First" approach, we ensure that even in high-glare outdoor environments, the hierarchy remains unmistakable.

---

## 2. Colors: Tonal Architecture

Our palette is anchored in deep, nocturnal blues (`primary`) and industrial slate greys (`secondary`). This creates a sophisticated, high-contrast environment that minimizes eye strain while emphasizing critical data.

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using 1px solid borders to section off content. Boundaries must be defined solely through background color shifts. For example, a `surface-container-low` section sitting on a `surface` background provides all the definition required. This creates a more expansive, premium feel that avoids the "boxed-in" look of legacy software.

### Surface Hierarchy & Nesting
Treat the UI as a physical stack of materials.
*   **Base Layer:** `surface` (#f7f9fb)
*   **Sectioning:** `surface-container-low` (#f2f4f6)
*   **Interactive Cards:** `surface-container-lowest` (#ffffff) for maximum "lift."
*   **System Overlays:** `surface-container-highest` (#e0e3e5) for tertiary navigation or secondary map controls.

### The "Glass & Gradient" Rule
To elevate the utility beyond a basic form-filler, use **Glassmorphism** for floating map controls. Apply `surface` with a 70% opacity and a 12px backdrop-blur. 

### Signature Textures
Main Action Buttons (CTAs) should not be flat. Apply a subtle linear gradient from `primary` (#002446) to `primary-container` (#1a3a5f) at a 135-degree angle. This provides a tactile "clickability" and visual depth that signifies a premium, reliable tool.

---

## 3. Typography: Editorial Utility

The typography strategy pairs **Manrope** for structural authority and **Inter** for data-heavy legibility.

*   **Display & Headline (Manrope):** Large, bold, and unapologetic. Use `display-md` (2.75rem) for summary stats on inspection dashboards. The wide geometric stance of Manrope suggests stability.
*   **Title & Body (Inter):** Inter is our workhorse. Use `title-md` (1.125rem) for form labels to ensure they are legible at arm's length in the field. 
*   **Labels (Inter):** Small-scale data (`label-sm`) should always be in `on-surface-variant` (#43474e) to distinguish metadata from primary user input.

**Hierarchy Note:** High-contrast scales are key. A `headline-sm` title should be immediately followed by a `body-md` description to create a clear "hook and read" flow.

---

## 4. Elevation & Depth: Tonal Layering

We convey hierarchy through **Tonal Layering** rather than traditional structural lines or heavy shadows.

*   **The Layering Principle:** Stacking surface tiers creates a soft, natural lift. A `surface-container-lowest` card placed on a `surface-container-low` background creates enough contrast to signify importance without visual clutter.
*   **Ambient Shadows:** If a floating element (like a map FAB) requires a shadow, it must be an "Ambient Shadow": Use `on-surface` (#191c1e) at 6% opacity with a 24px blur and 8px Y-offset. It should feel like a soft glow, not a hard drop.
*   **The "Ghost Border" Fallback:** If a border is required for accessibility in high-glare environments, use the `outline-variant` (#c3c6cf) at **15% opacity**. Never use a 100% opaque border.

---

## 5. Components

### Buttons & Touch Targets
*   **Primary Button:** Gradient-filled (`primary` to `primary-container`), `lg` (0.5rem) roundedness. Minimum height: 56px for field-optimized touch.
*   **Secondary/Tertiary:** No background. Use `primary` text with a `surface-variant` hover state.

### Input Fields & Forms
*   **The Container:** Use `surface-container-low` for the input field background. 
*   **The Focus State:** Shift the background to `surface-container-lowest` and apply a 2px "Ghost Border" using `primary`. 
*   **Validation:** Use `error` (#ba1a1a) text for helper messages, but use `error_container` (#ffdad6) for the field background to ensure the error is unavoidable.

### Cards & Lists
*   **No Dividers:** Forbid the use of 1px lines between list items. Use 16px of vertical whitespace and a slight tonal shift (`surface-container-low` to `surface-container-highest`) to separate entries.
*   **Map Chips:** Floating action chips should utilize the Glassmorphism rule (70% opacity `surface` with blur) to ensure the map remains visible beneath the UI.

### Field-Specific Components
*   **Status Badges:** Use `tertiary_container` (#003779) for "In Progress" and `primary_fixed` (#d3e3ff) for "Complete." These should be pill-shaped (`full` roundedness) with `label-md` type.
*   **The Inspection Slider:** A custom large-scale slider for "Pass/Fail" gradients, utilizing the `secondary` slate grey for the track and `primary` for the thumb.

---

## 6. Do’s and Don’ts

### Do:
*   **Do** use asymmetrical margins (e.g., 24px left, 16px right) on tablet views to create an editorial feel.
*   **Do** prioritize `surface-container` shifts over lines to define data groups.
*   **Do** ensure all interactive elements have a minimum 48x48px hit area, even if the visual asset is smaller.

### Don’t:
*   **Don't** use pure black (#000000) for text. Always use `on-surface` (#191c1e) to maintain the slate-blue tonal profile.
*   **Don't** use standard 1px borders for "Card" containers. Let the background color do the work.
*   **Don't** crowd the interface. If a field technician is wearing gloves or moving, they need "Breathing Room"—use the `xl` (0.75rem) or `lg` (0.5rem) spacing tokens generously between functional groups.