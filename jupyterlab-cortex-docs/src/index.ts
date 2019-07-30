/*
 * CognitiveScale Cortex Studio
 *
 * Copyright (c) Cognitive Scale, Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */

import Debug from 'debug';
import { JupyterLab, JupyterLabPlugin } from '@jupyterlab/application';
import { IMainMenu } from '@jupyterlab/mainmenu';
import { INotebookTracker } from "@jupyterlab/notebook";
import { Menu } from '@phosphor/widgets';

import { addCommands, CommandIDs } from './menuCommands';
import CortexPanel from "./Widgets/CortexPanel/CortexPanel";

const debug = Debug('CortexPanel');

declare var global: any;

const cortexPluginId = '@c12e/cortex:plugin';

/**
 * Initialization data for the extension.
 */
const extension: JupyterLabPlugin<CortexPanel> = {
    id: cortexPluginId,
    requires: [IMainMenu, INotebookTracker],
    autoStart: true,
    activate: (app: JupyterLab, mainMenu: IMainMenu, nbTracker: INotebookTracker) => {
        debug('JupyterLab extension cortex-studio-skill-lab is activated!');

        /// If we are in standalone mode, stub out the studio interface
        if (typeof global.callRPC === "undefined") {
            global.callRPC = () => { };
        }
        if (typeof global.registerRPC === "undefined") {
            global.registerRPC = () => { };
        }

        // Register commands with app
        addCommands(app);

        // Add a Cortex submenu to File -> New
        const { commands } = app;
        const menu = new Menu({ commands });
        menu.title.label = 'Cortex';
        [CommandIDs.newFromExample, CommandIDs.newFromTemplate].forEach(
            command => menu.addItem({ command })
        );
        mainMenu.fileMenu.newMenu.addGroup([{ type: 'submenu', submenu: menu }]);

        const widget: CortexPanel = new CortexPanel(app, nbTracker);
        app.shell.addToLeftArea(widget, { rank: 102 });

        return widget;
    }
};

export default extension;
