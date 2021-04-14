import json
import pickle
import numpy as np
from cortex import Cortex
from sklearn import datasets
from sklearn.ensemble import RandomForestClassifier

if __name__ == '__main__':
    import os
    job_data = json.loads(os.environ["CORTEX_PAYLOAD"])
    params = json.loads(job_data)

    # Train model
    iris = datasets.load_iris()

    X = iris["data"][:, 3:]  # petal width
    y = (iris["target"] == 2).astype(np.int)

    clf = RandomForestClassifier(n_estimators=100, max_depth=2, random_state=0)
    clf.fit(X, y)

    # Save model
    pickle.dump(clf, open('./model.pickle', 'wb'))

    client = Cortex.client(api_endpoint=params["apiEndpoint"], project=params["projectId"], token=params["token"])
    experiment = client.experiment('sample_experiment')

    with open("./model.pickle", "rb") as model:
        with experiment.start_run() as run:
            run.log_artifact_stream("model", model)  # save model
            run.set_meta("algo", "RandomForestClassifier Model")  # save meta data
    print(f'Received: {params}')
