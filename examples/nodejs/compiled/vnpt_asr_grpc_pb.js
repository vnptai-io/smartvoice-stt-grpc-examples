// GENERATED CODE -- DO NOT EDIT!

'use strict';
var grpc = require('@grpc/grpc-js');
var vnpt_asr_pb = require('./vnpt_asr_pb.js');
var vnpt_audio_pb = require('./vnpt_audio_pb.js');

function serialize_vnpt_audio_asr_RecognizeRequest(arg) {
  if (!(arg instanceof vnpt_asr_pb.RecognizeRequest)) {
    throw new Error('Expected argument of type vnpt.audio.asr.RecognizeRequest');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_vnpt_audio_asr_RecognizeRequest(buffer_arg) {
  return vnpt_asr_pb.RecognizeRequest.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_vnpt_audio_asr_RecognizeResponse(arg) {
  if (!(arg instanceof vnpt_asr_pb.RecognizeResponse)) {
    throw new Error('Expected argument of type vnpt.audio.asr.RecognizeResponse');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_vnpt_audio_asr_RecognizeResponse(buffer_arg) {
  return vnpt_asr_pb.RecognizeResponse.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_vnpt_audio_asr_StreamingRecognizeRequest(arg) {
  if (!(arg instanceof vnpt_asr_pb.StreamingRecognizeRequest)) {
    throw new Error('Expected argument of type vnpt.audio.asr.StreamingRecognizeRequest');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_vnpt_audio_asr_StreamingRecognizeRequest(buffer_arg) {
  return vnpt_asr_pb.StreamingRecognizeRequest.deserializeBinary(new Uint8Array(buffer_arg));
}

function serialize_vnpt_audio_asr_StreamingRecognizeResponse(arg) {
  if (!(arg instanceof vnpt_asr_pb.StreamingRecognizeResponse)) {
    throw new Error('Expected argument of type vnpt.audio.asr.StreamingRecognizeResponse');
  }
  return Buffer.from(arg.serializeBinary());
}

function deserialize_vnpt_audio_asr_StreamingRecognizeResponse(buffer_arg) {
  return vnpt_asr_pb.StreamingRecognizeResponse.deserializeBinary(new Uint8Array(buffer_arg));
}


var VnptSpeechRecognitionService = exports.VnptSpeechRecognitionService = {
  recognize: {
    path: '/vnpt.audio.asr.VnptSpeechRecognition/Recognize',
    requestStream: false,
    responseStream: false,
    requestType: vnpt_asr_pb.RecognizeRequest,
    responseType: vnpt_asr_pb.RecognizeResponse,
    requestSerialize: serialize_vnpt_audio_asr_RecognizeRequest,
    requestDeserialize: deserialize_vnpt_audio_asr_RecognizeRequest,
    responseSerialize: serialize_vnpt_audio_asr_RecognizeResponse,
    responseDeserialize: deserialize_vnpt_audio_asr_RecognizeResponse,
  },
  streamingRecognize: {
    path: '/vnpt.audio.asr.VnptSpeechRecognition/StreamingRecognize',
    requestStream: true,
    responseStream: true,
    requestType: vnpt_asr_pb.StreamingRecognizeRequest,
    responseType: vnpt_asr_pb.StreamingRecognizeResponse,
    requestSerialize: serialize_vnpt_audio_asr_StreamingRecognizeRequest,
    requestDeserialize: deserialize_vnpt_audio_asr_StreamingRecognizeRequest,
    responseSerialize: serialize_vnpt_audio_asr_StreamingRecognizeResponse,
    responseDeserialize: deserialize_vnpt_audio_asr_StreamingRecognizeResponse,
  },
};

exports.VnptSpeechRecognitionClient = grpc.makeGenericClientConstructor(VnptSpeechRecognitionService, 'VnptSpeechRecognition');
