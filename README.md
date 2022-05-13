# Bulk SBOM generator:
This application will load all Application Profiles from the account and will generate one SBOM for each. 
All SBOM json files will be saved to the target directory.

## Requirements:
- A Veracode account with the security lead role
- An SCA subscription
- API Credentials (ID and Key)
- At least one application available to the user that has been scanned using SCA Upload and Scan

## How to use:
- Package the application using Maven
- Call the jar by passing the required parameters

## Parameters:
All parameters are mandatory
- Veracode Credentials ID
  - --veracode_id or -vi
- Veracode Credentials Key
  - --veracode_key or -vk
- Target Directory
  - --target_directory or -td
