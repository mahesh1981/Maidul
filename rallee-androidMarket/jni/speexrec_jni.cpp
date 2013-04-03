#include <jni.h>

#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <speex/speex.h>
#include <speex/speex_echo.h>
#include <speex/speex_preprocess.h>
#include <speex/speex_bits.h>
#include <speex/speex_buffer.h>
#include <speex/speex_header.h>
#include <speex/speex_types.h>

// the header length of the RTP frame (must skip when en/decoding)
static const int rtp_header = 0;

int codec_status = 0;

const int CODEC_OPENED = 1;
const int CODEC_CLOSED = 0;

int aec_status = 0;

const int AEC_OPENED = 1;
const int AEC_CLOSED = 0;

static int enc_frame_size;

static SpeexBits ebits;
void *enc_state;

//#define FRAME_SIZE 320
//#define FILTER_LENGTH 3200

SpeexEchoState *echoState;
SpeexPreprocessState *den;
int sampleRate = 16000;

///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////  Echo Cancellation //////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////

extern "C"
JNIEXPORT jint JNICALL Java_com_radiorunt_utilities_jni_Speexrec_getAecStatus()
{
	return (jint)aec_status;
}

extern "C"
JNIEXPORT void Java_com_radiorunt_utilities_jni_Speexrec_initEcho(JNIEnv *env, jobject jobj, jint frame_size, jint filter_length)
{
	if(aec_status==AEC_OPENED)
	return;
	aec_status = AEC_OPENED;

	int frm_size;
	int f_length;

	frm_size = frame_size;
	f_length = filter_length;

    echoState = speex_echo_state_init(frame_size, filter_length);
    den =  speex_preprocess_state_init(frame_size, sampleRate);
	speex_echo_ctl(echoState, SPEEX_ECHO_SET_SAMPLING_RATE, &sampleRate);
	speex_preprocess_ctl(den, SPEEX_PREPROCESS_SET_ECHO_STATE, echoState);
}

extern "C"
JNIEXPORT void Java_com_radiorunt_utilities_jni_Speexrec_speexEchoPlayback	(JNIEnv *env, jobject jobj, jshortArray echo_frame){

	jshort echo_frame_buf[enc_frame_size];

//	speex_echo_state_reset(echoState);

	env->GetShortArrayRegion(echo_frame, 0, enc_frame_size, echo_frame_buf);

	speex_echo_playback(echoState, echo_frame_buf);

	env->SetShortArrayRegion(echo_frame, 0, enc_frame_size, echo_frame_buf);
}

extern "C"
JNIEXPORT void Java_com_radiorunt_utilities_jni_Speexrec_speexEchoCapture	(JNIEnv * env, jobject jobj, jshortArray inputFrame, jshortArray outputFrame){

	jshort inputFrame_buf[enc_frame_size];
	jshort outputFrame_buf[enc_frame_size];

	env->GetShortArrayRegion(inputFrame, 0, enc_frame_size, inputFrame_buf);
//	env->GetShortArrayRegion(outputFrame, 0, enc_frame_size, outputFrame_buf);

	speex_echo_capture(echoState, inputFrame_buf, outputFrame_buf);

	env->SetShortArrayRegion(outputFrame, 0, enc_frame_size, outputFrame_buf);
}


extern "C"
JNIEXPORT void Java_com_radiorunt_utilities_jni_Speexrec_speexEchoCaptureEncode	(JNIEnv * env, jobject jobj, jshortArray inputFrame, jbyteArray outputFrame, jint size){

	jshort inputFrame_buf[enc_frame_size];
	jshort capture_buff[enc_frame_size];
	jbyte output_buffer[enc_frame_size];
	jsize encoded_length = size;
	jshort buffer[enc_frame_size];

	env->GetShortArrayRegion(inputFrame, 0, enc_frame_size, inputFrame_buf);

	speex_echo_capture(echoState, inputFrame_buf, capture_buff);

	speex_bits_reset(&ebits);

	speex_encode_int(enc_state, capture_buff, &ebits);

	jint tot_bytes = speex_bits_write(&ebits, (char *)output_buffer, enc_frame_size);
	env->SetByteArrayRegion(outputFrame, 0, tot_bytes, output_buffer);
}

extern "C"
JNIEXPORT void Java_com_radiorunt_utilities_jni_Speexrec_speexPreprocess	(JNIEnv * env, jobject jobj, jshortArray inputFrame){
	jshort inputFrame_buf[enc_frame_size];
	env->GetShortArrayRegion(inputFrame, 0, enc_frame_size, inputFrame_buf);

	speex_preprocess_run(den, inputFrame_buf);

	env->SetShortArrayRegion(inputFrame, 0, enc_frame_size, inputFrame_buf);

}

