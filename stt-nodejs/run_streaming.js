const fs = require('fs');
const wav = require('wav');
const grpc = require('@grpc/grpc-js');

const Messages = require('./compiled/vnpt_asr_pb');
const Services = require('./compiled/vnpt_asr_grpc_pb');
const AudioEncoding = require('./compiled/vnpt_audio_pb');


const audioFile = '../audio/4s.wav';
const serverAddress = 'grpc.vnpt.vn:443'
const metadata = new grpc.Metadata();
metadata.add('authorization', '');
metadata.add('token-id', '');
metadata.add('token-key', '');


let client = new Services.VnptSpeechRecognitionClient(
  // serverAddress, grpc.credentials.createInsecure()
  serverAddress, grpc.credentials.createSsl()
);

let call = client.streamingRecognize(metadata);
call.on('data', function (data) {
  data = data.toObject()
  console.log(data);
  if (data.resultsList.length > 0) {
    console.log(data.resultsList.at(-1).alternativesList[0]);
  }
})
call.on('end', function () {
  console.log('End');
})
call.on('error', function (e) {
  console.log(e)
})


const CHUNK_SIZE = 2048

// Create a readable stream from your WAV file
const file = fs.createReadStream(audioFile, {highWaterMark: CHUNK_SIZE});

// Create a WAV Reader instance
const reader = new wav.Reader();

// Pipe the file stream into the WAV reader
file.pipe(reader);

// Listen for the "format" event to get the audio metadata from wav file's header
reader.on('format', function (format) {
  console.log('Audio Format Information:', format);
  // Format object contains:
  // {
  //   audioFormat: 1,      // PCM
  //   channels: 2,         // Stereo
  //   sampleRate: 44100,   // e.g., 44.1 kHz
  //   byteRate: 176400,
  //   blockAlign: 4,
  //   bitsPerSample: 16,
  //   extra: {}
  // }

  // set streaming config
  let config = new Messages.RecognitionConfig()
  config.setEncoding(AudioEncoding.AudioEncoding.LINEAR_PCM);
  config.setSampleRateHertz(format.sampleRate);
  config.setMaxAlternatives(1);
  config.setLanguageCode('vi-VN');
  let streamingConfig = new Messages.StreamingRecognitionConfig();
  streamingConfig.setConfig(config);
  streamingConfig.setInterimResults(true);

  console.log(streamingConfig)
  console.log(streamingConfig.toObject())

  // initial request to send the config
  let streamingRequest = new Messages.StreamingRecognizeRequest()
  streamingRequest.setStreamingConfig(streamingConfig);
  call.write(streamingRequest)
});

// Listen for "data" events from the reader stream
// Each chunk here will be a Buffer of raw PCM audio data (headers stripped)
reader.on('data', function (chunk) {
  console.log(`Received data chunk of size: ${chunk.length} bytes`);
  let streamingRequest = new Messages.StreamingRecognizeRequest()
  streamingRequest.setAudioContent(chunk);
  call.write(streamingRequest);
});

// Listen for the "end" event when the file reading is complete
reader.on('end', function () {
  console.log('audio end');
  call.end(); // signal the end of the call
});
