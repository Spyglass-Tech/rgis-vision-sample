
# RGIS Vision Sample Application

The purpose of this sample app is to aid the RGIS team in adapting their current RGIS Counter app to properly launch the product label reader app Spyglass is building called "RGIS Vision". This sample application is designed for testing and understanding the RGIS Vision integration. The sample application receives the necessary parameters to invoke the RGIS Vision application and demonstrates how this integration is implemented.

#### Responses and Errors
- If the RGIS Vision application cannot be launched, an error message will be displayed.
- If the integration is successful, the results and necessary operations will occur.

#### Warnings
- **IMPORTANT**: This version of RGIS Vision is not complete and is distributed strictly for integration purposes. Please do NOT show or share beyond the needs of the development team.
- This application is created for testing and understanding the RGIS Vision integration. Do not use real databases or sensitive data.
- This sample application can be used to test the RGIS Vision integration. It will help you understand how to provide parameters and observe the results.

#### Notes
- The sample application relies on the presence of the RGIS Vision application installed on the same device. Before using the sample application, please ensure that the RGIS Vision application is installed and properly set up on your device (including accepting all premissions requirements).
- For how to install and use on a device, see below section "Usage Scenario"
- If you are using an older version that is already installed, please uninstall the old version from your device before installing the new version.


## Launching RGIS Vision
To launch the RGIS Vision application, you can use the `launchRgisVision()` function. Before calling this function, ensure that the following parameters are properly set:

- `config_file_path`: Optionally, provide a configuration JSON file path.
- `database_file_path`: The file path of the database for scanning.
- `table_name`: The name of the target table for scanning.
- `search_column`: The name of the column for performing the search.
- `description_column`: The name of the column where descriptions are obtained.

```kotlin
private fun launchRgisVision() {
    val intent = Intent(Intent.ACTION_VIEW)

    intent.apply {
        component = ComponentName(
            getString(R.string.rgis_vision_app_package),
            getString(R.string.rgis_vision_app_scanner_activity)
        )

        putExtra(getString(R.string.key_config_file_path), selectedConfigPath) // optional
        putExtra(getString(R.string.key_database_file_path), selectedDatabasePath)
        putExtra(getString(R.string.key_table_name), selectedTable)
        putExtra(getString(R.string.key_search_column), selectedSearchColumn)
        putExtra(getString(R.string.key_description_column), selectedDescriptionColumn)
    }

    try {
        rgisVisionAppLauncher.launch(intent)
    } catch (e: ActivityNotFoundException) {
        showSnackBar(getString(R.string.error_rgis_vision_app_not_found))
    }
}
```

### Handling RGIS Vision Responses
- After launching RGIS Vision, the response can be captured using the rgisVisionAppLauncher activity result launcher.
- If the scan is successful, the result will be returned with a JSON response under `Activity.RESULT_OK`. If an error occurs during the scan, it will be returned under `Activity.RESULT_CANCELED`.
- IMPORTANT: Make sure to handle both cases to properly respond to the RGIS Vision scan results.



```kotlin
    private val rgisVisionAppLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { intent ->
                    intent.getBundleExtra(getString(R.string.key_rgis_vision_response))
                        ?.let { scanResponse ->
                            binding.tvScanResponseJson.text =
                                scanResponse.getString(getString(R.string.key_rgis_vision_response_json))
                        }
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                result.data?.let { intent ->
                    intent.getBundleExtra(getString(R.string.key_rgis_vision_exception))
                        ?.let { exceptionBundle ->
                            showSnackBar(
                                exceptionBundle.getString(getString(R.string.key_rgis_vision_exception))
                                    ?: "Unknown Error"
                            )
                        }
                }
            }
        }
```

## Configuration Settings
The RGIS Vision application's configuration can be set using two different methods:
### 1. Method: Settings Screen
When you open RGIS Vision, a settings screen welcomes users. On this screen, users can modify the settings of the RGIS Vision application, and these changes are saved as default settings.

### 2. Method: Invoking with a JSON File
Users can invoke RGIS Vision with their own applications by sending the file path of a JSON file containing configuration data in external storage. This method is only applicable for the specific scanning session, and the configuration sent via JSON is not saved as default settings.

## JSON Configuration File
Below is an example content for the JSON configuration file:

```json
{
  "camera_preview_size": "720x1280",
  "frame_height": 15,
  "frame_width": 85,
  "manuel_entry": true,
  "audible_feedback": true,
  "max_result": 3,
  "max_char": 25,
  "min_char": 5,
  "force_perfect_match": false,
  "genesis_engine_config": {
    "grammar_rules": [
      "%L~?",
      "%D%D%D%D%D%D%D%D%D%D[%D,%0][%D,%0][%D,%0][%D,%0]"
    ]
  }
}
```

## Configuration Settings
The RGIS Vision application can be customized with various configuration settings. Here are detailed explanations of these settings:

- **`camera_preview_size` (String):**
    - Resolution setting for the rear camera during scanning.
    - Values: "480x640", "720x1280", "1080x1920"
    - Recommended Value: "720x1280"

- **`frame_height` (Integer):**
    - Height setting of the scanning frame on the scanner screen.
    - Value Range: 10..25
    - Recommended Value: 15

- **`frame_width` (Integer):**
    - Width setting of the scanning frame on the scanner screen.
    - Value Range: 50..90
    - Recommended Value: 85

- **`manual_entry` (Boolean):**
    - Determines whether a button for manually entering the searched value will be displayed on the scanning screen.
    - Recommended Value: true

- **`audible_feedback` (Boolean):**
    - Play a beep sound on successful scans.
    - Recommended Value: true

- **`max_result` (Integer):**
    - Maximum number of matches to be displayed when a 100% match is not found during scanning.
    - Value Range: 1..3
    - Recommended Value: 3

- **`max_char` (Integer):**
    - Sets the maximum character length of the text to be scanned.
    - Value Range: 10..40
    - Recommended Value: 25

- **`min_char` (Integer):**
    - Sets the minimum character length of the text to be scanned.
    - Value Range: 5..9
    - Recommended Value: 5

- **`force_perfect_match` (Boolean):**
    - Specifies whether the scanned text should be found only with a 100% match.
    - Recommended Value: false

- **`genesis_engine_config` (Object):**
    - Contains custom configurations for the OCR engine.

    - **`grammar_rules` (String Array):**
        - Lists the grammar rules for the OCR engine.
        - If rules are not desired, only `"*"` should be added to the list.


### RGIS Vision Sample Application Usage Scenario
Below is an example scenario for using the RGIS Vision Sample application:

##### To install on a device (one time):
1. First download the "RGIS Vision" application provided to you > Open the app and allow both required permissions.
2. Then download the "RGIS Vision Sample" application provided to you.


##### To use and test after install:
1. In the Sample app load the database using the Select Database button.
2. Select the `your_target_table` table.
3. Choose `your_search_columns` as the `Search Column`.
4. Select `description_column`  as the `Description Column`.
5. Click the "Launch RGIS Vision" button to start the RGIS Vision application.
6. Optionally, provide a configuration JSON file using the second method mentioned above.

Note: To ensure accurate scanning, the scanning frame includes a central line. Please ensure that the text you want to scan is positioned precisely along this central line within the frame.