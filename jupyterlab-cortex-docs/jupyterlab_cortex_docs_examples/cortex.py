from notebook.base.handlers import APIHandler
from tornado import web

from .resources import JupyterLabCortexResources

class LanguagesHandler(APIHandler):
    @web.authenticated
    def get(self):
        resources = JupyterLabCortexResources()
        languages = resources.get_languages()
        self.finish({ 'languages': languages })

class ExamplesHandler(APIHandler):
    @web.authenticated
    def get(self, language):
        resources = JupyterLabCortexResources()
        examples = resources.get_examples(language)
        self.finish({ 'examples': examples })

class ExampleDetailsHandler(APIHandler):
    @web.authenticated
    def get(self, language, example):
        resources = JupyterLabCortexResources()
        details = resources.get_example_details(language, example)
        self.finish({ 'details': details })

class TemplatesHandler(APIHandler):
    @web.authenticated
    def get(self, language):
        resources = JupyterLabCortexResources()
        templates = resources.get_templates(language)
        self.finish({ 'templates': templates })

class TemplateDetailsHandler(APIHandler):
    @web.authenticated
    def get(self, language, template):
        resources = JupyterLabCortexResources()
        details = resources.get_template_details(language, template)
        self.finish({ 'details': details })
