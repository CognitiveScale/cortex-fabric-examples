"""
Copyright 2018 Cognitive Scale, Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""

import io
import os
from setuptools import setup
from setuptools import find_packages

def get_version(file, name='__version__'):
    """Get the version of the package from the given file by
       executing it and extracting the given `name`.
    """
    path = os.path.realpath(file)
    version_ns = {}
    with io.open(path, encoding="utf8") as f:
        exec(f.read(), {}, version_ns)
    return version_ns[name]

setup(
    name='jupyterlab_cortex',
    description='A JupyterLab extension for Cortex 5 by CognitiveScale',
    long_description='A JupyterLab extension for Cortex 5 by CognitiveScale',
    version=get_version('jupyterlab_cortex/_version.py'),
    author='CognitiveScale',
    author_email='info@cognitivescale.com',
    url='https://docs.cortex.insights.ai',
    license='CognitiveScale Inc.',
    platforms=['linux', 'osx'],
    packages=find_packages(),
    include_package_data=True,
    install_requires=[
        'notebook',
        'pydash'
    ],
    tests_require=[
        'mocket>=2.0.0,<3',
        'mock>=2,<3',
        'pytest>=3.1,<4'
    ],
    classifiers=[
        'Operating System :: MacOS :: MacOS X',
        'Operating System :: POSIX',
        'Programming Language :: Python :: 3.6',
    ]
)
