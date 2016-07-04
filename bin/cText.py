#!/usr/bin/python

##############################################################################
##   Copyright (c) 2007 by Scott R. Little
##   University of Utah
##
##   Permission to use, copy, modify and/or distribute, but not sell, this
##   software and its documentation for any purpose is hereby granted
##   without fee, subject to the following terms and conditions:
##
##   1.  The above copyright notice and this permission notice must
##   appear in all copies of the software and related documentation.
##
##   2.  The name of University of Utah may not be used in advertising or
##   publicity pertaining to distribution of the software without the
##   specific, prior written permission of University of Utah.
##
##   3.  THE SOFTWARE IS PROVIDED "AS-IS" AND WITHOUT WARRANTY OF ANY KIND,
##   EXPRESS, IMPLIED OR OTHERWISE, INCLUDING WITHOUT LIMITATION, ANY
##   WARRANTY OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
##
##   IN NO EVENT SHALL UNIVERSITY OF UTAH OR THE AUTHORS OF THIS SOFTWARE BE
##   LIABLE FOR ANY SPECIAL, INCIDENTAL, INDIRECT OR CONSEQUENTIAL DAMAGES
##   OF ANY KIND, OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA
##   OR PROFITS, WHETHER OR NOT ADVISED OF THE POSSIBILITY OF DAMAGE, AND ON
##   ANY THEORY OF LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE
##   OR PERFORMANCE OF THIS SOFTWARE.
##
##############################################################################

#attributes for color selection
NONE = "00"
BOLD = "01"
DIM = "02"
UNDERLINE = "04"
BLINK = "05"
REVERSE = "07"
HIDDEN = "08"

#available colors for fg and bg
BLACK = 0
RED = 1
GREEN = 2
YELLOW = 3
BLUE = 4
MAGENTA = 5
CYAN = 6
WHITE = 7

def cSetAll(attr, fgColor, bgColor):
	return "\033["+attr+";"+str(fgColor+30)+";"+str(bgColor+40)+"m"

def cSetAttr(attr):
	return "\033["+attr+"m"

def cSetFg(fgColor):
	return "\033["+str(fgColor+30)+"m"

def cSetBg(bgColor):
	return "\033["+str(bgColor+40)+"m"

def cSetFgBg(fgColor,bgColor):
	return "\033["+str(fgColor+30)+";"+str(bgColor+40)+"m"

def cSetAttrFg(attr, fgColor):
	return "\033["+attr+";"+str(fgColor+30)+"m"

def cSetAttrBg(attr, bgColor):
	return "\033["+attr+";"+str(bgColor+40)+"m"
