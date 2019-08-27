.PHONY: install dev.test test dist

install:
	pip install -r requirements-dev.txt

dev.test:
	pytest --cache-clear  \
	    --capture=no \
		--ignore cortex_docs_examples/notebooks/python3/examples/deployment \
		--nbval-lax cortex_docs_examples/notebooks/python3/examples

dev.network.test:
	pytest --cache-clear  \
		--capture=no \
		--current-env \
		--invitation_code=${INVITATION_CODE} \
		--nbval-lax \
		tests/setup.py cortex_docs_examples/notebooks/python3/examples notebooks

test:
	tox -r

dist:
	mkdir -p dist && \
	cd cortex_docs_examples && \
	zip -r ../dist/notebooks.zip . && \
	cd -