extern "C"
JNIEXPORT void Java_com_radiorunt_utilities_jni_Speexrec_echoCancellation	(JNIEnv *env, jshortArray rec, jshortArray play, jshortArray out ){

	jshort echo_buf[enc_frame_size];
	jshort ref_buf[enc_frame_size];
	jshort e_buf[enc_frame_size];

	env->GetShortArrayRegion(rec, 0, enc_frame_size, echo_buf);
	env->GetShortArrayRegion(play, 0, enc_frame_size, ref_buf);

	speex_echo_cancellation(echoState, echo_buf, ref_buf, e_buf);
//	speex_preprocess_run(den, e_buf);

	env->SetShortArrayRegion(out, 0, enc_frame_size, e_buf);

}

extern "C"
JNIEXPORT void Java_com_radiorunt_utilities_jni_Speexrec_echoCancellationEncode	(JNIEnv *env, jshortArray rec, jshortArray play, jbyteArray encoded ){

	jshort echo_buf[enc_frame_size];
	jshort ref_buf[enc_frame_size];
	jshort e_buf[enc_frame_size];
	jbyte output_buffer[enc_frame_size];

	env->GetShortArrayRegion(rec, 0, enc_frame_size, echo_buf);
	env->GetShortArrayRegion(play, 0, enc_frame_size, ref_buf);

	speex_echo_cancellation(echoState, echo_buf, ref_buf, e_buf);
	speex_preprocess_run(den, e_buf);

		speex_bits_reset(&ebits);

	speex_encode_int(enc_state, e_buf, &ebits);

	jint tot_bytes = speex_bits_write(&ebits, (char *)output_buffer, enc_frame_size);
	env->SetByteArrayRegion(encoded, 0, tot_bytes, output_buffer);

}




//// Destroing echo cancelation
extern "C"
JNIEXPORT void Java_com_radiorunt_utilities_jni_Speexrec_destroyEcho(JNIEnv * env, jobject jobj){
	if(aec_status==AEC_CLOSED)
	return;
	aec_status=AEC_CLOSED;

speex_echo_state_destroy(echoState);
speex_preprocess_state_destroy(den);
}

///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
//////////////////// End of Echo Cancellation /////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////          Encoder Functions        /////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
extern "C"
JNIEXPORT jint JNICALL Java_com_radiorunt_utilities_jni_Speexrec_getEncoderFrameSize()
{
	return (jint)enc_frame_size;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_radiorunt_utilities_jni_Speexrec_getStatus()
{
	return (jint)codec_status;
}

extern "C"
JNIEXPORT void JNICALL Java_com_radiorunt_utilities_jni_Speexrec_open
(JNIEnv *env, jobject obj, jint compression) {
	if(codec_status==CODEC_OPENED)
	return;
	codec_status = CODEC_OPENED;
	int tmp;
	speex_bits_init(&ebits);

	enc_state = speex_encoder_init(&speex_wb_mode);
	tmp = compression;
	speex_encoder_ctl(enc_state, SPEEX_SET_QUALITY, &tmp);
	speex_encoder_ctl(enc_state, SPEEX_GET_FRAME_SIZE, &enc_frame_size);
}

extern "C"
JNIEXPORT jint JNICALL Java_com_radiorunt_utilities_jni_Speexrec_encode
(JNIEnv *env, jobject obj, jshortArray in, jbyteArray encoded) {

	if(codec_status==CODEC_CLOSED)
	return (jint)0;

	jshort buffer[enc_frame_size];
	jbyte output_buffer[enc_frame_size];

	speex_bits_reset(&ebits);
	env->GetShortArrayRegion(in, 0, enc_frame_size, buffer);

	speex_encode_int(enc_state, buffer, &ebits);

	jint tot_bytes = speex_bits_write(&ebits, (char *)output_buffer, enc_frame_size);
	env->SetByteArrayRegion(encoded, 0, tot_bytes, output_buffer);

	return (jint)tot_bytes;
}

extern "C"
JNIEXPORT void JNICALL Java_com_radiorunt_utilities_jni_Speexrec_close
(JNIEnv *env, jobject obj) {
	if(codec_status==CODEC_CLOSED)
	return;

	codec_status=CODEC_CLOSED;

	speex_bits_destroy(&ebits);
	speex_encoder_destroy(enc_state);
}
///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////       End of  Encoder Functions    //////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////
