
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
- The sample application relies on the presence of the RGIS Vision application. Before using the sample application, please ensure that the RGIS Vision application is installed and properly set up on your device.
- Currently, the sample application only works with the sample database downloaded from [this link](https://drive.google.com/file/d/1RJbKIzAuQVpvN7NPymL7On54Aj-96C-E/view?usp=sharing).
- For how to install and use on a device, see below section "Usage Scenario"

#### Download Sample Database
You can download the sample database from [this link](https://drive.google.com/file/d/1RJbKIzAuQVpvN7NPymL7On54Aj-96C-E/view?usp=sharing).

## Launching RGIS Vision
To launch the RGIS Vision application, you can use the `launchRgisVision()` function. Before calling this function, ensure that the following parameters are properly set:

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
After launching RGIS Vision, the response can be captured using the rgisVisionAppLauncher activity result launcher.

If the scan is successful, the result will be returned with a JSON response under `Activity.RESULT_OK`. If an error occurs during the scan, it will be returned under `Activity.RESULT_CANCELED`.

Make sure to handle both cases to properly respond to the RGIS Vision scan results.



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

### RGIS Vision Sample Application Usage Scenario
Below is an example scenario for using the RGIS Vision Sample application:

##### To install on a device (one time):
1. First download the "RGIS Vision" application provided to you > Open the app and allow both required permissions.
2. Then download the "RGIS Vision Sample" application provided to you.
3. Download the sample database from this link to your device.
##### To use and test after install:
1. In the Sample app load the downloaded database using the Select Database button.
2. Select the `products` table.
3. Choose `product_id` as the `Search Column`.
4. Select `product_name` or `product_description` as the `Description Column`.
5. Click the "Launch RGIS Vision" button to start the RGIS Vision application.

Once the RGIS Vision application is launched, you can scan the following example "product_id"s to observe the results:

- 10890EB0

- 138585D0

These two `product_id` will return a single result.

Additionally, to try a scenario where multiple results are found, scan the following "product_id":

- 2C5E2623


Note: To ensure accurate scanning, make sure that the scanning frame contains only the text to be scanned. Please ensure that there are no extra objects outside the frame.