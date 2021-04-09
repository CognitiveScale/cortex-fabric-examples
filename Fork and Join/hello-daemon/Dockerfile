FROM python:3.7.4-buster

WORKDIR /app

COPY hello-daemon/src hello-daemon/src
COPY hello-daemon/requirements.txt hello-daemon/requirements.txt
RUN pip install -r hello-daemon/requirements.txt
ENV PYTHONPATH "${PYTHONPATH}:/app"
CMD ["uvicorn", "hello-daemon.src.__main__:app", "--host", "0.0.0.0", "--port", "5000"]
