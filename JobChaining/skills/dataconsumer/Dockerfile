FROM python:3.8.3-buster
WORKDIR /app
COPY requirements.txt /app
COPY *.py /app
RUN pip install -r requirements.txt
ENTRYPOINT ["python","/app/job.py"]
