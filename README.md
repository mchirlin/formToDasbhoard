## Appian System Tools

### Server Logs Tools
Original Author: Estelle Peterson

- **listLogFiles()** function
  - Description: Lists available server log file names from the file system
  - Inputs: N/A
  - Outputs: Returns a list (String[]) of server file names
    
- **Get Log Files** smart service
  - Description: Creates an appian document and copies the log file content from the file system
  - Inputs: 
	    - List of log files to generate
	    - Destination folder location to save log files in Appian
  - Outputs:
	    - The generated document of the server log file

### Process Model Settings Tools
Original author: Michael Chirlin

- Process Model Settings Application
  - Small Application that queries all process models and then allows for bulk updating of the settings
- **Get Process Model Settings** smart service
     - Description: Gets the Security, Cleanup, and Alert Settings for every process model the user has access to
     - Inputs:
       - None
     - Outputs
      - List of ProcessModelSettings (CDT)
- **Set Process Model Settings** smart service
    - Description: Bulk sets the Security, Cleanup, or Alert Settings for the select process models
    - Inputs:
      - Process Model Ids (not uuids)
      - Security Settings 
	        - Update Security (Boolean)
	        - Admin Users and Groups
	        - Editor Users and Groups
	        - Manager Users and Groups
	        - Viewer Users and Groups
	        - Initiator Users and Groups
	        - Deny Users and Groups
      - Cleanup Settings
	        - Update Cleanup (Boolean)
	        - Cleanup Type
	        - Cleanup Delay
      - Alert Settings
	        - Update Alerts (Boolean)
	        - Is Custom
	        - Is Notify Initiator
	        - Is Notify Owner
	        - Is Notify By Expression
	        - Expression
	        - Is Notify Users and Groups
	        - Users and Groups
    - Outputs:
      - None 

### Identify Applications Containing Object Tool

Original author: jason.ruvinsky@appian.com

- Identify Apps Containing Object application

- **Identify Applications Containing Object** smart service
	- Description: Will scan all Appian Applications and identify which applications contain a specified Appian object.
	- Inputs:
		 - Object Id - The ID of the object to scan for. If not provided, then Object Uuid will be used.
		 - Object Uuid - The UUID of the object to scan for. This may be used instead of Object Id.
		 - Object Type - The Appian type of the object to scan for.
		 - Include Draft Applications - If true, this will scan all applications including draft applications. If false, this will only scan published applications.

	- Outputs:
 	   - Application Names - A list of the names of all applications containing the object.
 	   - Application URLs - A list of the urls of all applications containing the object.

### Get Components from Application Zip Tool
Original author: Michael Chirlin

- **Get Components from Application Zip** smart service
	- Description: This plugin provides a Smart Service for gathering component data from an application zip file. Includes a mobile enabled application that compares previous uuids to submitted uuids.

  - Inputs
     - Appian Document that is the application zip

  - Outputs
      - Array of Component CDT