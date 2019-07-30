/*
 * CognitiveScale Cortex Studio
 *
 * Copyright (c) Cognitive Scale, Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */

import * as React from 'react';

import { ReactElementWidget } from '@jupyterlab/apputils';

import { ContentMode } from '../../Common/ContentMode';
import { ImportContentBody } from '../../Components/ImportContentBody/ImportContentBody';

export interface ImportSelection {
    language: string;
    selection: string;
}

export class ImportContentWidget extends ReactElementWidget {
    mode: ContentMode;
    language: string;
    selection: string;

    constructor(mode: ContentMode) {
        super(<ImportContentBody mode={mode} handleSelectionChanged={(...args) => this.handleSelectionChanged(...args)} />);
        this.mode = mode;
        this.language = null;
        this.selection = null;
    }

    public getValue(): ImportSelection {
        return ({ language: this.language, selection: this.selection });
    }

    protected handleSelectionChanged = (language: string, selection: string) => {
        this.language = language;
        this.selection = selection;
    }
}