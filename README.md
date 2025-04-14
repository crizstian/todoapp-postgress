# FME JAVA WORKSHOP

### Requisitos:

- Docker Desktop o Rancher Desktop
- VScode
- VScode devcontainer plugin

### Pasos

- abrir proyecto en VSCode
- abrir proyecto con devcontainer

### Compilacion
```
#!/bin/bash
# Set Maven version
MAVEN_VERSION="3.9.5"
# Create directory for Maven
mkdir -p ~/maven
# Download Maven
curl -fsSL https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz -o ~/maven/apache-maven-${MAVEN_VERSION}-bin.tar.gz
# Extract Maven
tar -xzf ~/maven/apache-maven-${MAVEN_VERSION}-bin.tar.gz -C ~/maven
# Set up environment variables
echo 'export M2_HOME=~/maven/apache-maven-'${MAVEN_VERSION} >> ~/.bashrc
echo 'export PATH=$M2_HOME/bin:$PATH' >> ~/.bashrc
# Source the updated bashrc
source ~/.bashrc
# Verify installation
mvn -version

cd todo-app

mvn clean install

mvn clean package

java -jar target/todo-app-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### PSQL
```
sudo apt update && sudo apt upgrade -y

sudo apt install -y postgresql postgresql-contrib

psql -h postgresdb -p 5432 -U postgres -d todo_app -f todo-app/src/main/resources/schema.sql
# password: postgres

psql -h postgresdb -p 5432 -U postgres -d todo_app -c "SELECT * FROM todos;"
# password: postgres

```
