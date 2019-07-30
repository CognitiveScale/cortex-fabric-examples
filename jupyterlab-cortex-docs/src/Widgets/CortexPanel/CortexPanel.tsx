/*
 * CognitiveScale Cortex Studio
 *
 * Copyright (c) Cognitive Scale, Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */

import * as React from 'react';
import * as ReactDOM from 'react-dom';

import { JupyterLab } from '@jupyterlab/application';
import { Widget } from '@phosphor/widgets';
import { toArray } from '@phosphor/algorithm';
import { nbformat } from '@jupyterlab/coreutils';

import Debug from 'debug';
import * as _ from "lodash";
import { INotebookTracker, NotebookPanel } from "@jupyterlab/notebook";
import { TerminalManager, TerminalSession, KernelMessage, Kernel, Session, SessionManager } from "@jupyterlab/services";

import { ExplorerComponent } from "../../Components/ExplorerComponent/ExplorerComponent";
import { DatasetInspector } from "../../Components/Inspectors/DatasetInspector/DatasetInspector";
import { SkillInspector } from "../../Components/Inspectors/SkillInspector/SkillInspector";

import './CortexPanel.css';
import { CatalogClient, Skill, SkillListItem } from '../../Clients/Cortex/CatalogClient';
import { DatasetsClient, Dataset, Dataframe } from '../../Clients/Cortex/DatasetsClient';

const debug = Debug('CortexPanel');

export interface PythonVarInfo {
    name: string;
    module: string;
    type: string;
}

export interface InspectorDefinition {
    entityType: string;
    module?: string;
    type?: string;
    factory: any;
    queryFunction: any;
}

const _ipythonInit: string = `
import re
import json
from IPython.core.magics.namespace import NamespaceMagics

def _getCortexVars():
    nm = NamespaceMagics()
    nm.shell = get_ipython().kernel.shell
    varlist = nm.who_ls()

    cortexVars = []

    for val in varlist:
        typObj = type(eval(val))
        cortexVars.append({ 'name': val, 'module': typObj.__module__, 'type': typObj.__name__ });

    return json.dumps(cortexVars)

def _inspectDataset(varName):
    cortexObj = eval(varName)
    df = cortexObj.as_pandas()
    return df.head(100).to_json(orient='table')

_getCortexVars()
`;

export default class CortexPanel extends Widget {

    // This array contains filters for the python data types that we HAVE inspectors for.
    supportedTypes: InspectorDefinition[] = [
        {
            entityType: 'var',
            module: 'cortex.dataset',
            type: 'Dataset',
            factory: React.createFactory(DatasetInspector),
            queryFunction: () => {
                return this.executeIPythonCode(this.currentNotebook,
                    `_inspectDataset('${this.selectedItem.name}')`)
                    .then((result) => {
                        this.selectedData = {
                            columns: _.map(result.schema.fields, (c) => {
                                return { headerName: c.name, field: c.name };
                            }),

                            rows: result.data
                        };
                    });
            }
        },
        {
            entityType: 'skill',
            factory: React.createFactory(SkillInspector),
            queryFunction: () => {
                const client = new CatalogClient(this.cortexToken, this.cortexBaseUrl);
                return client.describeSkill(this.selectedItem.name)
                    .then((skill: Skill) => {
                        this.selectedData = skill;
                    });
            }
        },
        {
            entityType: 'dataset',
            factory: React.createFactory(DatasetInspector),
            queryFunction: () => {
                const client = new DatasetsClient(this.cortexToken, this.cortexBaseUrl);
                return client.getDataframe(this.selectedItem.name)
                    .then((dataframe: Dataframe) => {
                        this.selectedData = {
                            columns: _.map(dataframe.columns, (c) => {
                                return { headerName: c, field: c };
                            }),

                            rows: _.map(dataframe.values, (row) => {
                                return _.fromPairs(_.map(dataframe.columns, (col, i) => {
                                    return [col, row[i]];
                                }));
                            }),
                        };
                    });
            }
        },
        //    { module: 'cortex.dataset', type: '_Viz' },
        //    { module: 'cortex.pipeline', type: 'DatasetPipeline' },
        //    { module: 'pandas.core.frame', type: 'DataFrame' },
    ];

    app: JupyterLab;
    cortexToken: string;
    cortexBaseUrl: string;
    project: any;

    nbTracker: INotebookTracker;
    terminals: TerminalSession.IModel[] = [];
    sessions: Session.IModel[] = [];

    inspectorWidget: Widget;

    notebookVars: PythonVarInfo[];

    skills: Array<SkillListItem>;
    datasets: Array<Dataset>;

    selectedItem: any;
    selectedData: any;
    selectedInspector: any;

    currentNotebook: NotebookPanel;

    private static isLocalCondaProject(project: any): boolean {
        return _.endsWith(_.get(project, 'projectType'), '-conda');
    }

