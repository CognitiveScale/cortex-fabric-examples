FROM python:3.9-slim
ADD . /app
RUN apt-get update \
    && apt-get upgrade -y \
    && pip install --upgrade -r /app/requirements.txt \
    && apt-get clean
ENTRYPOINT ["python", "/app/main.py"]
