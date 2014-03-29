SOURCE_VERSION = 1.7
JFLAGS ?= -g:source,lines,vars -encoding utf8
PROCESSOR_FACTORIES_MODULES ?= net.aeten.core
TOUCH_DIR = .touch


all: compile jar eclipse src test

# Sources
SRC = messenger
src: $(SRC)
messenger:: aeten.core slf4j

# COTS
COTS = aeten.core jcip.annotations slf4j
cots: $(COTS)
aeten.core::       jcip.annotations slf4j
jcip.annotations::
slf4j::

# Tests
TEST = messenger.test
test: $(TEST)
messenger.test:: messenger parsing.yaml stream slf4j.simple

# Tests COTS
TEST_COTS = parsing.yaml stream slf4j.simple
slf4j.simple:: slf4j
stream:: aeten.core
parsing.yaml:: aeten.core

clean:
	$(RM) -rf $(BUILD_DIR) $(DIST_DIR) $(GENERATED_DIR) $(TOUCH_DIR)

SRC_DIRS = src/ test/
MODULES = $(SRC) $(COTS) $(TEST) $(TEST_COTS)
include Java-make/java.mk

