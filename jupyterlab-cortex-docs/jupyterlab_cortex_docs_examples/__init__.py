""" Cortex Skill Lab : Cortex 5 for JupyterLab """

import re

from tornado.web import StaticFileHandler
from notebook.utils import url_path_join

from ._version import __version__
from .cortex import ExampleDetailsHandler, ExamplesHandler, LanguagesHandler, TemplateDetailsHandler, TemplatesHandler
from .resources import JupyterLabCortexResources

def _jupyter_server_extension_paths():
    return [{ 'module': 'jupyterlab_cortex' }]

def load_jupyter_server_extension(nb_server_app):
    """
    Called when the extension is loaded.

    Args:
        nb_server_app (NotebookApp): handle to the Notebook webserver instance.
    """
    # See https://jupyter-notebook.readthedocs.io/en/stable/extending/handlers.html
    web_app = nb_server_app.web_app
    host_pattern = '.*$'
    base_url = web_app.settings['base_url']
    cortex = url_path_join(base_url, 'cortex')

    languages = url_path_join(cortex, 'languages')
    examples = url_path_join(languages, r'(?P<language>(?:[^/]+))', 'examples')
    example_details = url_path_join(examples, r'(?P<example>(?:[^/]+))')
    templates = url_path_join(languages, r'(?P<language>(?:[^/]+))', 'templates')
    template_details = url_path_join(templates, r'(?P<template>(?:[^/]+))')

    resources = JupyterLabCortexResources()
    files = url_path_join(languages, '(.*)')

    handlers = [
        (languages, LanguagesHandler),
        (examples, ExamplesHandler),
        (example_details, ExampleDetailsHandler),
        (templates, TemplatesHandler),
        (template_details, TemplateDetailsHandler),
        (files, StaticFileHandler, { 'path': resources.get_root_dir() })
    ]
    web_app.add_handlers(host_pattern, handlers)