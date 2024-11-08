build:
	mvn clean install

run-dev:
	mvn clean spring-boot:run

prod:
	mvn -Drevision=0.0.1 clean package -Pproduction

image:
	docker build -t amithkoujalgi/ollama4j-web-ui:0.0.1 .

run-image:
	docker run -it -p 9090:8080 -e OLLAMA_HOST_ADDR='http://192.168.29.223:11434' amithkoujalgi/ollama4j-web-ui:0.0.1