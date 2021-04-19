## Cortex OpenAPI Postman examples

The files contained in this folder are intended for use with Postman to assist users in familiarizing themselves with the Cortex Fabric OpenAPIs.

## How to use API Examples

1. Import both files to your Postman instance.

2. You must set the environment URL to your DCI instance and [your JWT token](https://cognitivescale.github.io/cortex-fabric/docs/getting-started/access/) in the example environment file.
    ```
    {
    	"id": "bc096431-54cd-488c-a1b3-85635cdb3b6f",
    	"name": "API-Example-environment",
    	"values": [
    		{
    			"key": "url",
    			"value": "<insert-your-dci-url-here>",
    			"enabled": true
        },
        {
          "key": "token",
          "value": "<your-JWT-token",
          "enabled": true
        },
    ```

3. Select a method from the collection and click send to obtain a sample output.
