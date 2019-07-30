/*
 * CognitiveScale Cortex Studio
 *
 * Copyright (c) Cognitive Scale, Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */

import * as React from 'react';
import * as _ from 'lodash';
import Tree, { TreeNode } from 'rc-tree';

import 'rc-tree/assets/index.css';
import './ExplorerComponent.css';
import {PythonVarInfo} from "../../Widgets/CortexPanel/CortexPanel";

export interface IProps {
    datasets: any[];
    skills: any[];
    notebookVars: PythonVarInfo[];
    onSelect: (type: string, obj: any) => void;
}

export interface IState {
}

export class ExplorerComponent extends React.Component<IProps, IState> {

    constructor(props: IProps) {
        super(props);
    }

    static nodeSelected(keys: any, evt: any) {
        evt.node.props.data();
    }

    datasetsList() {
        const datasets = _.map(this.props.datasets, (o) => <TreeNode
            title={o.name}
            key={`datasets-${o.name}`}
            data={() => this.props.onSelect('dataset', o) }
            className='skillab-container__groupentry'
        />);
        return (datasets.length) ? <TreeNode
                title="Datasets"
                key="cortex-datasets"
                selectable={false}
                className='explorer-component__groupname'
            >
            {datasets}
        </TreeNode>
            : null;
    }

    skillsList() {
        const skills = _.map(this.props.skills, (o) => <TreeNode
            title={o.name}
            key={`skills-${o.name}`}
            data={() => this.props.onSelect('skill', o) }
            className='explorer-component__groupentry'
        />);
        return (skills.length) ? <TreeNode
                title="Skills"
                key="cortex-skills"
                selectable={false}
                className='explorer-component__groupname'
            >
            {skills}
        </TreeNode>
            : null;
    }

    varsList() {
        const vars = _.map(this.props.notebookVars, (o) => <TreeNode
            title={`${o.name} - ${o.type}`}
            key={`vars-${o.name}`}
            data={() => this.props.onSelect('var', o) }
            className='explorer-component__groupentry'
            icon={null}
        />);
        return (vars.length) ? <TreeNode
                title="Notebook Objects"
                key="cortex-runtime"
                selectable={false}
                className='explorer-component__groupname'
            >
                {vars}
            </TreeNode>
            : null;
    }

    render() {
        return (
            <div className={'explorer-component'}>
                <Tree
                    showLine
                    defaultExpandAll
                    autoExpandParent
                    onSelect={(keys:string[], evt:any) => { ExplorerComponent.nodeSelected(keys, evt) }}
                >
                    <TreeNode title="Cortex" key="cortex" selectable={false}>
                        {this.datasetsList()}
                        {this.skillsList()}
                        {this.varsList()}
                    </TreeNode>
                </Tree>
            </div>
        );
    }
}
