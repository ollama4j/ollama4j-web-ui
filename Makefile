build:
	mvn clean install

run-dev:
	mvn clean spring-boot:run

prod:
	mvn -Drevision=0.0.1 clean package -Pproduction