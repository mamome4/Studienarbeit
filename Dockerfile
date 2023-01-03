FROM maven

COPY EvaluationContent/CucumberTest/ /usr/src/myapp/
COPY EvaluationContent/main/ /usr/src/myapp/src/main/
WORKDIR /usr/src/myapp

RUN mvn test