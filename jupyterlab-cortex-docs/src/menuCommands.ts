/*
 * CognitiveScale Cortex Studio
 *
 * Copyright (c) Cognitive Scale, Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */

import { JupyterLab } from "@jupyterlab/application";

import { importContent } from './Commands/importContent';
import { ContentMode } from './Common/ContentMode';

// tslint:disable no-namespace
export namespace CommandIDs {
    export const newFromExample = 'cortex:new-from-example';
    export const newFromTemplate = 'cortex:new-from-template';
}

export function addCommands(app: JupyterLab) {
    const { commands } = app;

    commands.addCommand(CommandIDs.newFromExample, {
        label: 'Example',
        caption: 'Populate your workspace with a Cortex example',
        execute: () => importContent(app, ContentMode.Examples)
    });

    commands.addCommand(CommandIDs.newFromTemplate, {
        label: 'Template',
        caption: 'Populate your workspace with a Cortex template',
        execute: () => importContent(app, ContentMode.Templates)
    });
}