---
name: Precision Survey System
colors:
  surface: '#f9f9ff'
  surface-dim: '#d4daeb'
  surface-bright: '#f9f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f1f3ff'
  surface-container: '#e8eeff'
  surface-container-high: '#e2e8f9'
  surface-container-highest: '#dde2f3'
  on-surface: '#151c28'
  on-surface-variant: '#414755'
  inverse-surface: '#2a303d'
  inverse-on-surface: '#ecf0ff'
  outline: '#717786'
  outline-variant: '#c1c6d7'
  surface-tint: '#005bc1'
  primary: '#0058bc'
  on-primary: '#ffffff'
  primary-container: '#0070eb'
  on-primary-container: '#fefcff'
  inverse-primary: '#adc6ff'
  secondary: '#326578'
  on-secondary: '#ffffff'
  secondary-container: '#b5e7fe'
  on-secondary-container: '#36697c'
  tertiary: '#9e3d00'
  on-tertiary: '#ffffff'
  tertiary-container: '#bf551c'
  on-tertiary-container: '#fffbff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d8e2ff'
  primary-fixed-dim: '#adc6ff'
  on-primary-fixed: '#001a41'
  on-primary-fixed-variant: '#004493'
  secondary-fixed: '#baeaff'
  secondary-fixed-dim: '#9ccee4'
  on-secondary-fixed: '#001f29'
  on-secondary-fixed-variant: '#144d5f'
  tertiary-fixed: '#ffdbcd'
  tertiary-fixed-dim: '#ffb595'
  on-tertiary-fixed: '#351000'
  on-tertiary-fixed-variant: '#7c2e00'
  background: '#f9f9ff'
  on-background: '#151c28'
  surface-variant: '#dde2f3'
typography:
  h1:
    fontFamily: Manrope
    fontSize: 32px
    fontWeight: '700'
    lineHeight: '1.2'
    letterSpacing: -0.02em
  h2:
    fontFamily: Manrope
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.3'
    letterSpacing: -0.01em
  h3:
    fontFamily: Manrope
    fontSize: 20px
    fontWeight: '600'
    lineHeight: '1.4'
  body-lg:
    fontFamily: Manrope
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.6'
  body-sm:
    fontFamily: Manrope
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.5'
  label-caps:
    fontFamily: Manrope
    fontSize: 12px
    fontWeight: '600'
    lineHeight: '1'
    letterSpacing: 0.05em
  metric-value:
    fontFamily: Manrope
    fontSize: 30px
    fontWeight: '900'
    lineHeight: '1'
  tiny-label:
    fontFamily: Manrope
    fontSize: 10px
    fontWeight: '500'
    lineHeight: '1'
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  xs: 4px
  base: 8px
  sm: 12px
  md: 16px
  lg: 24px
  xl: 32px
  gutter-mobile: 12px
  margin-mobile: 20px
---

## Brand & Style
The brand personality is **Corporate Modern**, blending high-precision engineering aesthetics with professional SaaS reliability. It targets technical administrators and field managers who require a high-trust, data-dense environment.

The visual style is characterized by a "Clean Tech" approach: high-clarity interfaces, ample whitespace to separate complex data streams, and subtle rhythmic accents using the primary blue. The emotional goal is to evoke a sense of organized efficiency, accuracy, and real-time connectivity. It avoids unnecessary decorative elements in favor of functional clarity, using thin strokes and soft container tiers to establish hierarchy.

## Colors
The palette is rooted in a "Fidelity Blue" (#007AFF), which serves as the primary action color and brand identifier. This is supported by a sophisticated range of cool-tinted neutrals. 

- **Primary:** Used for key actions, active states, and brand iconography.
- **Secondary/Tertiary:** Reserved for specialized data visualization and status indicators.
- **Surfaces:** A multi-tiered white and off-white system. True white (#FFFFFF) is used for elevated cards and sidebars to pop against the subtle blue-grey background (#F9F9FF).
- **Semantics:** Success (Emerald), Warning (Orange), and Info (Blue) are used sparingly in low-saturation backgrounds with high-saturation text for status "chips".

## Typography
Manrope is the sole typeface, utilized for its modern, geometric construction that remains highly legible at small scales. 

Hierarchy is established through aggressive weight scaling—ranging from Medium (500) for UI labels to Black (900) for primary metrics. Headlines use tight letter-spacing to maintain a "locked-in" professional appearance. Navigation and system metadata utilize uppercase transformations with increased tracking to differentiate "UI chrome" from user-generated data.

## Layout & Spacing
The system employs a **Fixed-Fluid Hybrid** model. The sidebar is a fixed 240px width, while the main content canvas is fluid with a maximum container width of 1600px to prevent line-length fatigue on ultra-wide monitors.

A strict 8px base grid governs internal component spacing. Content is organized into a "Bento Grid" layout for dashboard metrics and a 2:1 ratio split for the main content vs. activity sidebar. Sections are separated by large 32px (xl) padding to create a distinct sense of modularity.

## Elevation & Depth
Depth is conveyed primarily through **Tonal Layering** and **Subtle Outlines** rather than heavy shadows.

- **Level 0 (Background):** #F9F9FF (Surface).
- **Level 1 (Cards/Sidebar):** #FFFFFF with a 1px border of #E0F0FF. This creates a "flat-plus" look.
- **Level 2 (Interaction):** Hover states on cards use a primary-tinted border (Primary/30%) and a soft `shadow-sm`.
- **Level 3 (Floating/Feedback):** Tooltips and primary buttons utilize `shadow-lg` with a color-matched tint (e.g., Primary/20%) to appear physically lifted.
- **Glassmorphism:** The top navigation bar uses a 80% opacity blur (`backdrop-blur-md`) to maintain context while scrolling.

## Shapes
The shape language is **Rounded**, conveying modern accessibility while remaining structured. 

Standard components (buttons, input fields, cards) use a 0.5rem (8px) radius. Secondary elements like metric containers and larger dashboard cards scale up to 1rem (16px) or 1.5rem (24px) for a softer, more "containerized" feel. Interactive pills (search bars, status badges) use a fully rounded/pill-shaped geometry to distinguish them from structural layout blocks.

## Components
- **Buttons:** Primary buttons are high-contrast with bold text and leading icons. Ghost buttons use #F5F7FA backgrounds for secondary actions.
- **Input Fields:** Search bars are pill-shaped with subtle background fills (#F5F7FA) and no borders, using icons as primary visual anchors.
- **Status Chips:** Use a "Soft Fill" style—10% opacity background of the semantic color with 100% opacity bold text for maximum legibility without visual noise.
- **Metric Cards:** Feature a vertical stack: Icon/Change-Indicator (Top), Label (Center), and Black-weighted Value (Bottom).
- **Activity Feed:** A vertical timeline component using a 1px #F0F8FF line with 32px circular icon nodes representing event types.
- **Progress Bars:** Ultra-thin (6px) rounded tracks using a neutral background and high-saturation primary/secondary fills.