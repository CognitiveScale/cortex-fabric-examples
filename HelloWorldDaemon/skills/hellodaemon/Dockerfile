FROM python:3-slim-buster
WORKDIR /app
COPY requirements.txt /app
COPY *.py /app
RUN pip install -r requirements.txt
EXPOSE 5000
CMD ["python","/app/daemon.py"]
