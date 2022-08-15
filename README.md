# Bulk SBOM generator:
This application reads all application profiles or agent-based SCA projects and generates an SBOM for each.  
- For Upload and Scan, all SBOM json files will be saved in **target directory**.  
- For Agent-based, a directory will be created within the **target directory** for each workspace and within, for each project, a json file will be saved.

## Requirements:
- Java 8 installed
- A Veracode account with the security lead role
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
