# Bulk SBOM generator:
This application allows for bulk SBOM generation. 
- For Upload and Scan, an SBOM json will be saved inside **target directory** for each application profile found.  
- For Agent-based, a directory will be created within the **target directory** for each workspace found and an SBOM json file saved for each project identified.

## Requirements:
- Java 8 installed
- A Veracode account with access to the SBOM functionality
- An SCA subscription
- API Credentials (ID and Key)
- Either:
  - One application available to the user that has been scanned using SCA Upload and Scan
  - One project containing SCA agent-based scan results

## How to use:
- Package the application using Maven
- Call the jar by passing the required parameters
- Example call: java -jar **jar-name** -vi **veracode-id** -vk **veracode-key** -td **target-directory** -s **source**

## Parameters:
All parameters are mandatory
- Veracode Credentials ID
  - --veracode_id or -vi
- Veracode Credentials Key
  - --veracode_key or -vk
- Target Directory
  - --target_directory or -td
- SBOM Source
  - --source or -s
  - Agent-based or Upload and Scan
