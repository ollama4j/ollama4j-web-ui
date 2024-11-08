# Liberica JDK is a lightweight, fully open-source distribution of OpenJDK, free for all users including commercial
# and production use, and verified by TCK for Java SE.
#
# https://hub.docker.com/r/bellsoft/liberica-openjdk-alpine
# https://bell-sw.com/libericajdk-containers/

FROM bellsoft/liberica-openjdk-alpine:17.0.12-10

LABEL maintainer="Amith Koujalgi <koujalgi.amith@gmail.com>"

RUN apk --no-cache add curl

RUN apk add fontconfig ttf-dejavu

COPY target/*.jar /app/

RUN touch /app/application.yaml

ENV JAVA_OPTS="-Xms512m -Xmx1024m"

ENV APP_CFG_FILE_PATH="/app/application.yaml"

HEALTHCHECK --interval=5s --timeout=30s --retries=30 CMD curl -I -XGET http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=8080 -jar /app/ollama4j-web-ui.jar"]