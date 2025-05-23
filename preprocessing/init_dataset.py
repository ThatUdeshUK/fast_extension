# %% [markdown]
# # Initialize Datasets
# ## Places

# %%
import pandas as pd
import numpy as np
import seaborn as sns

import json
from tqdm import tqdm

# %% [markdown]
# ### Dataset Parameters

# %%
# ds_path = "../../data/places_dump_US.geojson"
ds_path = "/scratch1/ukumaras/datasets/places_o200000_q5000000_scaled.json"
output_dir = "/scratch1/ukumaras/datasets/exported/"

num_objects = 1000000
num_queries = 1000000

should_scale = True
grid_range = 512
query_span = 0.10
spatial_uni = False
fixed_max_keywords = True
min_keywords = 1
max_keywords = 7

seed = 7

# %% [markdown]
# ### Load Data

# %%
content = []

with open(ds_path, "r") as f:
    place_id = 0
    for i, line in tqdm(enumerate(f)):
        if (len(content) >= num_objects + num_queries):
            break

        place = json.loads(line)
        if ('id' in place and 'properties' in place and 'tags' in place['properties']):
            if (len(place['properties']['tags']) < min_keywords):
                continue
            
            content.append({
                "id": place_id,
                "x": place['geometry']['coordinates'][0],
                "y": place['geometry']['coordinates'][1],
                "keywords": place['properties']['tags'],
                "scaled": False
            })
            place_id += 1;


df = pd.DataFrame(content)
df.head()

# %% [markdown]
# ### Pre-process

# %%
def scale(c, minc, maxc):
    return (c - minc) * grid_range / (maxc - minc)


if should_scale and not spatial_uni:
    print(df.columns)
    minx = df['x'].min()
    miny = df['y'].min()
    maxx = df['x'].max()
    maxy = df['y'].max()
    
    df['x'] = df['x'].apply(lambda x: scale(x, minx, maxx))
    df['y'] = df['y'].apply(lambda y: scale(y, miny, maxy))
    df['scaled'] = True

df.head()

# %% [markdown]
# ### Uniform Sample

# %%
if spatial_uni:
    sample = pd.DataFrame(np.random.uniform(0, 512, (num_objects + num_queries, 2)), columns=['x', 'y'])
    
    df['x'] = sample['x']
    df['y'] = sample['y']

df.head()

# %%
if fixed_max_keywords:
    import random

    def clamp_keywords(keywords):
        if (len(keywords) < max_keywords):
            return keywords
        return sorted(random.sample(keywords, max_keywords))
    df['keywords'] = df['keywords'].apply(clamp_keywords)

df.head()

# %%
df['keywords'] = df['keywords'].apply(sorted)
df.head()

# %% [markdown]
# ### Visualize

# %%
sns.scatterplot(x="x", y="y", data=df.loc[:, ["x", "y"]])

# %% [markdown]
# ### Export

# %%
# fail

# %%
output_name = f'{output_dir}places_o{num_objects}_q{num_queries}'

if (should_scale and not spatial_uni):
    output_name += f'_scaled'

if (spatial_uni):
    output_name += f'_spatialuni'

if (min_keywords != 0):
    output_name += f'_minkeys' + str(min_keywords)

if fixed_max_keywords:
    output_name += f'_maxkeys' + str(max_keywords)

# %%
def scale_query(row):
    step = grid_range * query_span
    row['min_x'] = max(0, row['x'] - step)
    row['min_y'] = max(0, row['y'] - step)
    row['max_x'] = min(grid_range, row['x'] + step)
    row['max_y'] = min(grid_range, row['y'] + step)
    return row
q_df = df.apply(scale_query, axis=1, result_type='expand').drop(columns=['scaled'])
q_df['et'] = num_queries + num_objects

q_df.iloc[:num_objects, :].to_json(f'{output_name}_queries.jsonl', orient='records', lines=True)
print(f'{output_name}_queries.jsonl')

# %%
q_df

# %%
df.iloc[num_objects:num_objects+num_queries, :].to_json(f'{output_name}_objects.jsonl', orient='records', lines=True)
print(f'{output_name}_objects.jsonl')

# %%
df.shape

# %%


# %%



