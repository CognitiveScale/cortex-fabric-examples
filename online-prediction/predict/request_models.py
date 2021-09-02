"""
Copyright (c) 2021. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Example Code [License](https://cognitivescale.github.io/cortex-fabric-examples/LICENSE.md)
"""
from typing import Optional

from pydantic import BaseModel, conlist, Field


class InvokeRequestPayload(BaseModel):
    columns: conlist(str, min_items=1)
    instances: conlist(list, min_items=1)


class InvokeRequestProperties(BaseModel):
    experiment_name: Optional[str] = Field(alias="experiment-name")
    run_id: Optional[str] = Field(alias="run-id")


class InvokeRequest(BaseModel):
    api_endpoint: str = Field(..., alias="apiEndpoint")
    token: str = Field(...)
    project_id: str = Field(..., alias="projectId")
    payload: InvokeRequestPayload = Field(...)
    properties: InvokeRequestProperties = Field(...)


class InitializeRequestProperties(BaseModel):
    experiment_name: str = Field(..., alias="experiment-name")
    run_id: Optional[str] = Field(alias="run-id")


class InitializeRequest(BaseModel):
    api_endpoint: str = Field(..., alias="apiEndpoint")
    token: str = Field(...)
    project_id: str = Field(..., alias="projectId")
    properties: InitializeRequestProperties = Field(...)

