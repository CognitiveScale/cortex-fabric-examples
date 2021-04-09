import requests, json

## Sessions API  - To create, update a session and retirve exising sesssion daataa via API
class SessionsAPI:

    def __init__(self, apiEndpoint, project, token):
        """
        init
        :param apiEndpoint:
        :param project:
        :param token:
        """
        self.apiEndpoint = apiEndpoint
        self.project = project
        self.token = token
        self.sessions_base_url = "{0}/fabric/v4/projects/{1}/sessions".format(self.apiEndpoint, self.project)
        self.headers = {'content-type': "application/json", 'authorization': "Bearer {0}".format(self.token)}

    def post_by_key(self, key,  value, session_id=None):
        """
        posts session
        :param session_id:
        :param data:
        :return:
        """
        if not key or type(key) != str:
            raise ValueError("Key should be str and not empty")
        url = self.sessions_base_url + "/"+ session_id if session_id else self.sessions_base_url
        if not value: raise ValueError("Session data should not be empty")
        payload = {"state": {key: json.dumps(value)}}
        response = requests.post(url, data=json.dumps(payload), headers=self.headers)
        response.raise_for_status()
        return response.json()

    def get_by_key(self, key, session_id):
        """
        gets session data
        :param url:
        :param project:
        :param token:
        :param session_id:
        :return:
        """

        url = self.sessions_base_url + "/" + session_id
        if not session_id: raise ValueError("sessionId should not be empty")
        response = requests.get(url, headers=self.headers)
        response.raise_for_status()
        if key in response.json()['state']:
            return json.loads(response.json()['state'][key])
        else:
            return None

    def post(self, data=None, session_id=None):
        """
        posts session
        :param session_id:
        :param data:
        :return:
        """
        url = self.sessions_base_url + "/"+ session_id if session_id else self.sessions_base_url
        if not data: raise ValueError("Session data should not be empty")
        payload = {"state": data}
        response = requests.post(url, data=json.dumps(payload), headers=self.headers)
        response.raise_for_status()
        return response.json()

    def get(self, session_id):
        """
        gets session data
        :param url:
        :param project:
        :param token:
        :param session_id:
        :return:
        """

        url = self.sessions_base_url + "/" + session_id
        if not session_id: raise ValueError("sessionId should not be empty")
        response = requests.get(url, headers=self.headers)
        response.raise_for_status()
        json_rs = response.json()
        return json_rs['state']
