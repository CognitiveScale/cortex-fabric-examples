/*
 * CognitiveScale Cortex Studio
 *
 * Copyright (c) Cognitive Scale, Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */

import { merge } from 'lodash/fp';

export class CortexClient {
    protected _auth: { headers: any };
    protected _baseUrl: string;

    constructor(jwt: string, baseUrl: string) {
        this._auth = { headers: { Authorization: `Bearer ${jwt}` } };
        this._baseUrl = baseUrl;
    }

    protected async api<T>(path: string, options?: any): Promise<T> {
        const url = new URL(path, this._baseUrl).href;
        const init = merge(this._auth, options);
        return fetch(url, init)
            .then(response => {
                if (!response.ok) {
                    throw new Error(response.statusText);
                }
                return response.json();
            });
    }
}