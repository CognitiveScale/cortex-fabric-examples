install:
	pip install -r requirements-dev.txt

dev.test:
	pytest --cache-clear  \
		--ignore cortex_docs_examples/notebooks/python3/examples/deployment \
		--nbval-lax cortex_docs_examples/notebooks/python3/examples
	
test:
	tox -r

zipit:
	zip -r  cortex-skill-lab-examples.zip . -x *.git*
