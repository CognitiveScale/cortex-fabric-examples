import os
import pytest

def test_setup(context):
    print(f'Testing against API {os.environ["CORTEX_URI"]}')