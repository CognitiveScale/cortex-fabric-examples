import os
import json
from cortex import Cortex
from cortex.connection import ConnectionClient
from cortex.skill import SkillClient


# where this file is located
EXEC_PATH = os.path.dirname(__file__)

# path to skill definition
SKILL_DEF = os.path.join(EXEC_PATH, 'skill.json')

# path to connection definition
CONNECTION_DEF = os.path.join(EXEC_PATH, '..', 'connections', 'connection.json')


CONFIG = {
    'project': '',
    'skill_name': '',
    'connection_name': 'gcs-german-credit'
}

def save_connection(cortex_client):
    with open(CONNECTION_DEF, 'r') as f:
        connection_json = json.load(f)
    connection_client = ConnectionClient(cortex_client)
    connection_client.save_connection(project=CONFIG['project'], connection=connection_json)

def save_skill(cortex_client):
    with open(SKILL_DEF, 'r') as f:
        skill_data = json.load(f)
    skill_client = SkillClient(cortex_client)
    skill_client.save_skil(skill_data)

def invoke_skill(cortex_client):
    # invoke the skill - verify a successful response
    params = {
        'connection_name': CONFIG['connection_name'],
    }
    skill_client = SkillClient(cortex_client)
    response = skill_client.invoke(CONFIG['project'], skill_name, 'params', params, {})
    if response.get('success'):
        activation_id = response.get('activationId')

def main():
    # NOTE: assumes the Cortex CLI has been configured
    client = Cortex.client()
    save_skill(client)


def deploy_skill():
    pass

def invoke_skill():
    pass

if __name__ == '__main__':
    main()
