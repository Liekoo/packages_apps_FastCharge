# FastCharge for Redmi Note 13 4G (Sapphire)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com)
[![API](https://img.shields.io/badge/API-36%2B-brightgreen.svg)](https://android-arsenal.com/api?level=36)

A modern Android system app for controlling charging speeds on Xiaomi Redmi Note 13 4G (Sapphire) based on Poco F6 (peridot) with Material Expressive design.

## Overview

FastCharge provides a user-friendly interface to control your device's charging speed through three distinct modes. Built with Material Design 3 expressive theme, it seamlessly integrates into Android Settings and Quick Settings tiles.

## Features

- 🎨 **Material Expressive Design** - Modern UI following Material Design 3 guidelines
- ⚡ **Three Charging Modes** - Slow, Fast, and Super Fast charging
- 🎯 **Quick Settings Tile** - Easy access from notification shade

## Integration

### 1. Clone the Repository

```bash
git clone https://github.com/poco-f6-peridot/packages_apps_FastCharge -b master packages/apps/FastCharge
```

### 2. Add to Device Makefile

Add the following line to your device's `device.mk`:

```makefile
# FastCharge
$(call inherit-product, packages/apps/FastCharge/fastcharge.mk)
```

### 3. Build ROM

```bash
# Build the entire ROM
m bacon
```

## Screenshots

<p align="center">
  <img src="readme_resources/screenshot_1.png" width="250" />
  <img src="readme_resources/screenshot_2.png" width="250" />
  <img src="readme_resources/screenshot_3.png" width="250" />
  <img src="readme_resources/screenshot_4.png" width="250" />
</p>

## Charging Modes

| Mode | sport_mode | smart_chg |
|------|------------|-----------|
| **0 - Slow** | 0 | 0 |
| **1 - Fast** | 0 | 8 |
| **2 - Super Fast** | 1 | 8 |

## Kernel Patches Required

The following kernel commits are required for FastCharge to function:

1. **Initial FastCharge Implementation**
   - 
   - Adds basic fastcharge_enable node

2. **Multi-Mode Support**
   -
   - Implements 3-mode charging system

3. **Mode Detection Fix**
   - 
   - Fixes mode 1 detection for Xiaomi hardware quirk

## Usage

### Settings App
1. Open **Settings** → **Battery**
2. Tap **Fast Charge**
3. Select your preferred charging mode
4. The illustration updates to reflect your choice

### Quick Settings Tile
1. Pull down notification shade
2. Edit tiles and add **Fast Charge**
3. Tap the tile to cycle through modes:
   - Slow → Fast → Super Fast → Slow


## All Credits to:

- **Developer**: kenway214
- **Developer**: Liekoo
- **Based on Device**: Xiaomi Poco F6 (peridot)
- **Adapted to Saphire**: Redmi note 13 4G (sapphire)
- **Original Concept**: [YAAP FastCharge](https://github.com/yaap/packages_apps_FastCharge)
- **Inspiration**: Xiaomi TurboCharging implementation
- **Design**: Material Design 3 Expressive theme

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

---

**Made with ⚡ for Poco F6 (peridot)**
