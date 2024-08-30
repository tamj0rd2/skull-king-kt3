@PHONY: setup
setup:
	git config core.hooksPath ./githooks

#(trace, debug, info, warn, error
@PHONY: format
format:
	@ktlint -F -l error >/dev/null 2>&1 || true
