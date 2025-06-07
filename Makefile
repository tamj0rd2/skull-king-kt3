@PHONY: setup
setup:
	git config core.hooksPath ./githooks

.PHONY: format
format:
	@git ls-files --cached --others --exclude-standard "*.kt" "*.kts" | xargs ktfmt --kotlinlang-style -F

@PHONY:
start-docker:
	@docker-compose down
	@docker-compose up -d
