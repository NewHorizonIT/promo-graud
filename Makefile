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

# FRONTEND
fe-install:
	cd promo-graud-fe && npm install

fe-dev:
	cd promo-graud-fe && npm run dev

fe-build:
	cd promo-graud-fe && npm run build

fe-lint:
	cd promo-graud-fe && npm run lint

fe-format:
	cd promo-graud-fe && npm run format

fe-format-check:
	cd promo-graud-fe && npm run format:check

# INTEGRATION
dev: up be-run

.PHONY: up down logs be-run be-build be-test be-validate be-clean dev \
        fe-install fe-dev fe-build fe-lint fe-format fe-format-check
