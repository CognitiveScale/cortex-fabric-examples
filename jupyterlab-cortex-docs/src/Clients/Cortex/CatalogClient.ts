/*
 * CognitiveScale Cortex Studio
 *
 * Copyright (c) Cognitive Scale, Inc.
 * All rights reserved.
 * Dissemination or any rights to code or any derivative works thereof is strictly forbidden
 * unless licensed and subject to a separate written agreement with CognitiveScale.
 */

import { CortexClient } from './CortexClient';

export interface SkillListItem {
    name: string;
    title: string;
}

interface ListSkillsResponse {
    processors: Array<SkillListItem>;
}

export interface Skill {
    description?: string;
    name: string;
    title: string;
}

export class CatalogClient extends CortexClient {
    private static Endpoints = {
        skill: 'v3/catalog/skills/:NAME',
        skills: 'v3/catalog/skills',
    };

    constructor(jwt: string, baseUrl: string) {
        super(jwt, baseUrl);
    }

    async describeSkill(name: string) : Promise<Skill | null> {
        const endpoint = CatalogClient.Endpoints.skill.replace(':NAME', name);
        return this.api<Skill>(endpoint);
    }

    async listSkills() : Promise<Array<SkillListItem>> {
        const response = await this.api<ListSkillsResponse>(CatalogClient.Endpoints.skills);
        return response.processors;
    }
}