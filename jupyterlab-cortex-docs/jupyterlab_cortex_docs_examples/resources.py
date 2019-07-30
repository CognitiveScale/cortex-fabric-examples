import json
import os
import pkg_resources
from pydash.objects import get, merge

from .utils import file_paths, walk_dir

class JupyterLabCortexResources():
    root_dir = 'jupyterlab_cortex'

    def __init__(self):
        self.distribution = pkg_resources.get_distribution(self.root_dir)

    def _get_metadata(self, path):
        return json.loads(self.distribution.get_resource_string(__name__, f'{path}/metadata.json'))

    def _get_metadata_list(self, path):
        contents = self.distribution.resource_listdir(path)
        subdirs = [x for x in contents if self.distribution.resource_isdir(f'{path}/{x}')]
        metadata = list(map(lambda dir: dict([('id', dir), ('metadata', self._get_metadata(f'{path}/{dir}'))]), subdirs))
        return metadata

    def _get_details(self, path):
        metadata = self._get_metadata(path)
        files = [x for x in self._list_files(path) if x != 'metadata.json']
        results = merge(get(metadata, 'contentDefinition'), { 'files': files })
        return results

    def _list_files(self, path):
        root = self.distribution.get_resource_filename(__name__, path)
        iter = file_paths(walk_dir(root))
        return [os.path.relpath(x, root) for x in iter]

    def get_languages(self):
        context = f'{self.root_dir}/notebooks'
        languages = self._get_metadata_list(context)
        results = [{ 'id': get(x, 'id'), 'title': get(x, 'metadata.languageDefinition.title')} for x in languages]
        return results

    def get_examples(self, language):
        context = f'{self.root_dir}/notebooks/{language}/examples'
        examples = self._get_metadata_list(context)
        results = [{ 'id': get(x, 'id'), 'title': get(x, 'metadata.contentDefinition.title')} for x in examples]
        return results

    def get_example_details(self, language, example):
        context = f'{self.root_dir}/notebooks/{language}/examples/{example}'
        results = self._get_details(context)
        return results

    def get_templates(self, language):
        context = f'{self.root_dir}/notebooks/{language}/templates'
        templates = self._get_metadata_list(context)
        results = [{ 'id': get(x, 'id'), 'title': get(x, 'metadata.contentDefinition.title')} for x in templates]      
        return results

    def get_template_details(self, language, template):
        context = f'{self.root_dir}/notebooks/{language}/templates/{template}'
        results = self._get_details(context)
        return results

    def get_root_dir(self):
        context = f'{self.root_dir}/notebooks'
        result = self.distribution.get_resource_filename(__name__, context)
        return result