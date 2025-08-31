@PHONY: setup
setup:
	git config core.hooksPath ./githooks

.PHONY: format-staged
format-staged:
	@files=$$(git diff --cached --name-only --diff-filter=ACMRTUXB | grep -E '\.kt[s]?$$' | paste -sd: -); \
	if [ -n "$$files" ]; then \
		./gradlew ktfmtPrecommit --include-only="$$files"; \
	else \
		echo "No staged Kotlin files to format."; \
	fi

.PHONY: format
format:
	./gradlew ktfmtPrecommit

@PHONY:
start-docker:
	@docker-compose down
	@docker-compose up -d
