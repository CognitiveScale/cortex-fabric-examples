FROM python:3.9.10-alpine
WORKDIR /app
COPY requirements.txt /app
COPY *.py /app
RUN pip install -r requirements.txt
ENTRYPOINT ["python","/app/job.py"]
