/*
 * CognitiveScale Cortex Studio
 *
 * Copyright (c) Cognitive Scale, Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */

import { Dialog, ReactElementWidget, Styling } from "@jupyterlab/apputils";
import { MessageLoop } from "@phosphor/messaging";
import { Widget } from "@phosphor/widgets";

export class ReactElementRenderer extends Dialog.Renderer {
    createBody(value: Dialog.BodyType<any>): Widget {
        const body = super.createBody(value);
        if (body instanceof ReactElementWidget) {
            MessageLoop.sendMessage(body, Widget.Msg.UpdateRequest);
            Styling.styleNode(body.node);
        }
        return body;
    }
}
