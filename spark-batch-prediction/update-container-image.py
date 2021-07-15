import sys
import json

# Load config.json as dictionary
d = json.load(open("config.json", 'rb'))
# Update spark container image
d["pyspark"]["options"]["--conf"]["spark.kubernetes.container.image"] = sys.argv[1]
# Save the updated config.json
json.dump(d, open("config.json", 'w'))