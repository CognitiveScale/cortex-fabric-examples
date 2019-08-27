import os
import pytest
import requests

collect_ignore = ['setup.ipynb']

def pytest_addoption(parser):
    parser.addoption('--base_url', action='store', help='url for cortex 5', default='https://api.cortex-dev.insights.ai')
    parser.addoption('--tenant_id', action='store', help='tenant id used for test suite', default='cortex-examples-tenant')
    parser.addoption('--admin_token', action='store', help='admin token used for test suite.', default=None)
    parser.addoption('--admin_username', action='store', help='admin used for test suite', default='cortex-examples-username')
    parser.addoption('--admin_password', action='store', help='admin password used for test suite', default='cortex-examples-password')
    parser.addoption('--invitation_code', action='store', help='invitation code for registering new tenants', default=None)

@pytest.fixture(scope='session')
def context(request):
    orig_token = os.environ.get('CORTEX_TOKEN')
    orig_uri = os.environ.get('CORTEX_URI')

    context = Context(request)
    os.environ['CORTEX_TOKEN'] = context.token
    os.environ['CORTEX_URI'] = context.uri

    yield context

    if orig_token: os.environ['CORTEX_TOKEN'] = orig_token
    if orig_uri: os.environ['CORTEX_URI'] = orig_uri


class Context():
    def __init__(self, request):
        self.base_url = request.config.getoption('--base_url')
        self.tenant_id = request.config.getoption('--tenant_id')
        self.admin_token = request.config.getoption('--admin_token')
        self.admin_username = request.config.getoption('--admin_username')
        self.admin_password = request.config.getoption('--admin_password')
        self.invitation_code = request.config.getoption('--invitation_code')

        if not self.admin_token:
            self.register_tenant()
            self.admin_token = self.authenticate_user()

    def get_token(self):
        return self.admin_token

    def get_uri(self):
        return self.base_url

    def register_tenant(self):
        path = f'v2/admin/tenants/register?invitationCode={self.invitation_code}'
        endpoint = requests.compat.urljoin(self.base_url, path)

        header = { 'Content-Type': 'application/json' }
        payload = {
            'name': 'Cortex Examples Testing',
            'email': f'{self.admin_username}@cognitivescale.com',
            'first': 'Cortex',
            'last': 'Examples',
            'tenantId': self.tenant_id,
            'admin': self.admin_username,
            'password': self.admin_password,
            'eula_accepted': True
        }
        
        # Attempt to register the tenant, and raise an exception if the response is anything other
        # than success or a 400 with 'already registered' in the response body message field.
        response = requests.post(endpoint, headers=header, json=payload)
        if not response:
            if response.status_code != 400:
                response.raise_for_status()
            else:
                body = response.json()
                if not 'already exists' in body['message']:
                    response.raise_for_status()

    def authenticate_user(self):
        path = f'v2/admin/{self.tenant_id}/users/authenticate'
        endpoint = requests.compat.urljoin(self.base_url, path)

        header = { 'Content-Type': 'application/json' }
        payload = {
            'username': self.admin_username,
            'password': self.admin_password
        }

        response = requests.post(endpoint, headers=header, json=payload)
        response.raise_for_status()

        body = response.json()
        token = body['jwt']

        return token

    token = property(get_token)
    uri = property(get_uri)