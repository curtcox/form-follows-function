/**
 * 							NativeFmodEx Project
 *
 * Do you want to use FMOD Ex API (www.fmod.org) with the Java language ? I've created NativeFmodEx for you.
 * Copyright � 2005-2007 J�r�me JOUVIE (Jouvieje)
 *
 * Created on 23 feb. 2005
 * @version file v1.0.0
 * @author J�r�me JOUVIE (Jouvieje)
 *
 *
 * WANT TO CONTACT ME ?
 * E-mail :
 * 		jerome.jouvie@gmail.com
 * My web sites :
 * 		http://jerome.jouvie.free.fr/
 * 		http://topresult.tomato.co.uk/~jerome/
 *
 *
 * INTRODUCTION
 * FMOD Ex is an API (Application Programming Interface) that allow you to use music
 * and creating sound effects with a lot of sort of musics.
 * FMOD is at :
 * 		http://www.fmod.org/
 * The reason of this project is that FMOD Ex can't be used direcly with Java, so I've created
 * this project to do this.
 *
 *
 * GNU LESSER GENERAL PUBLIC LICENSE
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */

#ifndef NATIVEFMODEX_H_
#define NATIVEFMODEX_H_

/*
 * NativeFmodEx version (same format than FMOD version !) :
 * 0xaaaabbcc -> aaaa = major version number.  bb = minor version number.  cc = development version number.
 */
#define NATIVEFMODEX_VERSION 0x00010404

/*
 * PLATFORM Enumeration
 */
#define NATIVE2JAVA_WIN_32   0
#define NATIVE2JAVA_WIN_64   1
#define NATIVE2JAVA_LINUX    3
#define NATIVE2JAVA_LINUX_64 4
#define NATIVE2JAVA_MAC      5

//The platform in which this library is compiled
#if defined (_WIN32)
    #if defined (_WIN64)
        // _WIN32 might be defined as well
        #define CURRENT_PLATFORM NATIVE2JAVA_WIN_64
    #else
        #define CURRENT_PLATFORM NATIVE2JAVA_WIN_32
    #endif
#elif defined (_WIN64)
    #define CURRENT_PLATFORM NATIVE2JAVA_WIN_64
#elif defined (__linux__)
    #if defined __x86_64__
        #define CURRENT_PLATFORM NATIVE2JAVA_LINUX_64
    #else
        #define CURRENT_PLATFORM NATIVE2JAVA_LINUX
    #endif
#elif defined (__APPLE__) || defined(macosx)
    #define CURRENT_PLATFORM NATIVE2JAVA_MAC
#else
    #error Native platform not supported yet
#endif

#endif

