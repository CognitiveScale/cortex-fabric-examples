"""
Copyright (c) 2021. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Example Code [License](https://cognitivescale.github.io/cortex-fabric-examples/LICENSE.md)
"""
import pickle
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.tree import DecisionTreeClassifier
from sklearn.preprocessing import StandardScaler, OneHotEncoder


def encode(cat_columns):
    encoder = OneHotEncoder()
    # Encode Training Data
    encoded_train_df = encoder.fit_transform(x_train[cat_columns]).toarray()
    # Encode Training Data
    encoded_test_df = encoder.fit_transform(x_test[cat_columns]).toarray()
    return encoded_train_df, encoded_test_df, encoder


def normalize(numeric_columns):
    normalizer = StandardScaler()
    # Normalize Training Data
    numeric_train_df = normalizer.fit_transform(x_train[numeric_columns])
    # Normalize Training Data
    numeric_test_df = normalizer.fit_transform(x_test[numeric_columns])
    return numeric_train_df, numeric_test_df, normalizer


# Training German Credit Model using Decision Tree Classifier
def train_with_encoder(X):
    categorical_columns = ['checkingstatus', 'history', 'purpose', 'savings', 'employ', 'status', 'others',
                           'property', 'age', 'otherplans', 'housing', 'job', 'telephone', 'foreign']
    numerical_columns = [idx for idx in X.columns if idx not in categorical_columns]
    # encode categorical features
    encoded_train_df, encoded_test_df, encoder = encode(categorical_columns)

    # Normalize numerical features
    normalized_train_df, normalized_test_df, normalizer = normalize(numerical_columns)

    # Merge after encoding & normalizing
    train_df = np.concatenate((encoded_train_df, normalized_train_df), axis=1)
    test_df = np.concatenate((encoded_test_df, normalized_test_df), axis=1)

    # Train a decision tree classifier model
    d_tree_model = DecisionTreeClassifier(criterion='entropy', random_state=0)

    # Fit Model
    d_tree_model.fit(train_df, y_train.values)
    print("Model Trained Successfully!")

    # Metrics
    accuracy = d_tree_model.score(test_df, y_test.values)
    print("Model Accuracy: ", accuracy)

    model_obj = {"model": d_tree_model, "cat_columns": categorical_columns, "encoder": encoder, "normalizer": normalizer}

    return model_obj


# Training German Credit Model using Decision Tree Classifier without Encoder
def train_without_encoder():
    # Train a decision tree classifier model
    d_tree_model = DecisionTreeClassifier(criterion='entropy', random_state=0)

    # Fit Model
    d_tree_model.fit(x_train.values, y_train.values)
    print("Model Trained Successfully!")

    # Metrics
    accuracy = d_tree_model.score(x_test.values, y_test.values)
    print("Model Accuracy: ", accuracy)

    return d_tree_model


def train(filepath, is_encoded):
    global x_train, x_test, y_train, y_test
    data = pd.read_csv(filepath)
    # Separate Training & Label data
    y = data['outcome']
    X = data.drop('outcome', axis=1)

    # Splitting test and training data
    x_train, x_test, y_train, y_test = train_test_split(X, y, random_state=0)

    print(type(x_train), type(y_test))

    if is_encoded:
        # Train with Encoder
        model = train_with_encoder(X)
    else:
        # Train without Encoder
        model = train_without_encoder()

    # Save Model as pickle
    save_model(model)


def save_model(model):
    pickle.dump(model, open("../model/german_credit_model.pickle", "wb"))


if __name__ == '__main__':
    train(filepath="german_credit_eval.csv", is_encoded=True)
