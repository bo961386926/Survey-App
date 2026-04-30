---
name: Glacier Precision
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#3f4852'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#6f7883'
  outline-variant: '#bec7d4'
  surface-tint: '#00629d'
  primary: '#00629d'
  on-primary: '#ffffff'
  primary-container: '#00a3ff'
  on-primary-container: '#00375a'
  inverse-primary: '#98cbff'
  secondary: '#565e74'
  on-secondary: '#ffffff'
  secondary-container: '#dae2fd'
  on-secondary-container: '#5c647a'
  tertiary: '#006686'
  on-tertiary: '#ffffff'
  tertiary-container: '#4da6cd'
  on-tertiary-container: '#00394c'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#cfe5ff'
  primary-fixed-dim: '#98cbff'
  on-primary-fixed: '#001d33'
  on-primary-fixed-variant: '#004a77'
  secondary-fixed: '#dae2fd'
  secondary-fixed-dim: '#bec6e0'
  on-secondary-fixed: '#131b2e'
  on-secondary-fixed-variant: '#3f465c'
  tertiary-fixed: '#c0e8ff'
  tertiary-fixed-dim: '#7bd1fa'
  on-tertiary-fixed: '#001e2b'
  on-tertiary-fixed-variant: '#004d66'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  display:
    fontFamily: Manrope
    fontSize: 48px
    fontWeight: '700'
    lineHeight: '1.1'
    letterSpacing: -0.02em
  h1:
    fontFamily: Manrope
    fontSize: 32px
    fontWeight: '600'
    lineHeight: '1.2'
    letterSpacing: -0.01em
  h2:
    fontFamily: Manrope
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.3'
  h3:
    fontFamily: Manrope
    fontSize: 20px
    fontWeight: '600'
    lineHeight: '1.4'
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: '1.6'
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.6'
  body-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.5'
  label-caps:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: '1'
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 8px
  container-max: 1440px
  gutter: 24px
  margin-page: 40px
  card-padding: 24px
---

## Brand & Style

The design system is built upon the concept of "frozen clarity." It targets professionals in field survey management who require extreme precision and a sense of calm reliability while working in complex environments. The aesthetic prioritizes high-end technical utility through an Apple-inspired minimalist lens.

The visual style merges **Minimalism** with sophisticated **Glassmorphism**. By utilizing generous whitespace and translucent layers, the interface feels lightweight yet structurally sound. Every element is designed to feel like a high-precision instrument—cool to the touch, perfectly calibrated, and effortlessly premium. The emotional goal is to evoke confidence and reduce cognitive load through extreme visual order.

## Colors

The palette is anchored by **Glacier Blue**, a vibrant yet deep crystal blue that represents the core "action" and "precision" points of the system. The background is a sterile **Pure White**, providing a canvas that emphasizes clarity and light.

To support the primary blue, the system uses a deep Slate secondary color for high-contrast text and critical iconography. Glass surfaces are defined by a semi-transparent white with a high-saturation backdrop blur, creating a sense of depth without introducing heavy color shifts. Subtle cool-gray neutrals are used for borders and secondary metadata to maintain a monochromatic, "icy" professional feel.

## Typography

This design system utilizes a dual-font strategy to balance character with utility. **Manrope** is used for headlines to provide a modern, geometric, and technical appearance. It conveys structural integrity and matches the large corner radii of the layout.

**Inter** is the workhorse for all body text, data entries, and labels. Chosen for its exceptional legibility in professional software, it ensures that survey data remains readable under any field conditions. Letter spacing is slightly tightened for large headlines to maintain a premium "editorial" feel, while labels are occasionally tracked out for better scannability in dense management dashboards.

## Layout & Spacing

The layout follows a **Fixed Grid** philosophy for desktop dashboards, centering content within a 1440px container to ensure survey data remains focused and eye-level. A 12-column grid is employed with generous 24px gutters to allow the "breathable" minimalist aesthetic to thrive.

Spacing follows a strict 8px linear scale. Large internal margins (24px to 40px) within panels ensure that the interface never feels cramped, even when displaying complex field datasets. Use larger gaps between distinct sections to reinforce the visual hierarchy and the "Apple-esque" sense of space.

## Elevation & Depth

Depth in this design system is achieved through **Glassmorphism** and **Ambient Shadows** rather than traditional solid layering. 

1.  **The Base Layer:** Pure White (#FFFFFF).
2.  **The Glass Layer:** Used for navigation bars and floating panels. These surfaces use a `backdrop-filter: blur(20px)` and a thin 1px border of `white (20% opacity)` to simulate the edge of a glass pane.
3.  **Shadows:** Extremely soft, diffused shadows are used to lift cards. Avoid harsh blacks; instead, use a deep blue-tinted shadow (e.g., `rgba(0, 40, 80, 0.05)`) with a high blur radius (30px to 40px) and a slight Y-offset.

This creates a "stacked glass" effect that feels light and premium, suggesting that the software is a transparent window into the field data.

## Shapes

The shape language is defined by "squircle-like" smoothness. While the standard roundedness is set to Level 2 (8px), the primary container cards for this system utilize a specific **24px (1.5rem) radius** to achieve the signature premium look.

Smaller interactive elements like buttons and input fields should use a 12px radius to maintain harmony with the larger containers. This high degree of roundness softens the technical nature of survey management, making the professional tool feel as approachable as a high-end consumer application. Lines should be kept thin (1px) and used sparingly, letting the shapes and shadows define the boundaries.

## Components

### Buttons
Primary buttons use a solid Glacier Blue fill with white text. Secondary buttons should use a subtle light blue tint or a "ghost" style with a 1px border. All buttons feature a 12px corner radius and a subtle lift on hover.

### Cards
Cards are the primary container for survey data. They must feature the 24px corner radius, a soft ambient shadow, and no border. When used on top of a map or a photo background, they should transition to the frosted glass (glassmorphism) style.

### Inputs & Fields
Input fields are minimalist—pure white backgrounds with a subtle 1px gray-blue border. On focus, the border thickens slightly and glows with a soft Glacier Blue shadow. Labels always sit above the field in Inter (body-sm).

### Status Chips
For field survey status (e.g., "In Progress," "Verified," "Synced"), use pill-shaped chips with a low-saturation background and high-saturation text of the same hue. This maintains the clean, minimalist aesthetic while providing clear functional feedback.

### Data Tables
Tables should be borderless, using horizontal rules only in a very light gray. Header rows should be Manrope (label-caps) for a technical, high-precision look.