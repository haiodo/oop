MARP ?= marp
PORT ?= 8080
OUT  ?= build

DECKS := $(sort $(wildcard s*.md) $(wildcard extra_*.md))
NAMES := $(DECKS:.md=)
OPEN  := $(shell command -v open >/dev/null 2>&1 && echo open || echo xdg-open)

.DEFAULT_GOAL := help
.PHONY: help serve html pdf clean $(NAMES)

help:
	@echo "make serve            - открыть все презентации в браузере (http://localhost:$(PORT)), с автообновлением"
	@echo "make <имя>            - собрать одну презентацию и открыть её"
	@echo "make html | make pdf  - собрать все в $(OUT)/"
	@echo "make clean            - удалить $(OUT)/"
	@echo
	@echo "Презентации:"
	@printf '  %s\n' $(NAMES)

serve:
	PORT=$(PORT) $(MARP) --server .

$(OUT):
	@mkdir -p $(OUT)

$(OUT)/%.html: %.md | $(OUT)
	$(MARP) $< -o $@

$(OUT)/%.pdf: %.md | $(OUT)
	$(MARP) --pdf --pdf-outlines $< -o $@

# make s05_move_smartptr - собрать эту презентацию и открыть в браузере
$(NAMES): %: $(OUT)/%.html
	$(OPEN) $<

html: $(addprefix $(OUT)/,$(addsuffix .html,$(NAMES)))

pdf: $(addprefix $(OUT)/,$(addsuffix .pdf,$(NAMES)))

clean:
	rm -rf $(OUT)
