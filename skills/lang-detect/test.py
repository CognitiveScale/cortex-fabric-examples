from cortex import Cortex, Message
from lang_detect.detect import detect
import sys
import argparse
from pprint import pprint


def test_detect(args):
    text = u'¡Bienvenido a CognitiveScale, que tengas un buen día!'
    print('Detecting language in: {}'.format(text))
    m = Message.with_payload({'text': text})
    result = detect(m.to_params())
    pprint(result['payload'])


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print('Usage: test.py [action] <args...>')
        sys.exit(-1)

    action_name = sys.argv[1]
    args = sys.argv[2:]
    if action_name == 'detect':
        test_detect(args)
    else:
        print('Unknown action name: {}'.format(action_name))
        sys.exit(-1)