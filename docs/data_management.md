# Data Science Lifecycle

* Data Preparation
* Feature Engineering
* Model selection and optimisation

## Data Preparation

### Use Case #1: Start with a local raw source data file

1. Load local source data into Pandas DataFrame
2. Instantiate Cortex client and builder
3. Use the builder to create a dataset based on the DataFrame created before

```python
import pandas as pd
from cortex import Cortex

# Load load local source data file into Pandas
df = pd.DataFrame.from_csv('/data/kaggle/ames_housing/train.csv')

# Connect to Cortex
cortex = Cortex.client()

# Get a builder from the Cortex client
builder = cortex.builder()

# Use the builder to create a Dataset
ds = builder.dataset('kaggle/ames-housing-train').title('Ames Housing Traing Data').from_df(df, '/kaggle/ames_housing_train.json', style='json').build()

# The Dataset is configured and saved in the Cortex repository, the local source data is configured and uploaded to the Cortex Managed Content service
```



## Dataset Contracts

```python
from cortex import Cortex
from cortex.data_wrangling import cleaners

# Connect to Cortex
cortex = Cortex.client()

# Retrieve a Cortex DataSet to work with
ds = cortex.dataset('kaggle/ames-housing-train')

# Create a DataSet contract called 'ames_housing_01' representing the current discovery activity
c = ds.contract('ames_housing_01')

# Get a DataFrame for the source
df = c.get_source()

# Dump the data dictionary
c.discover.data_dictionary()
# Attribute	Type	% Nulls	Count	Unique	Value	Observations	Knowledge

# Begin applying cleaners
c.set_cleaner(c.clean.clean_header(df, in_place=True))
```

## Sampling

```python
# Sampling: pick the first N records
df = ds.sample(top_n=1000).as_pandas()

# Sampling: pick N records at random
df = ds.sample(random_n=1000).as_pandas()

# Sampling: get N% of the records
df = ds.sample(random_pct=0.1).as_pandas()
```

