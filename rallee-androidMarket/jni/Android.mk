#Android.mk file for wd-band compression and for speex_rec for echo cancelation


LOCAL_PATH := $(call my-dir)
SPEEX   := speex

include $(CLEAR_VARS)
LOCAL_MODULE    := speexrec
LOCAL_SRC_FILES := speexrec_jni.cpp \
$(SPEEX)/libspeex/cb_search.c \
$(SPEEX)/libspeex/preprocess.c \
$(SPEEX)/libspeex/filterbank.c \
$(SPEEX)/libspeex/exc_10_32_table.c \
$(SPEEX)/libspeex/exc_8_128_table.c \
$(SPEEX)/libspeex/filters.c \
$(SPEEX)/libspeex/gain_table.c \
$(SPEEX)/libspeex/hexc_table.c \
$(SPEEX)/libspeex/high_lsp_tables.c \
$(SPEEX)/libspeex/lsp.c \
$(SPEEX)/libspeex/ltp.c \
$(SPEEX)/libspeex/speex.c \
$(SPEEX)/libspeex/stereo.c \
$(SPEEX)/libspeex/vbr.c \
$(SPEEX)/libspeex/vq.c \
$(SPEEX)/libspeex/bits.c \
$(SPEEX)/libspeex/exc_10_16_table.c \
$(SPEEX)/libspeex/exc_20_32_table.c \
$(SPEEX)/libspeex/exc_5_256_table.c \
$(SPEEX)/libspeex/exc_5_64_table.c \
$(SPEEX)/libspeex/gain_table_lbr.c \
$(SPEEX)/libspeex/hexc_10_32_table.c \
$(SPEEX)/libspeex/lpc.c \
$(SPEEX)/libspeex/lsp_tables_nb.c \
$(SPEEX)/libspeex/modes.c \
$(SPEEX)/libspeex/modes_wb.c\
$(SPEEX)/libspeex/nb_celp.c \
$(SPEEX)/libspeex/quant_lsp.c \
$(SPEEX)/libspeex/sb_celp.c \
$(SPEEX)/libspeex/speex_callbacks.c \
$(SPEEX)/libspeex/speex_header.c \
$(SPEEX)/libspeex/window.c \
$(SPEEX)/libspeex/kiss_fft.c \
$(SPEEX)/libspeex/kiss_fftr.c \
$(SPEEX)/libspeex/fftwrap.c \
$(SPEEX)/libspeex/jitter.c \
$(SPEEX)/libspeex/mdf.c \
$(SPEEX)/libspeex/resample.c \
$(SPEEX)/libspeex/buffer.c \
$(SPEEX)/libspeex/scal.c


LOCAL_C_INCLUDES := $(LOCAL_PATH)/speex/include -all
LOCAL_CFLAGS = -DFIXED_POINT -DUSE_KISS_FFT -DEXPORT="" -UHAVE_CONFIG_H -I$(LOCAL_PATH)/$(SPEEX)/include -g
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := speex
LOCAL_SRC_FILES := speex_jni.cpp \
$(SPEEX)/libspeex/cb_search.c \
$(SPEEX)/libspeex/preprocess.c \
$(SPEEX)/libspeex/filterbank.c \
$(SPEEX)/libspeex/exc_10_32_table.c \
$(SPEEX)/libspeex/exc_8_128_table.c \
$(SPEEX)/libspeex/filters.c \
$(SPEEX)/libspeex/gain_table.c \
$(SPEEX)/libspeex/hexc_table.c \
$(SPEEX)/libspeex/high_lsp_tables.c \
$(SPEEX)/libspeex/lsp.c \
$(SPEEX)/libspeex/ltp.c \
$(SPEEX)/libspeex/speex.c \
$(SPEEX)/libspeex/stereo.c \
$(SPEEX)/libspeex/vbr.c \
$(SPEEX)/libspeex/vq.c \
$(SPEEX)/libspeex/bits.c \
$(SPEEX)/libspeex/exc_10_16_table.c \
$(SPEEX)/libspeex/exc_20_32_table.c \
$(SPEEX)/libspeex/exc_5_256_table.c \
$(SPEEX)/libspeex/exc_5_64_table.c \
$(SPEEX)/libspeex/gain_table_lbr.c \
$(SPEEX)/libspeex/hexc_10_32_table.c \
$(SPEEX)/libspeex/lpc.c \
$(SPEEX)/libspeex/lsp_tables_nb.c \
$(SPEEX)/libspeex/modes.c \
$(SPEEX)/libspeex/modes_wb.c\
$(SPEEX)/libspeex/nb_celp.c \
$(SPEEX)/libspeex/quant_lsp.c \
$(SPEEX)/libspeex/sb_celp.c \
$(SPEEX)/libspeex/speex_callbacks.c \
$(SPEEX)/libspeex/speex_header.c \
$(SPEEX)/libspeex/window.c \
$(SPEEX)/libspeex/kiss_fft.c \
$(SPEEX)/libspeex/kiss_fftr.c \
$(SPEEX)/libspeex/fftwrap.c \
$(SPEEX)/libspeex/jitter.c \
$(SPEEX)/libspeex/mdf.c \
$(SPEEX)/libspeex/resample.c \
$(SPEEX)/libspeex/buffer.c \
$(SPEEX)/libspeex/scal.c


LOCAL_C_INCLUDES := $(LOCAL_PATH)/speex/include -all
LOCAL_CFLAGS = -DFIXED_POINT -DUSE_KISS_FFT -DEXPORT="" -UHAVE_CONFIG_H -I$(LOCAL_PATH)/$(SPEEX)/include -g
include $(BUILD_SHARED_LIBRARY)