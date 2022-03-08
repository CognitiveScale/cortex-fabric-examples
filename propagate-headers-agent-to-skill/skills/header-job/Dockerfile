FROM python:3.9.10-buster
ADD . /app
RUN pip install -r /app/requirements.txt
ENTRYPOINT ["python", "/app/main.py"]
