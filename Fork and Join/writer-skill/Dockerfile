FROM python:3.7.4-buster

WORKDIR /app

COPY writer-skill/src writer-skill/src
COPY writer-skill/requirements.txt writer-skill/requirements.txt
RUN pip install -r writer-skill/requirements.txt
ENV PYTHONPATH "${PYTHONPATH}:/app"
CMD ["uvicorn", "writer-skill.src.__main__:app", "--host", "0.0.0.0", "--port", "5000"]
