FROM python:3.11.0a5-slim-bullseye
ADD . /app
RUN pip install -r /app/requirements.txt
ENTRYPOINT ["python", "/app/main.py"]
