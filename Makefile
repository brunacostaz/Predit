.PHONY: test run stop security

test:
	./mvnw clean verify

run:
	docker compose up --build

stop:
	docker compose down

security:
	./mvnw org.owasp:dependency-check-maven:aggregate
