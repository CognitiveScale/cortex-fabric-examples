/*
 * CognitiveScale Cortex Studio
 *
 * Copyright (c) Cognitive Scale, Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */

import { CortexClient } from './CortexClient';

export interface Dataframe {
    columns: Array<string>;
    values: Array<Array<any>>;
}

export interface Dataset {
    name: string;
    title: string;
}

interface ListDatasetsResponse {
    datasets: Array<Dataset>;
}

export class DatasetsClient extends CortexClient {
    private static Endpoints = {
        dataset: `v3/datasets/:NAME/dataframe`,
        datasets: 'v3/datasets',
    };

    constructor(jwt: string, baseUrl: string) {
        super(jwt, baseUrl);
    }

    async getDataframe(datasetName: string) : Promise<Dataframe | null> {
        const endpoint = DatasetsClient.Endpoints.dataset.replace(':NAME', datasetName);
        return this.api<Dataframe>(endpoint);
    }

    async listDatasets() : Promise<Array<Dataset>> {
        const response = await this.api<ListDatasetsResponse>(DatasetsClient.Endpoints.datasets);
        return response.datasets;
    }
}