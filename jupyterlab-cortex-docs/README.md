# jupyterlab-cortex

A JupyterLab extension for the Cortex Studio UI.

[![Build Status](https://travis-ci.org/CognitiveScale/jupyterlab-cortex.svg?branch=develop)](https://travis-ci.org/CognitiveScale/jupyterlab-cortex)

## Prerequisites

* JupyterLab

## Installation

```bash
jupyter labextension install @c12e/jupyterlab-cortex
pip install jupyterlab-cortex
jupter serverextension enable --py jupyterlab_cortex
```

## Development

### Setup

For a development install (requires npm version 4 or later), first install the required node dependencies.
```
> npm install
```
  
Create and activate a virtual environment (can use conda as shown below) in order to create an isolated work environment
```
> conda create -n <CCFTicketName or a descriptive virtual environment name>
> source activate <CCFTicketName or a descriptive virtual environment name>
```
Now install jupyterlab and the jupyter lab server extensions
```
> conda install -c conda-forge jupyterlab
# below is done through setup.py
> pip install .
```

Enable our `jupyterlab_cortex` server extension

```
> pip install .
> jupyter serverextension enable --py jupyterlab_cortex
```
Install `jupyterlab-cortex` as our extension.
```
> jupyter labextension install .
```

### Running

After following the above setup steps, run the below command to open a notebook with the extension in your browser
```
> jupyter lab
```

Note: the automatically opened url will be `localhost:8888/lab` which was redirected from another url with a token - which has the format of: `http://localhost:8888/?token=foobar2491a95e97066fe211e1a9acba2a67301ba405be4cb6761`. This url with the token will be printed in your terminal output.

#### Hot Reloading (or lack thereof)
running `jupyter labextension link .` SHOULD allow hot reloading, but in practice this has not worked. Therefore:

For new build changes to the client (i.e. `src` folder), run `jupyter labextension install .` before `jupyter lab` again.

For new build changes to the server (i.e. `jupyterlab_cortex` folder), run `pip install .` before running `jupyter lab` again.

