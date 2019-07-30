/*
 * CognitiveScale Cortex Studio
 *
 * Copyright (c) Cognitive Scale, Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */

import { JupyterLab } from '@jupyterlab/application';
import { showDialog, Dialog } from '@jupyterlab/apputils';
import { PathExt } from '@jupyterlab/coreutils';
import { Contents, ContentsManager } from '@jupyterlab/services';

import { map, sortedUniq } from 'lodash/fp';

import { CommonDetails, ContentClient } from '../Clients/Content/ContentClient';
import { ContentMode } from '../Common/ContentMode';
import { ReactElementRenderer } from '../Common/ReactElementRenderer';
import { ImportContentWidget } from '../Widgets/ImportContent/ImportContent';

function createDirectories(contentsMgr: ContentsManager, files: string[]): Promise<Contents.IModel[]> {
    const directories = sortedUniq(map(x => PathExt.dirname(x), files));
    const promises = map(x => contentsMgr.save(x, { type: 'directory' }), directories);
    return Promise.all(promises);
}

async function importFile(
    client: ContentClient,
    contentsMgr: ContentsManager,
    mode: ContentMode,
    language: string,
    selection: string,
    filepath: string
): Promise<Contents.IModel> {
    const contentFn = mode === ContentMode.Examples ? client.getExampleFile.bind(client) : client.getTemplateFile.bind(client);
    const content: string = await contentFn(language, selection, filepath);
    const model = await contentsMgr.save(filepath, { content, type: 'file', format: 'text' });
    return model;
}

export async function importContent(app: JupyterLab, mode: ContentMode): Promise<Contents.IModel[]> {
    const { serviceManager } = app;
    const { contents } = serviceManager;

    const client = new ContentClient();
    const contentDetailsFn: (language: string, id: string) => Promise<CommonDetails> =
        mode === ContentMode.Examples ?
        client.getExampleDetails.bind(client) :
        client.getTemplateDetails.bind(client);

    const { button, value } = await showDialog({
        body: new ImportContentWidget(mode),
        buttons: [Dialog.cancelButton(), Dialog.okButton({ label: 'Import' })],
        renderer: new ReactElementRenderer(),
        title: `Import Cortex ${mode === ContentMode.Examples ? 'Example' : 'Template'}`
    });

    if (button.label === "CANCEL") {
        return [];
    }

    const { language, selection } = value;
    const { files } = await contentDetailsFn(language, selection);
    const promises = map(x => importFile(client, contents, mode, language, selection, x), files);
    await createDirectories(contents, files);
    const models = await Promise.all(promises);
    return models;
}