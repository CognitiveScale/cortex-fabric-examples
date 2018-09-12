# Cortex Skill Lab examples

## Setup
1. Create a new Skill Lab project
2. Open the terminal
  1. Unzip `cortex-skill-lab-examples.zip` into the current working directory:
    ```bash
      $ unzip <path/to/cortex-skill-lab-examples.zip>
    ```
  2. conda install -c conda-forge scikit-learn 


## Cortex Skill Lab Hello World example

### Running the Hello World example

1. From the left-side menu, open `skill-lab/skills/hello_world.ipynb`
2. Run the file.


### Running the Hello World example with a private Docker repository

1. Create a private docker repo `<repo_namespace>/<repo_name>`
2. Add `c12ereadonly` as collaborator to your repo. This will allow Cortex to pull your image.
3. From your laptop, `docker login` to be able to push your image to docker.io.
4. From the left-side menu, open `skill-lab/skills/hello_world.ipynb`
5. Add e `--prefix` to the `%%cortex_action` ipython magic call
  ```python
  %%cortex_action --name '<repo_namespace>/<repo_name>' --function hello_world --prefix registry.cortex-stage.insights.ai:5000
  ```
6. Run the file.

## Cortex Skill Lab Ames Realestate example
### Running the Ames example

1. From the left-side menu, open the `skill-lab/ames-housing/ames_*.ipynb` files.
2. Run the files in the following order:
  1. `ames_analysis.ipynb`
  2. `ames_clean.ipynb`
  3. `ames_features.ipynb`
  4. `ames_modeling.ipynb`
  5. `ames_deploy.ipynb`

### Running the Ames Housing example with a private Docker repository

1. Create a private docker repo `<repo_namespace>/<repo_name>`
2. Add `c12ereadonly` as collaborator to your repo. This will allow Cortex to pull your image.
3. From your laptop, `docker login` to be able to push your image to docker.io.
4.  In the `ames_deploy.ipynb`, "Model deployment - Step 3: Build and Deploy Cortex Action", replace the `builder.action()` call with the following:

```python
action_name = '<repo_namespace>/<repo_name>'
image_prefix = 'registry.cortex.insights.ai:5000'
builder.action(action_name)\
       .from_model(model, x_pipeline=x_pipe, y_pipeline=y_pipe, target='SalePrice')\
       .image_prefix(image_prefix).build()
```
