#!/usr/bin/env bash

PROJECT_NAME=$1

cortex models save model.json --project ${PROJECT_NAME}

cortex experiments save experiment.json --project ${PROJECT_NAME}

cortex experiments create-run run.json --project ${PROJECT_NAME}