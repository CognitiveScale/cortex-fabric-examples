import sys
import json
import pickle

# Load config.json as dictionary
d = json.load(open("config.json", 'rb'))
# Update spark container image
d["emr"]["options"]["--conf"]["spark.executorEnv.YARN_CONTAINER_RUNTIME_DOCKER_IMAGE"] = sys.argv[1]
d["emr"]["options"]["--conf"]["spark.yarn.appMasterEnv.YARN_CONTAINER_RUNTIME_DOCKER_IMAGE"] = sys.argv[1]
d["params"]["CORTEX_URI"] = sys.argv[2]
d["params"]["CORTEX_TOKEN"] = sys.argv[3]
# Save the updated config.json
json.dump(d, open("config.json", 'w'))

# pickle the config
pickle.dump(d, open('config.pickle', 'wb'))