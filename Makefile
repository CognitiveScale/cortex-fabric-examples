install:
	pip install -r requirements-dev.txt

test:
	pytest --cache-clear --nbval-lax \
		cortex_docs_examples/notebooks/python3/examples

tox.test:
	tox -r

zipit:
	zip -r  cortex-skill-lab-examples.zip . -x *.git*
