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

#if defined(__GNUC__)
    typedef long long __int64; /*For gcc on Windows */
#endif

#include "fmod_event.h"
#include "NativeFmodDesigner.h"
#include "CallbackManager.h"
#include "org_jouvieje_FmodDesigner_Defines_DefineJNI.h"
#include "org_jouvieje_FmodDesigner_InitFmodDesigner.h"
#pragma comment(lib, "fmod_event_net.lib")

JNIEXPORT jint JNICALL Java_org_jouvieje_FmodDesigner_Defines_DefineJNI_get_1FMOD_1EVENT_1VERSION(JNIEnv *jenv, jclass jcls) {
	return FMOD_EVENT_VERSION;
}

JNIEXPORT jint JNICALL Java_org_jouvieje_FmodDesigner_Defines_DefineJNI_get_1FMOD_1EVENT_1NET_1VERSION(JNIEnv *jenv, jclass jcls) {
	return FMOD_EVENT_NET_VERSION;
}

JNIEXPORT jint JNICALL Java_org_jouvieje_FmodDesigner_Defines_DefineJNI_get_1NATIVEFMODDESIGNER_1VERSION(JNIEnv *, jclass) {
	return NATIVEFMODDESIGNER_VERSION;
}

JNIEXPORT void JNICALL Java_org_jouvieje_FmodDesigner_InitFmodDesigner_attachJavaVM(JNIEnv *jenv, jclass jcls) {
	attachJavaVM(jenv);
}
