/*
 * CognitiveScale Cortex Studio
 *
 * Copyright (c) Cognitive Scale, Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */

import * as React from 'react';
// import * as _ from 'lodash';

import './SkillInspector.css';

export interface IProps {
    selectedItem: any;
    selectedData: any;
}

export class SkillInspector extends React.Component<IProps> {

    constructor(props: IProps) {
        super(props);
    }

    render() {

        return <div className='skill-inspector'>
                    <h2 className='skill-inspector__header'>Skill Title</h2>
                    <div className='skill-inspector__data'>{this.props.selectedData.title}</div>
                    <h2 className='skill-inspector__header'>Skill Name</h2>
                    <div className='skill-inspector__data'>{this.props.selectedData.name}</div>
                    <h2 className='skill-inspector__header'>Description</h2>
                    <div className='skill-inspector__data'>{this.props.selectedData.description}</div>
                </div>;
    }
}
