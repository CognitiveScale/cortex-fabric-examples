import sys
from cortex import Cortex


def build_detect_action(builder):
    action = builder.action('c12e/lang-detect').from_source_file(
        'lang_detect/detect.py', 'detect').with_requirements(['langdetect']).daemon().build()

    return action


def build_detect_skill(builder, action):
    b = builder.skill('c12e/lang-detect-skill').title('Language Detection').description(
        'Detects the language used in a provided text.')

    b.input('text_input').parameter('text', 'string', None, 'Text').all_routing(
        action, 'text_with_language').build()

    b.output('text_with_language').parameter('text', 'string', None, 'Text').parameter(
        'lang', 'string', None, 'Language').parameter('score', 'number', 'float', 'Score').parameter(
        'other_langs', 'array', None, 'Other Languages').build()

    return b.build()


def build_detect():
    cortex = Cortex.client()
    builder = cortex.builder()

    action = build_detect_action(builder)
    print('Deployed action {} at version v{}'.format(action.name, action.version))
    print(action.get_deployment_status())

    skill = build_detect_skill(builder, action)
    print('Deployed skill {} at version v{}'.format(skill.name, skill.version))


if __name__ == '__main__':
    if len(sys.argv) == 2:
        step = sys.argv[1]
        if step == 'detect':
            build_detect()
        else:
            print('Usage: build.py [detect]')
    else:
        build_detect()
