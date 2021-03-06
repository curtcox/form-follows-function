#
# Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.  Sun designates this
# particular file as subject to the "Classpath" exception as provided
# by Sun in the LICENSE file that accompanied this code.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
# CA 95054 USA or visit www.sun.com if you need additional information or
# have any questions.
#

# Makefile for langtools: wrapper around Ant build.xml file

#
# On Solaris, the 'make' utility from Sun will not work with these makefiles.
#    This little rule is only understood by Sun's make, and is harmless
#    when seen by the GNU make tool. If using Sun's make, this causes the
#    make command to fail.
#
SUN_MAKE_TEST:sh = @echo "ERROR: PLEASE USE GNU VERSION OF MAKE"; exit 33

#
# Minimal platform defs
# Need FullPath because we can't rely on gnumake abspath, until we use v3.81
#

SYSTEM_UNAME := $(shell uname)

ifeq ($(SYSTEM_UNAME), Windows_NT)
DEV_NULL = NUL
else
ifneq (,$(findstring CYGWIN,$(SYSTEM_UNAME)))
DEV_NULL = NUL
USING_CYGWIN = true
else
DEV_NULL = /dev/null
endif 
endif

ifdef USING_CYGWIN
define FullPath
$(shell cygpath -a -s -m $1 2> $(DEV_NULL))
endef
else
define FullPath
$(shell cd $1 2> $(DEV_NULL) && pwd)
endef
endif

#
# Makefile args
#

ifdef QUIET
  ANT_OPTIONS += -quiet
endif

ifdef VERBOSE
  ANT_OPTIONS += -verbose -diagnostics
endif

ifdef JDK_VERSION
  ANT_OPTIONS += -Djdk.version=$(JDK_VERSION)
endif 

ifdef FULL_VERSION
  ANT_OPTIONS += -Dfull.version='$(FULL_VERSION)' # will contain spaces
endif 

ifdef MILESTONE
  ANT_OPTIONS += -Dmilestone=$(MILESTONE)
endif

ifdef BUILD_NUMBER
  ANT_OPTIONS += -Dbuild.number=$(BUILD_NUMBER)
else
  ifdef JDK_BUILD_NUMBER
    ANT_OPTIONS += -Dbuild.number=$(JDK_BUILD_NUMBER)
  endif
endif

ifeq ($(VARIANT), DBG)
  ANT_OPTIONS += -Djavac.debug=true
else
  ifeq ($(VARIANT), OPT)
    ANT_OPTIONS += -Djavac.debug=false
  endif
endif

# Note: jdk/make/common/Defs.gmk uses LANGUAGE_VERSION (-source NN)
# and the somewhat misnamed CLASS_VERSION (-target NN)
ifdef TARGET_CLASS_VERSION
  ANT_OPTIONS += -Djavac.target=$(TARGET_CLASS_VERSION)
else
  ifdef JAVAC_TARGET_ARG 
    ANT_OPTIONS += -Djavac.target=$(JAVAC_TARGET_ARG)
  endif
endif 

ifdef ALT_BOOTDIR
  ANT_OPTIONS += -Dboot.java.home=$(ALT_BOOTDIR)
  ANT_JAVA_HOME = JAVA_HOME=$(ALT_BOOTDIR)
endif

ifdef ALT_OUTPUTDIR
  OUTPUTDIR = $(ALT_OUTPUTDIR)
  ANT_OPTIONS += -Dbuild.dir=$(ALT_OUTPUTDIR)/build
  ANT_OPTIONS += -Ddist.dir=$(ALT_OUTPUTDIR)/dist
else
  OUTPUTDIR = ..
endif
#ABS_OUTPUTDIR = $(abspath $(OUTPUTDIR))
ABS_OUTPUTDIR = $(call FullPath,$(OUTPUTDIR))

ANT_TMPDIR = $(ABS_OUTPUTDIR)/build/ant-tmp
ANT_OPTS = ANT_OPTS=-Djava.io.tmpdir='$(ANT_TMPDIR)'

ifdef FINDBUGS_HOME
  ANT_OPTIONS += -Dfindbugs.home=$(FINDBUGS_HOME)
endif

ifdef ANT_HOME
  ANT = $(ANT_HOME)/bin/ant
  ifneq ($(shell test -x $(ANT) && echo OK), OK)
    $(error $(ANT) not found -- please update ANT_HOME)
  endif
else
  ANT = ant
  ifneq ($(shell test -x "`which $(ANT)`" && echo OK), OK)
    $(error 'ant' not found -- please set ANT_HOME or put 'ant' on your PATH)
  endif
endif

# Default target and expected 'do everything' target
all: build

# Standard make clobber target
clobber: clean

# All ant targets of interest
ANT_TARGETS = build clean sanity post-sanity diagnostics # for now

# Create a make target for each
$(ANT_TARGETS):
	@ mkdir -p $(OUTPUTDIR)/build $(ANT_TMPDIR)
	$(ANT_JAVA_HOME) $(ANT_OPTS) $(ANT) -diagnostics > $(OUTPUTDIR)/build/ant-diagnostics.log
	$(ANT_JAVA_HOME) $(ANT_OPTS) $(ANT) $(ANT_OPTIONS) $@

#-------------------------------------------------------------------
#
# Targets for Sun's internal JPRT build system

CD = cd
ZIP = zip

JPRT_ARCHIVE_BUNDLE=$(ABS_OUTPUTDIR)/$(JPRT_BUILD_FLAVOR)-bundle.zip

jprt_build_product jprt_build_debug jprt_build_fastdebug: all
	( $(CD) $(OUTPUTDIR) && \
	  $(ZIP) -q -r $(JPRT_ARCHIVE_BUNDLE) build dist )

#-------------------------------------------------------------------

# Declare these phony (not filenames)
.PHONY: $(ANT_TARGETS) all clobber \
	jprt_build_product jprt_build_debug jprt_build_fastdebug
