/*
 * CognitiveScale Cortex Studio
 *
 * Copyright (c) Cognitive Scale, Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */

import * as React from 'react';

import { AgGridReact } from 'ag-grid-react';
import 'ag-grid/dist/styles/ag-grid.css';
import 'ag-grid/dist/styles/ag-theme-balham.css';

import './DatasetInspector.css';

export interface IProps {
    selectedItem: any;
    selectedData: any;
}

export class DatasetInspector extends React.Component<IProps> {

    constructor(props: IProps) {
        super(props);
    }

    render() {

        return (
            <div className="ag-theme-balham"
                style={{ width: '100%', height: '100%' }}
            >
                <AgGridReact
                    columnDefs={this.props.selectedData.columns}
                    rowData={this.props.selectedData.rows}>
                </AgGridReact>
            </div>
        );

    }
}
