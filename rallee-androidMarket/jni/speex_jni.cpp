#include <jni.h>

#include <string.h>
#include <unistd.h>

#include <speex/speex.h>

// the header length of the RTP frame (must skip when en/decoding)
static const int rtp_header = 0;

int codec_status = 0;

const int CODEC_OPENED = 1;
const int CODEC_CLOSED = 0;

static int dec_frame_size;

static SpeexBits dbits;
void *dec_state;

extern "C"
JNIEXPORT jint JNICALL Java_com_radiorunt_utilities_jni_Speex_getDecoderFrameSize()
{
	return (jint)dec_frame_size;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_radiorunt_utilities_jni_Speex_getStatus()
{
	return (jint)codec_status;
}

extern "C"
JNIEXPORT void JNICALL Java_com_radiorunt_utilities_jni_Speex_open
(JNIEnv *env, jobject obj, jint compression) {
	if(codec_status==CODEC_OPENED)
	return;
	codec_status = CODEC_OPENED;
	int tmp;
	speex_bits_init(&dbits);

	dec_state = speex_decoder_init(&speex_wb_mode);
	tmp = compression;
	speex_decoder_ctl(dec_state, SPEEX_GET_FRAME_SIZE, &dec_frame_size);
}

extern "C"
JNIEXPORT jint JNICALL Java_com_radiorunt_utilities_jni_Speex_decode
(JNIEnv *env, jobject obj, jbyteArray encoded, jshortArray lin, jint size) {

	if(codec_status==CODEC_CLOSED)
	return (jint)0;

	jbyte buffer[dec_frame_size];
	jshort output_buffer[dec_frame_size];
	jsize encoded_length = size;

	env->GetByteArrayRegion(encoded, rtp_header, encoded_length, buffer);
	speex_bits_read_from(&dbits, (char *)buffer, encoded_length);
	speex_decode_int(dec_state, &dbits, output_buffer);
	env->SetShortArrayRegion(lin, 0, dec_frame_size,
			output_buffer);

	return (jint)dec_frame_size;
}

extern "C"
JNIEXPORT void JNICALL Java_com_radiorunt_utilities_jni_Speex_close
(JNIEnv *env, jobject obj) {
	if(codec_status==CODEC_CLOSED)
	return;

	codec_status=CODEC_CLOSED;

	speex_bits_destroy(&dbits);
	speex_decoder_destroy(dec_state);
}
