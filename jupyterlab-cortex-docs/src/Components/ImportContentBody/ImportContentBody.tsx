/*
 * CognitiveScale Cortex Studio
 *
 * Copyright (c) Cognitive Scale, Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */

import * as React from 'react';
import { head } from 'lodash/fp';

import { ContentMode } from '../../Common/ContentMode';
import { ContentClient, CommonMetadataList, CommonDetails } from '../../Clients/Content/ContentClient';

import './ImportContentBody.css';

const OverviewHeadings = Object.freeze({
    [ContentMode.Examples]: 'Example Overview',
    [ContentMode.Templates]: 'Template Overview'
});

interface IProps {
    mode: ContentMode;
    handleSelectionChanged: (language: string, selection: string) => void;
}

interface IState {
    /** Details about the selected Cortex example or template */
    contentDetails: CommonDetails;
    /** ID of the currently selected language (for now, there's only one) */
    contentLanguage: string;
    /** List of available Cortex examples or templates for the selected language */
    contentList: CommonMetadataList;
    /** ID of the currently selected Cortex example or template */
    contentSelection: string;
}

export class ImportContentBody extends React.Component<IProps, IState> {
    client: ContentClient;
    contentListFn: (language: string) => Promise<CommonMetadataList>;
    contentDetailsFn: (language: string, id: string) => Promise<CommonDetails>;

    state: IState = {
        contentLanguage: null,
        contentList: [],
        contentDetails: null,
        contentSelection: null
    };

    constructor(props: IProps) {
        super(props);
        this.client = new ContentClient();
        this.contentListFn =
            props.mode === ContentMode.Examples ?
            this.client.getExamples.bind(this.client) :
            this.client.getTemplates.bind(this.client);
        this.contentDetailsFn =
            props.mode === ContentMode.Examples ?
            this.client.getExampleDetails.bind(this.client) :
            this.client.getTemplateDetails.bind(this.client);
    }

    async componentDidMount() {
        const languages = await this.client.getLanguages();
        const { id: contentLanguage } = head(languages);
        const contentList = await this.contentListFn(contentLanguage);
        const { id: contentSelection } = head(contentList);
        const contentDetails = await this.contentDetailsFn(contentLanguage, contentSelection);

        this.setState(
            () => ({ contentDetails, contentLanguage, contentList, contentSelection }),
            () => {
                const { contentLanguage: language, contentSelection: selection } = this.state;
                this.props.handleSelectionChanged(language, selection);
            }
        );
    }

    private handleContentSelected = async (event: React.ChangeEvent<HTMLSelectElement>): Promise<void> => {
        const { contentLanguage } = this.state;
        const contentSelection = event.target.value;
        const contentDetails = await this.contentDetailsFn(contentLanguage, contentSelection);
        this.setState(
            () => ({ contentDetails, contentSelection }),
            () => {
                const { contentLanguage: language, contentSelection: selection } = this.state;
                this.props.handleSelectionChanged(language, selection);
            }
        );
    }

    render() {
        const { mode } = this.props;
        const { contentDetails, contentList } = this.state;
        return (
            <div className="import-content-body">
                <select className="import-content-selection" onChange={this.handleContentSelected}>
                    {contentList.map(x => (<option key={x.id} value={x.id}>{x.title}</option>))}
                </select>
                <div className="import-content-details">
                    {contentDetails && <h3 className="import-content-overview-heading">{OverviewHeadings[mode]}</h3>}
                    {contentDetails && <div className="import-content-overview">{contentDetails.description}</div>}
                </div>
            </div>
        );
    }
}