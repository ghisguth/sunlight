# Sunlight Project Overview

This project is a collection of 3D live wallpapers for Android, leveraging OpenGL ES 2.0 to render high-quality, animated backgrounds. The primary feature is the "Your Very Own Sun" wallpaper, which renders a customizable 3D sun and other star types.

## Architecture

The project is structured as a multi-module Gradle project:

- **`:sun`**: The main application module for the Sun live wallpaper.
- **`:blurredlines`**: A live wallpaper inspired by the "In the mist of web" demo.
- **`:limitlessgrid`**: A live wallpaper based on the blurred-lines project.
- **`:gfx`**: A library module containing OpenGL ES utilities (Shaders, Textures, FrameBuffers, etc.).
- **`:wallpaper`**: A library module providing base `WallpaperService` implementations for OpenGL.
- **`:shared`**: Shared utility code used across modules.
- **`:ux`**: User experience and UI components, including settings and color pickers.
- **`:demo`**: A demo application showcasing the graphics capabilities.

## Technologies

- **Language:** Java (Source level 21)
- **Graphics:** OpenGL ES 2.0 (GLSL shaders located in `gfx/res/raw` and module-specific `res/raw` folders)
- **Build System:** Gradle (Android Gradle Plugin 9.0.1)
- **Minimum SDK:** 24 (Android 7.0)
- **Target SDK:** 36 (Android 15)
- **Libraries:** AndroidX, Material Components, JUnit 4, Mockito

## Key Components

### Sun Wallpaper (`:sun`)
- `com.ghisguth.sun.Wallpaper`: The `WallpaperService` entry point.
- `com.ghisguth.sun.SunRenderer`: The core OpenGL renderer for the sun effect.
- `com.ghisguth.sun.WallpaperSettings`: Activity for configuring sun parameters (size, color, speed, etc.).
- **Features**: Material You dynamic colors, Direct Boot awareness, Surface frame-rate throttling (API 30+).

### Graphics Library (`:gfx`)
- `ShaderManager` & `Program`: Utilities for compiling and linking GLSL shaders.
- `TextureManager` & `Texture`: Utilities for loading and managing OpenGL textures.
- `FrameBuffer`: Support for off-screen rendering and post-processing effects.

## Building and Running

### Build Commands
To build the project and generate APKs for all application modules:
```bash
./gradlew assembleDebug
```

To build a specific module (e.g., the Sun wallpaper):
```bash
./gradlew :sun:assembleDebug
```

### Installation
To install the Sun wallpaper on a connected device:
```bash
./gradlew :sun:installDebug
```

### Testing
The project uses JUnit and Mockito for unit tests.
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Development Conventions

- **Graphics Resources:** Shaders are typically stored in `res/raw` as `.glsl` files.
- **Source Structure:** The project uses a custom source set configuration in Gradle, with Java files in `src` and resources in `res` (instead of the standard `src/main/java` and `src/main/res`).
- **Post-Processing:** Be mindful of performance on slower devices when adding new post-processing effects in the renderers.
- **Code Style:** Code is automatically formatted using **Spotless** and **Google Java Format (AOSP style)**. Run `./gradlew spotlessApply` to format changes.
- **Modernization:** Prioritize AndroidX components and Material You theming for all new UI additions.
