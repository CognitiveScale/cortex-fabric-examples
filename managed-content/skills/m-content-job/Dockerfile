FROM c12e/cortex-python-lib:fabric6
ADD . /app
WORKDIR /app
RUN pip install -r requirements.txt

ENV ENVOY_ADMIN_API=http://localhost:15000
ENV ISTIO_QUIT_API=http://localhost:15020
ENV SCUTTLE_LOGGING=false

ENTRYPOINT ["scuttle", "python", "main.py"]
