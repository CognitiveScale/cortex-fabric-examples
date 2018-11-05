from cortex import Message
from langdetect import detect_langs


def detect(params):
    msg = Message(params)
    text = msg.payload.get('text')

    results = detect_langs(text)

    others = []
    for lang in results[1:]:
        others.append({'lang': lang.lang, 'score': lang.prob})

    top_lang = results[0]

    return Message.with_payload(
        {'text': text, 'lang': top_lang.lang, 'score': top_lang.prob, 'other_langs': others}).to_params()
