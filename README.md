# ShakeToBlack

ShakeToBlack is an Android application that allows users to overlay a dark screen by shaking their device. This feature is particularly useful for dimming the screen or overlaying a dark filter in low-light environments, especially when listening to audio from video applications like YouTube without needing to see the screen.

## Features

- **Shake Detection**: Detects device shakes using the accelerometer sensor.
- **Adjustable Opacity**: Users can adjust the opacity of the overlay to their preference.
- **Shake Sensitivity**: Customize the sensitivity of shake detection.
- **Touch Screen Control**: Enable or disable touch interaction on the overlay.

## Permissions

ShakeToBlack requires the following permissions:

- `SYSTEM_ALERT_WINDOW`: Allows drawing overlays on top of other apps.
- `FOREGROUND_SERVICE`: Enables running services in the foreground.
- `BODY_SENSORS`: Grants access to the accelerometer sensor for shake detection.
- `FOREGROUND_SERVICE_MEDIA_PROJECTION`: Ensures compatibility for foreground services.

## Usage

1. Launch the app.
2. Shake your device to activate the overlay.
3. Adjust opacity and sensitivity using the seek bars.
4. Enable or disable touch interaction as needed.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