    private static isPlatformWindows(): boolean {
        const platform: string = navigator.platform.toString().toLowerCase();
        return platform.indexOf('win') === 0;
    }

    private isReady(nbPanel: NotebookPanel): Promise<void> {
        return nbPanel.session.ready.then(() => {
            if (nbPanel.session.kernel) {
                return nbPanel.session.kernel.ready;
            } else {
                return Promise.reject(nbPanel);
            }
        });
    }

    private executeIPythonCode(nbPanel: NotebookPanel, code: string): Promise<any> {
        return new Promise((resolve, reject) => {
            this.isReady(nbPanel).then(() => {

                let result: any = '{}';
                let future: Kernel.IFuture = nbPanel.session.kernel.requestExecute({ code });

                future.onIOPub = ((msg: KernelMessage.IIOPubMessage) => {
                    switch (msg.header.msg_type) {
                        case 'execute_result':
                            {
                                let payload = msg.content as nbformat.IExecuteResult;
                                let content: string = payload.data["text/plain"] as string;
                                result = content.replace(/^'|'$/g, "");
                            }
                            break;

                        case 'error':
                            {
                                let payload = msg.content as nbformat.IExecuteResult;
                                reject(`${payload.ename} - ${payload.evalue}`);
                            }
                            break;
                    }
                });

                future.done.then(() => {
                    try {
                        resolve(JSON.parse(result));
                    } catch (e) {
                        reject(e);
                    }
                });
            })
                /// This can happen if the kernel for the notebook has been shut down manually
                .catch(() => { });
        });
    }

    updateInspectorData(): Promise<void> {
        if (this.selectedInspector) {
            return this.selectedInspector.queryFunction.bind(this)();
        } else {
            this.selectedData = null;
            this.selectedItem = null;
            return Promise.resolve();
        }
    }

    updateNotebookVars(): Promise<void> {
        return new Promise((resolve) => {
            if (this.currentNotebook) {
                this.executeIPythonCode(this.currentNotebook, '_getCortexVars()').then((result) => {
                    this.notebookVars = _.filter(result, (t) => _.some(this.supportedTypes, (s) =>
                        _.isMatch(t, { module: s.module, type: s.type })));
                    resolve();
                }).catch(() => {
                    resolve();
                });
            } else {
                this.selectedData = null;
                this.selectedItem = null;
                this.notebookVars = [];
                resolve();
            }
        });
    }

    updateWidget() {
        this.updateNotebookVars()
            .then(() => this.updateInspectorData())
            .then(() => this.update());
    }

    onSelectedNotebookChanged(nbPanel: NotebookPanel) {
        this.currentNotebook = nbPanel;
        this.updateWidget();
    }

    getSkills(): Promise<void> {
        const client = new CatalogClient(this.cortexToken, this.cortexBaseUrl);
        return client.listSkills().then((skills: Array<SkillListItem>) => { this.skills = skills; });
    }

    getDatasets(): Promise<void> {
        const client = new DatasetsClient(this.cortexToken, this.cortexBaseUrl);
        return client.listDatasets().then((datasets: Array<Dataset>) => { this.datasets = datasets; });
    }

    updateInspectorVisibility() {
        if (this.app.shell.leftCollapsed || !this.isVisible) {
            if (this.inspectorWidget) {
                this.inspectorWidget.dispose();
                this.inspectorWidget = null;
            }
        } else {
            if (!this.inspectorWidget && this.isVisible) {
                this.app.shell.expandLeft();

                this.inspectorWidget = new Widget();
                this.inspectorWidget.id = 'cortex-inspector';
                this.inspectorWidget.title.label = `Cortex Inspector`;
                this.inspectorWidget.title.closable = false;
                this.inspectorWidget.title.iconClass = 'cortex-inspector__icon';

                this.app.shell.addToMainArea(this.inspectorWidget, { mode: 'split-bottom' });
            }
        }
    }

    updateSessionAuth(s: Session.ISession) {
        if (s.kernel) {
            s.kernel.ready.then(() => {
                s.kernel.requestExecute({
                    code: `import os\n` +
                        `os.environ['CORTEX_TOKEN'] = "${this.cortexToken}"\n` +
                        `os.environ['CORTEX_URI'] = "${this.cortexBaseUrl}"\n`
                }).done.then((response) => {
                    if (response.content.status !== 'ok') {
                        debug('Token refresh failed');
                    }
                });
            });
        }
    }

    static sendTerminalCommand(s: TerminalSession.ISession, cmd: string) {
        s.send({
            type: 'stdin',
            content: [cmd]
        });
    }

