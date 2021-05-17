FROM python:3.7.4-slim
ADD . /app
RUN pip install -r /app/requirements.txt
ENTRYPOINT ["python", "/app/train.py"]