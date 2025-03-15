[![Android APK Build and Release](https://github.com/AbdulRehman-Pro/BlurHashColorPalette/actions/workflows/release.yml/badge.svg?branch=main)](https://github.com/AbdulRehman-Pro/BlurHashColorPalette/actions/workflows/release.yml)

# BlurHash Color Palette

This Android application implements an image picker with BlurHash encoding, allowing users to select images, generate their BlurHash representation, and display them in a ViewPager2 with a smooth transformation effect.

## Features
- Image selection from gallery
- Encoding images to Base64 and BlurHash
- Extracting vibrant colors from images using Palette API
- Displaying images in a ViewPager2 with transformations (scaling, rotation, and fading effects)
- Saving and loading image data using shared preferences

## Dependencies
Ensure you have the following dependencies in your `build.gradle`:
```gradle
implementation 'androidx.viewpager2:viewpager2:1.0.0'
implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.5.1'
implementation 'androidx.recyclerview:recyclerview:1.2.1'
implementation 'com.github.bumptech.glide:glide:4.12.0'
implementation 'com.google.android.material:material:1.6.1'
```

## How It Works

### 1. Image Selection
- Uses `registerImagePicker` to allow users to pick images.
- Converts the selected image to Bitmap.
- Uses coroutines to encode images to Base64 and apply BlurHash processing.

### 2. ViewPager2 Configuration
- A custom `PageTransformer` applies:
  - Scaling (between 0.8f and 1f)
  - Rotation effect (up to 15 degrees)
  - Fading effect
- Loads images dynamically into the adapter and updates the UI based on selected items.

### 3. BlurHash Data Handling
- Extracts `darkMutedColor`, `lightVibrantColor`, and `vibrantColor` using the Palette API.
- Stores processed image data in a list and saves it locally.
- Displays processed images using a blurred background effect.

## Usage Instructions
1. Implement `Utils.generateBitmap()` to convert URIs to Bitmap.
2. Implement `Utils.saveBlurDataList()` and `Utils.getBlurDataList()` for local storage.
3. Ensure `BlurHashData` is correctly structured to hold image metadata.
4. Attach `dotsIndicator` to the ViewPager2 for visual navigation.

## UI Behavior
- Default state shows an image add prompt.
- When an image is selected, its processed BlurHash preview is displayed.
- Colors dynamically update UI elements to match extracted tones.

## Future Enhancements
- Add network integration for syncing stored images.
- Implement a caching mechanism to improve loading performance.
- Improve transition effects for better user experience.