    onSelect(entityType: string, obj: any) {
        this.selectedItem = obj;

        if (entityType === 'var') {
            this.selectedInspector = _.find(this.supportedTypes,
                {
                    entityType: entityType,
                    module: obj.module,
                    type: obj.type
                });
        } else {
            this.selectedInspector = _.find(this.supportedTypes, { entityType });
        }

        if (this.selectedInspector) {
            debug(`selected inspector = ${this.selectedInspector}`);
            this.updateWidget();
        }
    }

    onUpdateRequest() {
        this.updateInspectorVisibility();

        ReactDOM.render(< ExplorerComponent
            datasets={this.datasets}
            skills={this.skills}
            notebookVars={this.notebookVars}
            onSelect={(entityType, obj) => this.onSelect(entityType, obj)} />, this.node);

        if (this.inspectorWidget) {
            if (this.selectedItem && this.selectedData && this.selectedInspector) {
                const inspectorComponent = this.selectedInspector.factory({
                    selectedItem: this.selectedItem,
                    selectedData: this.selectedData
                });

                ReactDOM.render(inspectorComponent, this.inspectorWidget.node);
            }
        }
    }

    constructor(app: JupyterLab, nbTracker: INotebookTracker) {
        super();

        this.app = app;
        this.nbTracker = nbTracker;

        this.id = 'cortex-panel';
        this.title.closable = false;
        this.title.iconClass = 'cortex-panel__tablogo';

        this.addClass('cortex-panel');

        registerRPC('studioAuth', (obj: any) => {
            if ((obj.token !== this.cortexToken) || (obj.uri !== this.cortexBaseUrl)) {

                this.cortexToken = obj.token;
                this.cortexBaseUrl = obj.uri;
                this.project = obj.project;

                /// Iterate through existing sessions to update auth info
                app.serviceManager.sessions.refreshRunning().then(() => {
                    this.sessions = toArray(app.serviceManager.sessions.running()) as Session.IModel[];
                    _.forEach(this.sessions, (sModel: Session.IModel) => {
                        const s: Session.ISession = app.serviceManager.sessions.connectTo(sModel);
                        if (s.kernel) {
                            s.kernel.requestExecute({ code: _ipythonInit }).done.then(() => {
                                this.updateSessionAuth(s);
                            });
                        }
                    });
                });

                Promise.all([
                    this.getDatasets(),
                    this.getSkills(),
                ]).then(() => this.updateWidget());
            }
        });

        nbTracker.currentChanged.connect((sender: any, nbPanel: NotebookPanel) => this.onSelectedNotebookChanged(nbPanel));
        nbTracker.widgetAdded.connect((sender: any, nbPanel: NotebookPanel) => {
            nbPanel.session.ready.then(() => {
                if (nbPanel.session.kernel) {
                    nbPanel.session.kernel.ready.then(() => {
                        nbPanel.content.stateChanged.connect(() => {
                            this.updateWidget();
                        });
                    }
                    );
                }
            });
        });

        app.serviceManager.ready.then(() => {
            /// Initialize list of existing terminals
            this.terminals = toArray(app.serviceManager.terminals.running()) as TerminalSession.IModel[];
            app.serviceManager.terminals.runningChanged.connect((mgr: TerminalManager, models: TerminalSession.IModel[]) => {
                const isConda = CortexPanel.isLocalCondaProject(this.project);
                const isWindows: boolean = CortexPanel.isPlatformWindows();

                _.forEach(_.differenceBy(models, this.terminals, 'name'), (t) => {
                    mgr.connectTo(t.name).then((s: TerminalSession.ISession) => {
                        if (isConda) {
                            if (isWindows) {
                                CortexPanel.sendTerminalCommand(s, `cls\rconda activate ${this.project.name}\rcls\r`);
                            } else {
                                CortexPanel.sendTerminalCommand(s, `clear\rsource deactivate\rsource activate ${this.project.name}\rclear\r`);
                            }
                        }
                    });
                });
                this.terminals = models;
            });

            /// Initialize list of existing sessions
            app.serviceManager.sessions.refreshRunning().then(() => {
                this.sessions = toArray(app.serviceManager.sessions.running()) as Session.IModel[];
                app.serviceManager.sessions.runningChanged.connect((mgr: SessionManager, models: Session.IModel[]) => {
                    /// When we detect a new session, initialize it by running the python init code for inspectors
                    _.forEach(_.differenceBy(models, this.sessions, 'name'), (t) => {
                        const s: Session.ISession = mgr.connectTo(t);
                        if (s.kernel) {
                            s.kernel.requestExecute({ code: _ipythonInit }).done.then(() => {
                                this.updateSessionAuth(s);
                            });
                        }
                    });
                    this.sessions = models;
                });
            });

        });

        app.shell.layoutModified.connect(this.updateInspectorVisibility.bind(this));

        // This will request auth token from STUDIO, and upon receipt will update the panel widget.
        callRPC('requestAuth');
    }

}
