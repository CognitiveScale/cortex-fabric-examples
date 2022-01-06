import os
import logging
import json

logger = logging.getLogger(__name__)
root_path = os.path.realpath(os.path.dirname(__file__))
template_path = os.path.join(root_path, "intervention_template.json")
config_path = os.path.join(root_path, "config.json")


class Config:

    def __init__(self):
        with open(template_path, 'r') as f:
            self.data = json.load(f)
        with open(config_path, 'r') as cf:
            self.config = json.load(cf)

    def get_template(self, campaign_type, key):
        return self.data[campaign_type][key]

    def get_attr(self, key):
        return self.config[key]
