"""
Copyright (c) 2021. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Template/Example Code [License](https://github.com/CognitiveScale/cortex-code-templates/blob/main/LICENSE.md)
"""

import json

if __name__ == '__main__':
    import sys
    params = sys.argv[1]
    params = json.loads(params)
    print(f'Received: {params["payload"]}')
