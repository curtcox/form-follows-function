/**
 * 				NativeFmodEx Project
 *
 * Want to use FMOD Ex API (www.fmod.org) in the Java language ? NativeFmodEx is made for you.
 * Copyright � 2005-2008 J�r�me JOUVIE (Jouvieje)
 *
 * Created on 23 feb. 2005
 * @version file v1.4.3
 * @author J�r�me JOUVIE (Jouvieje)
 * 
 * 
 * WANT TO CONTACT ME ?
 * E-mail :
 * 		jerome.jouvie@gmail.com
 * My web sites :
 * 		http://jerome.jouvie.free.fr/
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
#include "Utils.h"
#include "Pointer.h"
#include "fmod.h"
#include "fmod.hpp"
#include "fmod_codec.h"
#include "fmod_dsp.h"
#include "fmod_output.h"
#include "org_jouvieje_FmodEx_FmodExJNI.h"
#include "CallbackManager.h"

JNIEXPORT jint JNICALL Java_org_jouvieje_FmodEx_FmodExJNI_Reverb_1release(JNIEnv *java_env, jclass jcls, jlong jpointer) {
	if(!jpointer) {
		ThrowException(java_env, NullPointerException, NULL_REVERB);
		return 0;
	}
	POINTER_TYPE pointer = (POINTER_TYPE)jpointer;

	FMOD_RESULT result_ = (*(FMOD::Reverb **)&pointer)->release();

	return (jint)result_;
}

JNIEXPORT jint JNICALL Java_org_jouvieje_FmodEx_FmodExJNI_Reverb_1set3DAttributes(JNIEnv *java_env, jclass jcls, jlong jpointer, jlong jposition, jfloat jmindistance, jfloat jmaxdistance) {
	if(!jpointer) {
		ThrowException(java_env, NullPointerException, NULL_REVERB);
		return 0;
	}
	POINTER_TYPE pointer = (POINTER_TYPE)jpointer;
	FMOD_VECTOR *position = 0;
	if(jposition) {
		POINTER_TYPE positionTmp = (POINTER_TYPE)jposition;
		position = *(FMOD_VECTOR **)&positionTmp;
	}
	float mindistance = (float)jmindistance;
	float maxdistance = (float)jmaxdistance;

	FMOD_RESULT result_ = (*(FMOD::Reverb **)&pointer)->set3DAttributes(position, mindistance, maxdistance);

	return (jint)result_;
}

JNIEXPORT jint JNICALL Java_org_jouvieje_FmodEx_FmodExJNI_Reverb_1get3DAttributes(JNIEnv *java_env, jclass jcls, jlong jpointer, jlong jposition, jobject jmindistance, jlong jmindistance_, jobject jmaxdistance, jlong jmaxdistance_) {
	if(!jpointer) {
		ThrowException(java_env, NullPointerException, NULL_REVERB);
		return 0;
	}
	POINTER_TYPE pointer = (POINTER_TYPE)jpointer;
	FMOD_VECTOR *position = 0;
	if(jposition) {
		POINTER_TYPE positionTmp = (POINTER_TYPE)jposition;
		position = *(FMOD_VECTOR **)&positionTmp;
	}
	float *mindistance = 0;
	if(jmindistance) {
		mindistance = (float *)((char *)java_env->GetDirectBufferAddress(jmindistance)+jmindistance_);
	}
	float *maxdistance = 0;
	if(jmaxdistance) {
		maxdistance = (float *)((char *)java_env->GetDirectBufferAddress(jmaxdistance)+jmaxdistance_);
	}

	FMOD_RESULT result_ = (*(FMOD::Reverb **)&pointer)->get3DAttributes(position, mindistance, maxdistance);

	return (jint)result_;
}

JNIEXPORT jint JNICALL Java_org_jouvieje_FmodEx_FmodExJNI_Reverb_1setProperties(JNIEnv *java_env, jclass jcls, jlong jpointer, jlong jproperties) {
	if(!jpointer) {
		ThrowException(java_env, NullPointerException, NULL_REVERB);
		return 0;
	}
	POINTER_TYPE pointer = (POINTER_TYPE)jpointer;
	FMOD_REVERB_PROPERTIES *properties = 0;
	if(jproperties) {
		POINTER_TYPE propertiesTmp = (POINTER_TYPE)jproperties;
		properties = *(FMOD_REVERB_PROPERTIES **)&propertiesTmp;
	}

	FMOD_RESULT result_ = (*(FMOD::Reverb **)&pointer)->setProperties(properties);

	return (jint)result_;
}

JNIEXPORT jint JNICALL Java_org_jouvieje_FmodEx_FmodExJNI_Reverb_1getProperties(JNIEnv *java_env, jclass jcls, jlong jpointer, jlong jproperties) {
	if(!jpointer) {
		ThrowException(java_env, NullPointerException, NULL_REVERB);
		return 0;
	}
	POINTER_TYPE pointer = (POINTER_TYPE)jpointer;
	FMOD_REVERB_PROPERTIES *properties = 0;
	if(jproperties) {
		POINTER_TYPE propertiesTmp = (POINTER_TYPE)jproperties;
		properties = *(FMOD_REVERB_PROPERTIES **)&propertiesTmp;
	}

	FMOD_RESULT result_ = (*(FMOD::Reverb **)&pointer)->getProperties(properties);

	return (jint)result_;
}

JNIEXPORT jint JNICALL Java_org_jouvieje_FmodEx_FmodExJNI_Reverb_1setActive(JNIEnv *java_env, jclass jcls, jlong jpointer, jboolean jactive) {
	if(!jpointer) {
		ThrowException(java_env, NullPointerException, NULL_REVERB);
		return 0;
	}
	POINTER_TYPE pointer = (POINTER_TYPE)jpointer;
	bool active = (bool)(jactive != 0);

	FMOD_RESULT result_ = (*(FMOD::Reverb **)&pointer)->setActive(active);

	return (jint)result_;
}

JNIEXPORT jint JNICALL Java_org_jouvieje_FmodEx_FmodExJNI_Reverb_1getActive(JNIEnv *java_env, jclass jcls, jlong jpointer, jobject jactive, jlong jactive_) {
	if(!jpointer) {
		ThrowException(java_env, NullPointerException, NULL_REVERB);
		return 0;
	}
	POINTER_TYPE pointer = (POINTER_TYPE)jpointer;
	bool *active = 0;
	if(jactive) {
		active = (bool *)((char *)java_env->GetDirectBufferAddress(jactive)+jactive_);
	}

	FMOD_RESULT result_ = (*(FMOD::Reverb **)&pointer)->getActive(active);

	return (jint)result_;
}

JNIEXPORT jint JNICALL Java_org_jouvieje_FmodEx_FmodExJNI_Reverb_1setUserData(JNIEnv *java_env, jclass jcls, jlong jpointer, jlong juserdata) {
	if(!jpointer) {
		ThrowException(java_env, NullPointerException, NULL_REVERB);
		return 0;
	}
	POINTER_TYPE pointer = (POINTER_TYPE)jpointer;
	POINTER_TYPE userdataTmp = (POINTER_TYPE)juserdata;
	void *userdata = *(void **)&userdataTmp;

	FMOD_RESULT result_ = (*(FMOD::Reverb **)&pointer)->setUserData(userdata);

	return (jint)result_;
}

JNIEXPORT jint JNICALL Java_org_jouvieje_FmodEx_FmodExJNI_Reverb_1getUserData(JNIEnv *java_env, jclass jcls, jlong jpointer, jobject juserdata) {
	if(!jpointer) {
		ThrowException(java_env, NullPointerException, NULL_REVERB);
		return 0;
	}
	POINTER_TYPE pointer = (POINTER_TYPE)jpointer;
	void *userdata/* = 0*/;

	FMOD_RESULT result_ = (*(FMOD::Reverb **)&pointer)->getUserData(&userdata);

	if(juserdata) {
		POINTER_TYPE newAddress/* = 0*/;
		*(void **)&newAddress = userdata;
		setPointerAddress(java_env, juserdata, newAddress);
	}
	return (jint)result_;
}


