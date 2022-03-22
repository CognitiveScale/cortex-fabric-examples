FROM python:3.9-slim
WORKDIR /app
COPY requirements.txt /app
COPY *.py /app

RUN apt-get update \
    && apt-get upgrade -y \
    && pip install --upgrade -r requirements.txt \
    && apt-get clean
    
ENTRYPOINT ["python","/app/job.py"]
