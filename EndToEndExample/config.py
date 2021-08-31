import os

PROJECT_ID = os.environ["PROJECT_NAME"]
API_ENDPOINT =  ""
# use `cortex configure token` to get token
CORTEX_TOKEN = ""

AWS_PUBLIC_KEY = ""
S3_BUCKET = "cortex-fabric-examples"
FILE_NAME = "german_credit_eval.csv"
URI = f"s3a://{S3_BUCKET}/{FILE_NAME}"
S3_ENDPOINT = "http://s3.us-east-1.amazonaws.com"

CONN_PARAMS = {
    "s3Endpoint": S3_ENDPOINT,
    "bucket": S3_BUCKET,
    "publicKey": AWS_PUBLIC_KEY,
    "secretKey": "#SECURE.awssecretadmin",
    "uri": URI
}
