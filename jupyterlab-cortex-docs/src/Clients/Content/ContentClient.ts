/*
 * CognitiveScale Cortex Studio
 *
 * Copyright (c) Cognitive Scale, Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */

import { URLExt } from "@jupyterlab/coreutils";
import { ServerConnection } from "@jupyterlab/services";

export interface CommonMetadata {
    id: string;
    title: string;
}

export type CommonMetadataList = Array<CommonMetadata>;

export interface CommonDetails {
    title: string;
    description: string;
    files: string[];
}

interface LanguagesResponse {
    languages: CommonMetadataList;
}

interface ExamplesResponse {
    examples: CommonMetadataList;
}

interface TemplatesResponse {
    templates: CommonMetadataList;
}

interface CommonDetailsResponse {
    details: CommonDetails;
}

/*
 * Utility function to make a request to the backend extension
 * @param
 */
function makeContentRequest(path: string, settings: ServerConnection.ISettings): Promise<Response> {
    const url = URLExt.join(settings.baseUrl, 'cortex', path);

    return ServerConnection.makeRequest(url, {}, settings).then(response => {
        if (response.status !== 200) {
            return response.text().then(data => {
                throw new ServerConnection.ResponseError(response, data);
            });
        }
        return response;
    });
}

export class ContentClient {
    serverSettings: ServerConnection.ISettings;

    constructor() {
        this.serverSettings = ServerConnection.makeSettings();
    }

    async getLanguages(): Promise<CommonMetadataList> {
        const response = await makeContentRequest('/languages', this.serverSettings);
        const { languages } = await response.json() as LanguagesResponse;
        return languages;
    }

    async getExamples(language: string): Promise<CommonMetadataList> {
        const response = await makeContentRequest(`/languages/${language}/examples`, this.serverSettings);
        const { examples } = await response.json() as ExamplesResponse;
        return examples;
    }

    async getExampleDetails(language: string, example: string): Promise<CommonDetails> {
        const response = await makeContentRequest(`/languages/${language}/examples/${example}`, this.serverSettings);
        const { details } = await response.json() as CommonDetailsResponse;
        return details;
    }

    async getExampleFile(language: string, example: string, path: string): Promise<string> {
        const response = await makeContentRequest(`/languages/${language}/examples/${example}/${path}`, this.serverSettings);
        const content = await response.text();
        return content;
    }

    async getTemplates(language: string): Promise<CommonMetadataList> {
        const response = await makeContentRequest(`/languages/${language}/templates`, this.serverSettings);
        const { templates } = await response.json() as TemplatesResponse;
        return templates;
    }

    async getTemplateDetails(language: string, template: string): Promise<CommonDetails> {
        const response = await makeContentRequest(`/languages/${language}/templates/${template}`, this.serverSettings);
        const { details } = await response.json() as CommonDetailsResponse;
        return details;
    }

    async getTemplateFile(language: string, template: string, path: string): Promise<string> {
        const response = await makeContentRequest(`/languages/${language}/templates/${template}/${path}`, this.serverSettings);
        const content = await response.text();
        return content;
    }
}