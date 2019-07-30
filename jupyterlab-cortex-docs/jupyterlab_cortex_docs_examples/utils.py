import os

def file_paths(walk_iter):
    for (dirname, _, filenames) in walk_iter:
        for filename in filenames:
            yield os.path.join(dirname, filename)

def walk_dir(path):
    for triple in os.walk(path):
        yield triple
