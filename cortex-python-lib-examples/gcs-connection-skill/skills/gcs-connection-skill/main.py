import os
import sys
import json
import tempfile
from urllib.parse import urlparse
from contextlib import contextmanager
from cortex import Cortex
from google.cloud import storage

# file where connection will be saved
CONNECTION_FILE_NAME = 'gcs_conn.csv'

@contextmanager
def gcs_client(service_account_key):
    """Context managed GCS client using the given (JSON string) service account key."""
    # The service account key needs to be saved to a file to authenticate to the Google API
    with tempfile.NamedTemporaryFile() as service_account_file:
        with open(service_account_file.name, 'w+') as f:
            json.dump(json.loads(service_account_key), f)
        os.environ['GOOGLE_APPLICATION_CREDENTIALS'] = service_account_file.name
        yield storage.Client()
        os.environ.pop('GOOGLE_APPLICATION_CREDENTIALS', '')


def download_connection_data_from_gcs(connection, download_location):
    """Downloads the file (blob) referenced by a GCS Cortex connection."""
    params = connection['params']
    connection_uri = next((x for x in params if x['name'] == 'uri'), {}).get('value')
    service_account_key = next((x for x in params if x['name'] == 'serviceAccountKey'), {}).get('value')

    # extract the bucket + blob name from URI - need explicit values for Google client
    _, bucket_name, blob_name, _, _, _ = urlparse(connection_uri)
    blob_name = blob_name[1:]  # remove leading slash from path

    with gcs_client(service_account_key) as storage_client:
        bucket = storage_client.bucket(bucket_name)
        blob = bucket.blob(blob_name)
        blob.download_to_filename(download_location) # download the file


def main(params):
    # get connection from cortex
    client = Cortex.from_message(params)
    connection_name = params['payload']['connection_name']
    connection = client.get_connection(connection_name)

    # verify that we can download the file from GCS
    with tempfile.TemporaryDirectory() as tmp_dirname:
        destination = os.path.join(tmp_dirname, CONNECTION_FILE_NAME)
        download_connection_data_from_gcs(connection, destination)
    return connection_name, destination

if __name__ == '__main__':
    params = sys.argv[1]
    params = json.loads(params)
    try:
        conn_name, destination = main(params)
        print(f'Connection: {conn_name} read successfully')
    except json.JSONDecodeError as je:
        print(f'Failed to decode GCS service account key, verify it was saved as JSON: {str(je)}')
    except Exception as e:
        print(json.dumps({'connection_name': params['payload'].get('connection_name', ''), 'status': 'failed', 'error': str(e)}))
        raise
