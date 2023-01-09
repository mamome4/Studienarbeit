FROM maven

COPY EvaluationContent/CucumberTest/ /usr/src/myapp/
WORKDIR /usr/src/myapp