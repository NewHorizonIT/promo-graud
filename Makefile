BE := promo-graud-be

# DOCKER
up:
	docker compose up -d

down:
	docker compose down

logs:
	docker compose logs -f

# BACKEND
be-run:
	cd $(BE) && ./mvnw spring-boot:run

be-build:
	cd $(BE) && ./mvnw clean package -DskipTests

be-test:
	cd $(BE) && ./mvnw test

be-validate:
	cd $(BE) && ./mvnw validate

be-clean:
	cd $(BE) && ./mvnw clean

# INTERGARTION
dev: up be-run

.PHONY: up down logs be-run be-build be-test be-validate be-clean dev
